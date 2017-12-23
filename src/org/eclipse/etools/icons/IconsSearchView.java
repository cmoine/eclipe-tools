package org.eclipse.etools.icons;

import java.awt.Toolkit;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.etools.Activator;
import org.eclipse.etools.util.ArrayTreeContentProvider;
import org.eclipse.etools.util.EToolsImage;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

public class IconsSearchView extends ViewPart {
	private static final int LIMIT = 100;
	
	private Set<String> KNOWN_EXTENSIONS=ImmutableSet.of("png", //$NON-NLS-1$
			"gif", //$NON-NLS-1$
			"jpg",  //$NON-NLS-1$
			"jpeg",  //$NON-NLS-1$
			"bmp"); //$NON-NLS-1$

	private static final ImageLoader LOADER=new ImageLoader();
	private List<Entry<Bundle, URL>> all=new ArrayList<Entry<Bundle, URL>>();
	private List<InputEntry> input=new ArrayList<InputEntry>(LIMIT);

	private TreeViewer viewer;
	private Tree tree;

	private Text filterText;

	private Clipboard clipboard;
	
	private static final class InputEntry {
		private URL url;
		private Bundle bundle;
		private Optional<Image> image;
		private int remaining;

		public InputEntry(Bundle bundle, URL url) {
			this.url = url;
			this.bundle = bundle;
		}
		
		public InputEntry(int remaining) {
			this.remaining = remaining;
		}
		
		public String getValue() {
			return bundle==null ? remaining + " more..." //$NON-NLS-1$
					: bundle.getSymbolicName()+url.getPath();
		}

		public Image getOrCreateImage() {
			if(image==null) {
				if(url!=null) {
					InputStream is=null;
					try {
						is=url.openStream();
						ImageData[] data = LOADER.load(is);
						if(data.length>0) {
							if(data[0].width>16 || data[0].height>16)
								data[0]=data[0].scaledTo(16, 16);
							image=Optional.of(new Image(Display.getDefault(), data[0]));
						}
					} catch(Exception e) {
						Activator.logError("Failed loading image from "+url, e); //$NON-NLS-1$
					} finally {
						IOUtils.closeQuietly(is);
					}
				}
				if(image==null)
					image=Optional.absent();
			}
			return image.orNull();
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		for(Bundle bundle: Activator.getDefault().getBundle().getBundleContext().getBundles()) {
			for(URL url: Collections.list(bundle.findEntries("/", null, true))) {
				String ext = StringUtils.substringAfterLast(url.toString(), ".");
				if(KNOWN_EXTENSIONS.contains(ext)) {
					all.add(Maps.immutableEntry(bundle, url));
				}
			}
		}

		Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));
        filterText=new Text(composite, SWT.BORDER);
        filterText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        filterText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateInput();
			}
		});
        
		viewer = new TreeViewer(composite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		tree=viewer.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer.setContentProvider(new ArrayTreeContentProvider());
		viewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((InputEntry) element).getValue();
			}

			@Override
			public Image getImage(Object element) {
				return ((InputEntry) element).getOrCreateImage();
			}
		});
		Menu menu=new Menu(tree);
		final MenuItem copyItem=new MenuItem(menu, SWT.NONE);
		clipboard = new Clipboard(parent.getDisplay());
		copyItem.setText(WorkbenchMessages.Workbench_copy);
		copyItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				InputEntry elt=(InputEntry) viewer.getStructuredSelection().getFirstElement();
				InputStream is=null;
				try {
					is=elt.url.openStream();
//					ImageLoader loader=new ImageLoader();
//					loader.load(is);
//					ByteArrayOutputStream stream = new ByteArrayOutputStream();
//					loader.save(stream, SWT.IMAGE_BMP);
//					loader.load(stream)
//					Image img=new Image(e.display, loader.data[0]);
//					clipboard.setContents(new Object[]{img.getImageData()}, new Transfer[]{ImageTransfer.getInstance()});
					Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new TransferableImage(ImageIO.read(is)), new ClipboardOwner() {
						public void lostOwnership(java.awt.datatransfer.Clipboard clipboard, Transferable contents) {
							// TODO Auto-generated method stub
							
						}
					});
//					img.dispose();
				} catch (IOException e1) {
					Activator.logError("Failed loading image for export", e1); //$NON-NLS-1$
				} finally {
					IOUtils.closeQuietly(is);
				}
			}
		});

		final MenuItem exportItem=new MenuItem(menu, SWT.NONE);
		exportItem.setText("Export..."); //$NON-NLS-1$
		exportItem.setImage(EToolsImage.ICONS_EXPORT_16.getImage());
		exportItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog=new FileDialog(tree.getShell(), SWT.SAVE);
				InputEntry elt=(InputEntry) viewer.getStructuredSelection().getFirstElement();
				dialog.setFileName(FilenameUtils.getName(elt.url.getPath()));
				String filepath = dialog.open();
				if(filepath!=null) {
					File file=new File(filepath);
					InputStream is=null;
					try {
						is=elt.url.openStream();
						FileUtils.copyInputStreamToFile(is, file);
						for(IFile f: ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(file.toURI())) {
							try {
								f.refreshLocal(IFile.DEPTH_ZERO, new NullProgressMonitor());
							} catch (CoreException e1) {
								Activator.logWarning("Failed refreshing file "+f, e1); //$NON-NLS-1$
							}
						}
					} catch (IOException e1) {
						Activator.logError("Failed loading image for export", e1); //$NON-NLS-1$
					} finally {
						IOUtils.closeQuietly(is);
					}
				}
			}
		});
		menu.addListener(SWT.Show, new Listener() {
			public void handleEvent(Event event) {
				boolean enabled = viewer.getStructuredSelection().size()==1 && ((InputEntry)viewer.getStructuredSelection().getFirstElement()).bundle!=null;
				copyItem.setEnabled(enabled);
				exportItem.setEnabled(enabled);
			}
		});
		tree.setMenu(menu);
		
		updateInput();
		getViewSite().getActionBars().getStatusLineManager().setMessage(input.size() + " images loaded");
	}
	
	@Override
	public void dispose() {
		super.dispose();
		clipboard.dispose();
	}

	private void updateInput() {
		for(InputEntry entry: input) {
			if(entry.image!=null && entry.image.isPresent())
				entry.image.get().dispose();
		}
		input.clear();
		String search=filterText.getText();
		for(Entry<Bundle, URL> entry: all) {
			if(FilenameUtils.getBaseName(entry.getValue().getPath()).contains(search)) {
				input.add(new InputEntry(entry.getKey(), entry.getValue()));
				if(input.size()>=LIMIT) {
					input.add(new InputEntry(all.size()-LIMIT));
					break;
				}				
			}
		}
		
		viewer.setInput(input);

	}

	@Override
	public void setFocus() {
		tree.setFocus();
	}
}
