package org.eclipse.etools.ei18n.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.etools.Activator;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;

public class EI18NUtil {
    private EI18NUtil() {
    }

    public static IFile getFile(IJavaElement element) {
        IResource res=null;
        try {
            while ((res=element.getCorrespondingResource()) == null)
                element=element.getParent();
            return (IFile) res;
        } catch (JavaModelException e) {
            Activator.log(IStatus.ERROR, "Failed finding corresponding resource", e); //$NON-NLS-1$
            return null;
        }
    }
}
