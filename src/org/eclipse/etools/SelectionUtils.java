package org.eclipse.etools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkingSet;

public class SelectionUtils {
    private SelectionUtils() {
    }

    public static List<File> getFiles(ISelection rawSelection) {
        List<File> files=new ArrayList<File>();
        if (rawSelection instanceof TextSelection) {
            IResource res=null;
            if (res != null)
                files.add(res.getLocation().toFile());
        } else {
            //            selection=(IStructuredSelection) rawSelection;
            for (Object o : ((IStructuredSelection) rawSelection).toArray()) {
                if (o instanceof IWorkingSet) {
                    IWorkingSet ws=(IWorkingSet) o;
                    for (IAdaptable adaptable : ws.getElements()) {
                        File file=FileUtils.getResource(adaptable);
                        if (file != null)
                            files.add(file);
                    }
                } else {
                    File file=FileUtils.getResource(o);
                    if (file != null)
                        files.add(file);
                }
            }
        }
        return files;
    }

    public static List<IResource> getResources(ISelection rawSelection) {
        List<File> files=getFiles(rawSelection);
        List<IResource> result=new ArrayList<IResource>(files.size());
        for (File file : files) {
            IWorkspaceRoot root=ResourcesPlugin.getWorkspace().getRoot();
            boolean found=false;
            for (IFile iFile : root.findFilesForLocationURI(file.toURI())) {
                if (iFile.exists()) {
                    result.add(iFile);
                    found=true;
                    break;
                }
            }
            if (!found) {
                for (IContainer container : root.findContainersForLocationURI(file.toURI())) {
                    if (container.exists()) {
                        result.add(container);
                        break;
                    }
                }
            }
        }
        return result;
    }
}
