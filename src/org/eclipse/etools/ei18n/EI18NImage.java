package org.eclipse.etools.ei18n;

import java.util.Locale;
import java.util.regex.Matcher;

import org.eclipse.core.resources.IFile;
import org.eclipse.etools.Activator;
import org.eclipse.etools.ei18n.util.EI18NConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

/**
 *
 * <BR>
 *
 * @author Christophe Moine
 */
public enum EI18NImage {
    ADD_16("/icons/ei18n/add.gif"), //$NON-NLS-1$
    DELETE_16("/icons/ei18n/delete.gif"), //$NON-NLS-1$
    ERROR_16("/icons/ei18n/error.gif"), //$NON-NLS-1$
    VALIDATE_16("/icons/ei18n/validate.gif"), //$NON-NLS-1$
    LOGO_16("/icons/ei18n/localizedProperties.png"); //$NON-NLS-1$

	private static final ImageRegistry IMAGE_REGISTRY=Activator.getDefault().getImageRegistry();

	private final String path;

	private EI18NImage(String path) {
		this.path=path;
	}

	public Image getImage() {
		return IMAGE_REGISTRY.get(path);
	}

	public ImageDescriptor getImageDescriptor() {
		return IMAGE_REGISTRY.getDescriptor(path);
	}

    public static Image getImage(IFile file) {
        Matcher matcher;
        if ((matcher=EI18NConstants.LOCALE_PATTERN.matcher(file.getName())).matches()) {
            String resourcePath="/icons/ei18n/flags/" + matcher.group(1) + ".png"; //$NON-NLS-1$ //$NON-NLS-2$
            if (Activator.getDefault().getBundle().getResource(resourcePath) != null)
                return IMAGE_REGISTRY.get(resourcePath);
        }

        return LOGO_16.getImage();
    }

    public static Image getImage(Locale locale) {
        if (locale != null) {
            String resourcePath="/icons/ei18n/flags/" + locale.getLanguage() + ".png"; //$NON-NLS-1$ //$NON-NLS-2$
            if (Activator.getDefault().getBundle().getResource(resourcePath) != null)
                return IMAGE_REGISTRY.get(resourcePath);
        }

        return LOGO_16.getImage();
    }
}