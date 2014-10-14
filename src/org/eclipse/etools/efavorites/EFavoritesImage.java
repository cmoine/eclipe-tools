package org.eclipse.etools.efavorites;
import org.eclipse.etools.Activator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

/**
 * @author Christophe Moine
 */
public enum EFavoritesImage {
    TRASH_16("/icons/efavorites/trash.png"), //$NON-NLS-1$
    STATIC_IMPORT_16("/icons/efavorites/static_import.gif"), //$NON-NLS-1$
    ENABLED_STAR_16("/icons/efavorites/enabled-star.png"), //$NON-NLS-1$
    DISABLED_STAR_16("/icons/efavorites/disabled-star.png"), //$NON-NLS-1$
    PREFERENCES_16("/icons/efavorites/preferences.png"); //$NON-NLS-1$

	private static final ImageRegistry IMAGE_REGISTRY=Activator.getDefault().getImageRegistry();

	private final String path;

	private EFavoritesImage(String path) {
		this.path=path;
	}

	public Image getImage() {
		return IMAGE_REGISTRY.get(path);
	}

	public ImageDescriptor getImageDescriptor() {
		return IMAGE_REGISTRY.getDescriptor(path);
	}
}