package org.eclipse.etools.codeanalysis;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.etools.Activator;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.FieldReferenceMatch;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.MethodReferenceMatch;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.ui.search.NewSearchResultCollector;

public class CodeAnalysis {

    private static final EtoolsSearchResult searchResult=new EtoolsSearchResult();
    private static final NewSearchResultCollector collector=new NewSearchResultCollector(searchResult, false);

    public static void calculate(List<ICompilationUnit> cus, IProgressMonitor monitor) {
        try {
            searchResult.removeAll();

            for (ICompilationUnit unit : cus) {
                for (IType type : unit.getTypes()) {
                    for (IMethod method : type.getMethods()) {
                        if (!method.isMainMethod()) {
                            performMethodSearch(method);
                        }
                    }
                    for (IField field : type.getFields()) {
                        performFieldSearch(field);
                    }
                }
                monitor.worked(1);
            }
        } catch (CoreException e) {
            Activator.logError("Failed calculating method usage", e); //$NON-NLS-1$
        }
    }

    private static void performFieldSearch(IField field) throws CoreException {
        SearchPattern pattern=SearchPattern.createPattern(field, IJavaSearchConstants.REFERENCES);
        IJavaSearchScope scope=SearchEngine.createWorkspaceScope();

        MySearchRequestor requestor=new MySearchRequestor();
        SearchEngine searchEngine=new SearchEngine();
        searchEngine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, scope, requestor, null);

        if (requestor.getNumberOfCalls() == 0) {
            FieldReferenceMatch match=new FieldReferenceMatch(field, SearchMatch.A_ACCURATE, -1, -1, true, true, false,
                    SearchEngine.getDefaultSearchParticipant(), field.getCompilationUnit().getCorrespondingResource());
            collector.acceptSearchMatch(match);
        }
    }

    private static void performMethodSearch(IMethod method) throws CoreException {
        SearchPattern pattern=SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES);
        IJavaSearchScope scope=SearchEngine.createWorkspaceScope();

        MySearchRequestor requestor=new MySearchRequestor();
        SearchEngine searchEngine=new SearchEngine();
        searchEngine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, scope, requestor, null);

        if (requestor.getNumberOfCalls() == 0) {
            MethodReferenceMatch match=new MethodReferenceMatch(method, SearchMatch.A_ACCURATE, -1, -1, false, false, false, false,
                    SearchEngine.getDefaultSearchParticipant(), method.getCompilationUnit().getCorrespondingResource());
            collector.acceptSearchMatch(match);
        }
    }

    public static EtoolsSearchResult getSearchResult() {
        return searchResult;
    }
}
