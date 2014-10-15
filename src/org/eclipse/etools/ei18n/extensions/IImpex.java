package org.eclipse.etools.ei18n.extensions;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;

public interface IImpex {
    //    public IRunnableWithProgress getExportOperation(Iterable<IFile> iterable, String destinationValue);
    public void export(final Iterable<IFile> iterable, File dst, IProgressMonitor monitor) throws InvocationTargetException, InterruptedException;

    //    public String getFileExtension();
}
