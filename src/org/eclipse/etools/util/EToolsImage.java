package org.eclipse.etools.util;
import org.eclipse.etools.Activator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

/**
 * @author Christophe Moine
 */
public enum EToolsImage {
    ICONS_EXPORT_16("/icons/icons/export.png"); //$NON-NLS-1$

	private static final ImageRegistry IMAGE_REGISTRY=Activator.getDefault().getImageRegistry();

	private final String path;

	private EToolsImage(String path) {
		this.path=path;
	}

	public Image getImage() {
		return IMAGE_REGISTRY.get(path);
	}

	public ImageDescriptor getImageDescriptor() {
		return IMAGE_REGISTRY.getDescriptor(path);
	}
}