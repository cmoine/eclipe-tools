package org.eclipse.etools.efavorites.model;

import java.io.ByteArrayInputStream;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.etools.Activator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

public class EFavorite {
    public static final String SEPARATOR="|"; //$NON-NLS-1$

    private final IFile file;
    private Image image;

    public EFavorite(String str) {
        if (str.contains(SEPARATOR)) {
            byte[] data=StringUtils.substringAfter(str, SEPARATOR).getBytes();
            str=StringUtils.substringBefore(str, SEPARATOR);
            try {
                image=new Image(Display.getCurrent(), new ImageLoader().load(new Base64InputStream(new ByteArrayInputStream(data)))[0]);
            } catch (Throwable t) {
                Activator.logError("Failed to load image " + data.length + ":" + StringUtils.join(data), t); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        file=(IFile) ResourcesPlugin.getWorkspace().getRoot().findMember(str);
	}

    public IFile getFile() {
        return file;
    }

    public Image getImage() {
        return image;
    }

    public String getName() {
        return file.getFullPath().toString();
    }

    public void dispose() {
        if (image != null)
            image.dispose();
    }

    public void open() {
        try {
            IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
        } catch (PartInitException e) {
            Activator.logError("Failed to open editor", e); //$NON-NLS-1$
        }
    }
}
