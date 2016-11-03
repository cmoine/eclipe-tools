package org.eclipse.etools.icons;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.etools.Activator;
import org.eclipse.etools.util.ArrayTreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerRow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;

import com.google.common.collect.ImmutableSet;

public class IconsSearchView extends ViewPart {
	private FilteredTree tree;
	
	private Set<String> KNOWN_EXTENSIONS=ImmutableSet.of("png","gif","jpg", "jpeg", "bmp");

	private TreeViewer viewer;
	
	private static final class Entry {
		private URL url;
		private Bundle bundle;

		public Entry(URL url, Bundle bundle) {
			this.url = url;
			this.bundle = bundle;
		}
		
		@Override
		public String toString() {
			return bundle.getSymbolicName()+url.getPath();
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		PatternFilter filter = new PatternFilter();
		tree = new FilteredTree(parent, SWT.FULL_SELECTION, filter, true);
		viewer = tree.getViewer();
//		viewer.setContentProvider(new ILazyContentProvider() {
//			public void updateElement(int index) {
//				// TODO Auto-generated method stub
//				System.out.println(
//						"IconsSearchView.createPartControl(...).new ILazyContentProvider() {...}.updateElement()");
//				
//			}
//		});
		new TreeViewerRowVisibilityStateSupport(viewer).addItemStateListener(new ViewerRowStateChangeListener() {
			private ImageLoader loader=new ImageLoader();
			
			public void itemStateChangedListener(ViewerRowStateChangedEvent event) {
				for(TreeViewerRow row: event.itemsHidden) {
					if(!event.itemsVisible.contains(row)) {
						Image image = row.getImage(0);
						if(image!=null)
							image.dispose();
					}
				}
				for(TreeViewerRow row: event.itemsVisible) {
					Entry entry=(Entry) row.getElement();
					URL url=entry.url;
					InputStream is=null;
					try {
						is=url.openStream();
						ImageData[] data = loader.load(is);
						if(data.length>0) {
							row.setImage(0, new Image(Display.getDefault(), data[0]));
						}
					} catch(Exception e) {
						Activator.logError("Failed loading image from "+url, e); //$NON-NLS-1$
					} finally {
						IOUtils.closeQuietly(is);
					}
				}
			}
		});
		viewer.setContentProvider(new ArrayTreeContentProvider());
		viewer.setLabelProvider(new LabelProvider() /* {
			private Map<URL, Image> images=Maps.newHashMap();
			private ImageLoader loader=new ImageLoader();
			
//			@Override
//			public String getText(Object element) {
//				// TODO Auto-generated method stub
//				return super.getText(element);
//			}

//			@Override
//			public Image getImage(Object element) {
//				if(!images.containsKey(element)) {
//					URL url=((Entry) element).url;
//					InputStream is=null;
//					try {
//						is=url.openStream();
//						ImageData[] data = loader.load(is);
//						if(data.length>0) {
//							images.put(url, new Image(Display.getDefault(), data[0]));
//						}
//					} catch(Exception e) {
//						Activator.logError("Failed loading image from "+url, e); //$NON-NLS-1$
//					} finally {
//						IOUtils.closeQuietly(is);
//					}
//				}
//				return images.get(element);
//			}
		}*/);
		List<Entry> input=new ArrayList<Entry>();
		for(Bundle bundle: Activator.getDefault().getBundle().getBundleContext().getBundles()) {
			for(URL url: Collections.list(bundle.findEntries("/", null, true))) {
				String ext = StringUtils.substringAfterLast(url.toString(), ".");
				if(KNOWN_EXTENSIONS.contains(ext)) {
					input.add(new Entry(url, bundle));
				}
			}
		}
		viewer.setInput(input);
		getViewSite().getActionBars().getStatusLineManager().setMessage(input.size() + " images loaded");
	}

	@Override
	public void setFocus() {
		tree.setFocus();
	}
}
