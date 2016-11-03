package org.eclipse.etools.util;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;

public class FileUtils {
    public static ISelection getResourceSelection(IWorkbenchPart part) {
        ISelection selection=null;
        if (part != null)
            if (part instanceof IEditorPart) {
                File file=getResource(part);
                if (file != null)
                    selection=new StructuredSelection(file);
            } else {
                try {
                    selection=part.getSite().getSelectionProvider().getSelection();
                } catch (Exception localException) {
                }
            }
        return selection;
    }

    public static File getResource(Object myObj) {
        Object object=null;

        if (myObj instanceof IEditorPart) {
            IEditorPart editorPart=(IEditorPart) myObj;
            IEditorInput input=editorPart.getEditorInput();
            Object adapter=input.getAdapter(IFile.class);
            if (adapter instanceof IFile) {
                object=adapter;
            } else {
                adapter=editorPart.getAdapter(IFile.class);
                if (adapter instanceof IFile)
                    object=adapter;
                else
                    object=input;
            }
        } else {
            object=myObj;
        }

        if (object instanceof File) {
            return (File) object;
        }

        //        String projectName=null;
        if (object instanceof IFile) {
            //            projectName=((IFile) object).getProject().getName();
            return ((IFile) object).getLocation().toFile();
        }
        if (object instanceof File) {
            return (File) object;
        }
        if (object instanceof IAdaptable) {
            IAdaptable adaptable=(IAdaptable) object;
            IFile ifile=(IFile) adaptable.getAdapter(IFile.class);
            if (ifile != null) {
                //                projectName=ifile.getProject().getName();
                return ifile.getLocation().toFile();
            }
            IResource ires=(IResource) adaptable.getAdapter(IResource.class);
            if (ires != null) {
                //                projectName=ires.getProject().getName();

                IPath path=ires.getLocation();
                if (path != null) {
                    return path.toFile();
                }

            }

            File file=(File) adaptable.getAdapter(File.class);
            if (file != null) {
                return file;
            }
        }
        return null;
    }
}