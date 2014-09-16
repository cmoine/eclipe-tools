package org.eclipse.etools.efavorites.actions;

import static org.eclipse.core.runtime.IStatus.ERROR;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Stack;

import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.etools.Activator;
import org.eclipse.etools.efavorites.EFavoritesImage;
import org.eclipse.etools.efavorites.model.EFavorite;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class EFavoritesAction implements IWorkbenchWindowPulldownDelegate {
    //	private static final String SEPARATOR="|"; //$NON-NLS-1$
    public static final String FAVORITES="favorites"; //$NON-NLS-1$

	private IWorkbenchWindow window;

	private final IPartListener listener=new IPartListener() {
		public void partOpened(IWorkbenchPart part) {
		}

		public void partDeactivated(IWorkbenchPart part) {
		}

		public void partClosed(IWorkbenchPart part) {
			if (action != null) {
				updateAction();
			}
		}

		public void partBroughtToTop(IWorkbenchPart part) {
		}

		public void partActivated(IWorkbenchPart part) {
			if (action != null) {
				updateAction();
			}
		}
	};
	private IAction action;
    public static final IDialogSettings SETTINGS=Activator.getDefault().getDialogSettings();
    private static final ImageLoader IMAGE_LOADER=new ImageLoader();

	private IFile file;

	private IEditorPart editor;
    public static final String PREFS_FAVORITES="preferences"; //$NON-NLS-1$

	public void run(IAction action) {
		if (file != null) {
			int index=searchFavoriteIndex();
			if (index != -1) {
				// Remove favorite
                SETTINGS.put(FAVORITES, ArrayUtils.remove(getFavorites(), index));
			} else {
				// Add favorite
                SETTINGS.put(FAVORITES, ArrayUtils.add(getFavorites(), file2str(true)));
			}
			updateAction();
		}
	}

	private int searchFavoriteIndex() {
		int i=0;
		String file=file2str(false);
		for (String str : getFavorites()) {
            if (str.contains(EFavorite.SEPARATOR))
                str=StringUtils.substringBefore(str, EFavorite.SEPARATOR);
			if (str.equals(file)) {
				return i;
			}
			i++;
		}
		return -1;
	}

	public void selectionChanged(IAction action, ISelection selection) {
		this.action=action;
	}

	public void dispose() {
		window.getActivePage().removePartListener(listener);
	}

	public void init(IWorkbenchWindow window) {
		this.window=window;
		window.getActivePage().addPartListener(listener);
	}

	public Menu getMenu(Control parent) {
		Menu menu=new Menu(parent);

        // Editor favorites
		String[] newFavorites=getFavorites();
		for (final String file : getFavorites()) {
            final EFavorite iFile=new EFavorite(file);
            if (iFile.getFile() != null) {
				MenuItem item=new MenuItem(menu, SWT.PUSH);
                item.setData(iFile);
                item.setText(iFile.getName());
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
                        ((EFavorite) event.data).open();
					}
				});
                if (iFile.getImage() != null)
                    item.setImage(iFile.getImage());
                item.addDisposeListener(new DisposeListener() {
                    public void widgetDisposed(DisposeEvent e) {
                        ((EFavorite) e.data).dispose();
                    }
                });
			} else {
				newFavorites=ArrayUtils.remove(newFavorites, ArrayUtils.indexOf(newFavorites, file));
			}
		}
        SETTINGS.put(FAVORITES, newFavorites);

        if (getPrefsFavorites().length > 0 && menu.getItemCount() > 0)
            new MenuItem(menu, SWT.SEPARATOR);

        // Preference favorites
        for (final String prefId : getPrefsFavorites()) {
            MenuItem item=new MenuItem(menu, SWT.PUSH);
            Stack<IPreferenceNode> nodes=new Stack<IPreferenceNode>();
            nodes.addAll(Arrays.asList(PlatformUI.getWorkbench().getPreferenceManager().getRootSubNodes()));
            while (!nodes.isEmpty()) {
                IPreferenceNode node=nodes.pop();
                if (prefId.equals(node.getId())) {
                    item.setText(node.getLabelText());
                    break;
                }
                nodes.addAll(Arrays.asList(node.getSubNodes()));
            }
            item.setImage(getOptionsImage());
            item.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getModalDialogShellProvider().getShell(), prefId, null, null).open();
                }
            });
        }
		return menu;
	}

    private Image getOptionsImage() {
        return EFavoritesImage.PREFERENCES_16.getImage();
    }

    public String file2str(boolean includeImage) {
		String icon=null;
		if (includeImage) {
			try {
                IMAGE_LOADER.data=new ImageData[] { editor.getTitleImage().getImageData() };
				ByteArrayOutputStream stream=new ByteArrayOutputStream();
                IMAGE_LOADER.save(new Base64OutputStream(stream), SWT.IMAGE_GIF);
				icon=new String(stream.toByteArray());
			} catch (Throwable t) {
				Activator.log(ERROR, "Failed to save image ", t); //$NON-NLS-1$
			}
		}
        return file.getFullPath().toString() + (icon == null ? StringUtils.EMPTY : EFavorite.SEPARATOR + icon);
	}

    // public static EFavorite str2file(String file) {
    // ImageData imageData=null;
    // if (file.contains(SEPARATOR)) {
    // byte[] data=StringUtils.substringAfter(file, SEPARATOR).getBytes();
    // file=StringUtils.substringBefore(file, SEPARATOR);
    // try {
    // imageData=IMAGE_LOADER.load(new Base64InputStream(new ByteArrayInputStream(data)))[0];
    // } catch (Throwable t) {
    //				Activator.log(ERROR, "Failed to load image " + data.length + ":" + StringUtils.join(data), t); //$NON-NLS-1$ //$NON-NLS-2$
    // }
    // }
    // return new EFavorite(, imageData);
    // }

	private String[] getFavorites() {
        String[] favorites=SETTINGS.getArray(FAVORITES);
		return favorites == null ? ArrayUtils.EMPTY_STRING_ARRAY : favorites;
	}

    private String[] getPrefsFavorites() {
        String[] favorites=SETTINGS.getArray(PREFS_FAVORITES);
        return favorites == null ? ArrayUtils.EMPTY_STRING_ARRAY : favorites;
    }

	public void updateAction() {
		editor=window.getActivePage().getActiveEditor();
		file=getFile(editor);
		// action.setEnabled(file != null);
		if (file != null && searchFavoriteIndex() != -1) {
            action.setImageDescriptor(EFavoritesImage.ENABLED_STAR_16.getImageDescriptor());
		} else {
            action.setImageDescriptor(EFavoritesImage.DISABLED_STAR_16.getImageDescriptor());
		}
	}

	private IFile getFile(IEditorPart editor) {
		if (editor != null && editor.getEditorInput() != null && editor.getEditorInput().getAdapter(IFile.class) != null)
			return (IFile) editor.getEditorInput().getAdapter(IFile.class);

		return null;
	}
}
