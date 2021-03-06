package org.eclipse.etools.codeanalysis;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.etools.Activator;
import org.eclipse.etools.search.EI18NTextSearchResult;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.search.internal.ui.text.FileMatch;
import org.eclipse.search.internal.ui.text.LineElement;

public class CodeAnalysis {

    public static void calculate(ICompilationUnit cu, EI18NTextSearchResult result) {
        try {
            IFile file=(IFile) cu.getCorrespondingResource();
            for (IType type : cu.getTypes()) {
                for (IMethod method : type.getMethods()) {
                    if (method.isMainMethod())
                        continue;

                    if (method.isConstructor() && !Flags.isPublic(method.getFlags()))
                        continue;

                    performMethodSearch(file, result, method);
                }
                for (IField field : type.getFields()) {
                    performFieldSearch(file, result, field);
                }
            }
        } catch (CoreException e) {
            Activator.logError("Failed calculating method usage", e); //$NON-NLS-1$
        }
    }

    private static void performFieldSearch(IFile file, EI18NTextSearchResult result, IField field) throws CoreException {
        SearchPattern pattern=SearchPattern.createPattern(field, IJavaSearchConstants.REFERENCES);
        IJavaSearchScope scope=SearchEngine.createWorkspaceScope();

        MySearchRequestor requestor=new MySearchRequestor();
        SearchEngine searchEngine=new SearchEngine();
        searchEngine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, scope, requestor, null);

        if (requestor.getNumberOfCalls() == 0) {
            result.addMatch(new FileMatch(file, field.getSourceRange().getOffset(), field.getSourceRange().getLength(), new LineElement(file, -1, 0,
                    "Unused field " + field.getElementName())));
        }
    }

    private static void performMethodSearch(IFile file, EI18NTextSearchResult result, IMethod method) throws CoreException {
        SearchPattern pattern=SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES);
        IJavaSearchScope scope=SearchEngine.createWorkspaceScope();

        MySearchRequestor requestor=new MySearchRequestor();
        SearchEngine searchEngine=new SearchEngine();
        searchEngine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, scope, requestor, null);

        if (requestor.getNumberOfCalls() == 0) {
            result.addMatch(new FileMatch(file, method.getSourceRange().getOffset(), method.getSourceRange().getLength(), new LineElement(file, -1, 0,
                    "Unused method " + method.getElementName())));
        }
    }
}
