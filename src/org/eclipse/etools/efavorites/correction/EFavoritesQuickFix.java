package org.eclipse.etools.efavorites.correction;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.etools.Activator;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.eclipse.jdt.internal.ui.text.correction.SimilarElement;
import org.eclipse.jdt.internal.ui.text.correction.SimilarElementsRequestor;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;

public class EFavoritesQuickFix implements IQuickAssistProcessor {

    public boolean hasAssists(IInvocationContext context) throws CoreException {

        return true;
    }

    public IJavaCompletionProposal[] getAssists(IInvocationContext context, IProblemLocation[] locations) throws CoreException {
        //        if (hasAssists(context)) {
        //            EFavoritesProposal proposal=new EFavoritesProposal(context);
        //            return new IJavaCompletionProposal[] { proposal };
        //        }
        try {
            if (context.getCoveringNode() instanceof SimpleName) {
                List<IJavaCompletionProposal> proposals=new ArrayList<IJavaCompletionProposal>();

                SimpleName node=(SimpleName) context.getCoveringNode();

                IFile file=(IFile) context.getCompilationUnit().getCorrespondingResource();
                IJavaProject project=JavaCore.create(file.getProject());

                int kind=ASTResolving.getPossibleTypeKinds(node, JavaModelUtil.is50OrHigher(project));

                //                IMarker[] markers=file.findMarkers("org.eclipse.jdt.core.problem", true, IResource.DEPTH_ZERO);
                for (IProblemLocation location : locations) {
                    //                    int charStart=(Integer) marker.getAttribute(IMarker.CHAR_START);
                    //                    int charEnd=(Integer) marker.getAttribute(IMarker.CHAR_END);
                    if (context.getSelectionOffset() >= location.getOffset() && context.getSelectionOffset() < location.getOffset() + location.getLength()) {
                        // Marker !!!
                        if (location.getProblemId() == IProblem.UndefinedType || location.getProblemId() == IProblem.UndefinedName) {
                            //                            String type=((String) marker.getAttribute(IJavaModelMarker.ARGUMENTS)).substring(2);
                            String type=location.getProblemArguments()[0];

                            context.getCoveringNode();
                            SimilarElement[] elements=SimilarElementsRequestor.findSimilarElement(context.getCompilationUnit(), node, kind);
                            for (int i=0; i < elements.length; i++) {
                                SimilarElement elem=elements[i];
                                if ((elem.getKind() & SimilarElementsRequestor.ALL_TYPES) != 0) {
                                    String fullName=elem.getName();
                                    String simpleName=StringUtils.substringAfterLast(fullName, ".");
                                    if (simpleName.equals(node.getIdentifier())) {
                                        //                                        System.out.println("EFavoritesQuickFix.hasAssists() " + fullName);
                                        proposals.add(new EFavoritesProposal(fullName));
                                    }
                                }
                            }
                        }
                    }
                }

                if (proposals.size() > 1)
                    return proposals.toArray(new IJavaCompletionProposal[proposals.size()]);
            }
        } catch (Exception e) {
            Activator.log(IStatus.ERROR, "Failed to find EFavorites completion", e);
        }

        return null;
    }
}
