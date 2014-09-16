package org.eclipse.etools.ei18n.extensions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.operation.IRunnableWithProgress;

public interface IImpex {
    public IRunnableWithProgress getExportOperation(Iterable<IFile> iterable, String destinationValue);

    public String getFileExtension();
}
