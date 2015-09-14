package org.eclipse.etools.codeanalysis;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.etools.SelectionUtils;
import org.eclipse.etools.search.EI18NTextSearchResult;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class FindDeadCodeHandler extends AbstractHandler {
	public Object execute(final ExecutionEvent event) throws ExecutionException {
        List<IResource> resources=SelectionUtils.getResources(HandlerUtil.getActiveMenuSelection(event));

        final List<ICompilationUnit> cus=SelectionUtils.getCUs(resources);
//        for (IResource res : resources) {
//            try {
//                if (JavaCore.create(res.getProject()) != null) {
//                    res.accept(new IResourceVisitor() {
//                        public boolean visit(IResource resource) throws CoreException {
//                            if (resource instanceof IFile) {
//                                IJavaElement javaElement=JavaCore.create((IFile) resource);
//                                if (javaElement instanceof ICompilationUnit) {
//                                    cus.add((ICompilationUnit) javaElement);
//                                }
//                            }
//                            return true;
//                        }
//                    }, IResource.DEPTH_INFINITE, IResource.NONE);
//                }
//            } catch (CoreException e) {
//                Activator.logError("Failed detecting dead code in " + res, e); //$NON-NLS-1$
//            }
//		}

        if (cus.isEmpty()) {
            MessageDialog.openInformation(HandlerUtil.getActiveShell(event), "Information", "Please select at least one compilation unit!");
        } else {
            NewSearchUI.runQueryInBackground(new ISearchQuery() {
                private final EI18NTextSearchResult searchResult=new EI18NTextSearchResult(this);

                public IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
                    monitor.beginTask("Searching Dead Code...", cus.size());
                    for (ICompilationUnit cu : cus) {
                        CodeAnalysis.calculate(cu, searchResult);
                        monitor.worked(1);
                    }
                    monitor.done();
                    return Status.OK_STATUS;
                }

                public String getLabel() {
                    return "Find Dead Code";
                }

                public boolean canRerun() {
                    return true;
                }

                public boolean canRunInBackground() {
                    return true;
                }

                public ISearchResult getSearchResult() {
                    return searchResult;
                }

            });
        }

		return null;
	}
}
