package org.eclipse.etools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;

public class SelectionUtils {
    private SelectionUtils() {
    }

    private static List<File> getFiles(ISelection rawSelection) {
        List<File> files=new ArrayList<File>();
        if (rawSelection instanceof TextSelection) {
            IEditorPart activeEditor=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
            if (activeEditor != null) {
                IResource res=(IResource) activeEditor.getEditorInput().getAdapter(IResource.class);
                if (res != null)
                    files.add(res.getLocation().toFile());
            }
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
    
    public static List<ICompilationUnit> getCUs(List<IResource> resources) {
//    	List<IResource> resources=SelectionUtils.getResources(HandlerUtil.getActiveMenuSelection(event));

        final List<ICompilationUnit> cus=new ArrayList<ICompilationUnit>();
        for (IResource res : resources) {
            try {
                if (JavaCore.create(res.getProject()) != null) {
                    res.accept(new IResourceVisitor() {
                        public boolean visit(IResource resource) throws CoreException {
                            if (resource instanceof IFile) {
                                IJavaElement javaElement=JavaCore.create((IFile) resource);
                                if (javaElement instanceof ICompilationUnit) {
                                    cus.add((ICompilationUnit) javaElement);
                                }
                            }
                            return true;
                        }
                    }, IResource.DEPTH_INFINITE, IResource.NONE);
                }
            } catch (CoreException e) {
                Activator.logError("Failed detecting dead code in " + res, e); //$NON-NLS-1$
            }
		}
        return cus;
    }
}
