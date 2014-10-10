package org.eclipse.etools.codeanalysis;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.search.FieldReferenceMatch;
import org.eclipse.jdt.core.search.MethodReferenceMatch;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchRequestor;

public class MySearchRequestor extends SearchRequestor {
    private int numberOfCalls = 0;

	@Override
	public void acceptSearchMatch(SearchMatch match) throws CoreException {
        if (match instanceof MethodReferenceMatch || match instanceof FieldReferenceMatch) {
		    numberOfCalls++;
		}
	}

    public int getNumberOfCalls() {
        return numberOfCalls;
    }
}
