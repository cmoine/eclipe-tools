package org.eclipse.etools.xsdgen;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.etools.Activator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.wiztools.xsdgen.XsdGenMain;

public class XsdGenHandler extends AbstractHandler {
    public Object execute(ExecutionEvent event) throws ExecutionException {
        for (Object o : ((IStructuredSelection) HandlerUtil.getCurrentSelectionChecked(event)).toList()) {
            if (o instanceof IAdaptable) {
                IFile file=(IFile) ((IAdaptable) o).getAdapter(IFile.class);
                try {
                    File javaFile=new File(file.getRawLocationURI());
                    IFile xsdFile=file.getParent().getFile(new Path(file.getName() + ".xsd")); //$NON-NLS-1$
                    PrintStream oldOut=System.out;
                    ByteArrayOutputStream baos=new ByteArrayOutputStream();
                    System.setOut(new PrintStream(baos));
                    XsdGenMain.main(new String[] { javaFile.getPath() });
                    System.setOut(oldOut);
                    xsdFile.create(new ByteArrayInputStream(baos.toByteArray()), true, new NullProgressMonitor());
                } catch (Exception e) {
                    Activator.log(IStatus.ERROR, "Failed generating XSD", e); //$NON-NLS-1$
                }
            }
        }
        return null;
    }
}
