package org.eclipse.etools.codeanalysis;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.etools.Activator;
import org.eclipse.etools.SelectionUtils;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.search2.internal.ui.SearchView;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

public class FindDeadCodeHandler extends AbstractHandler {
	public Object execute(final ExecutionEvent event) throws ExecutionException {
        ISelection rawSelection=HandlerUtil.getActiveMenuSelection(event);
        List<IResource> resources=SelectionUtils.getResources(rawSelection);

        final List<ICompilationUnit> cus=new ArrayList<ICompilationUnit>();
        for (IResource res : resources) {
            try {
                //                IWorkspaceRoot root=ResourcesPlugin.getWorkspace().getRoot();
                //                IResource res=null;
                //                for (IFile iFile : root.findFilesForLocationURI(file.toURI())) {
                //                    if (iFile.exists()) {
                //                        res=iFile;
                //                        break;
                //                    }
                //                }
                //                if (res == null) {
                //                    for (IContainer container : root.findContainersForLocationURI(file.toURI())) {
                //                        if (container.exists()) {
                //                            res=container;
                //                            break;
                //                        }
                //                    }
                //                }
                //
                //                if (res != null) {
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
                //                }
            } catch (CoreException e) {
                Activator.logError("Failed detecting dead code in " + res, e); //$NON-NLS-1$
            }
		}

        if (cus.isEmpty()) {
            MessageDialog.openInformation(HandlerUtil.getActiveShell(event), "Information", "Please select at least one compilation unit!");
        } else {
            Job job=new Job("Searching Dead Code...") { //$NON-NLS-1$
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    monitor.beginTask("Searching Dead Code...", cus.size()); //$NON-NLS-1$
                    CodeAnalysis.calculate(cus, monitor);
                    monitor.done();
                    // Open view in the UI thread
                    Display.getDefault().asyncExec(new Runnable() {
                        public void run() {
                            try {
                                SearchView view=(SearchView) HandlerUtil.getActiveWorkbenchWindow(event).getActivePage()
                                        .showView("org.eclipse.search.ui.views.SearchView");
                                view.showSearchResult(CodeAnalysis.getSearchResult());
                                view.queryAdded(CodeAnalysis.getSearchResult().getQuery());
                            } catch (PartInitException e) {
                                e.printStackTrace();
                            }
                        }

                    });
                    return Status.OK_STATUS;
                }
            };
            job.setUser(true);
            job.schedule();
        }

		return null;
	}
}
