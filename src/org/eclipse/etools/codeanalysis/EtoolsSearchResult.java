package org.eclipse.etools.codeanalysis;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.ui.search.AbstractJavaSearchResult;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;

public class EtoolsSearchResult extends AbstractJavaSearchResult implements ISearchResult {
    public String getLabel() {
        return "Find Dead Code";
    }

    public String getTooltip() {
        // TODO Auto-generated method stub
        return null;
    }

    public ImageDescriptor getImageDescriptor() {
        // TODO Auto-generated method stub
        return null;
    }

    public ISearchQuery getQuery() {
        return new ISearchQuery() {

            public IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
                return Status.OK_STATUS;
            }

            public ISearchResult getSearchResult() {
                return EtoolsSearchResult.this;
            }

            public String getLabel() {
                return EtoolsSearchResult.this.getLabel();
            }

            public boolean canRunInBackground() {
                return false;
            }

            public boolean canRerun() {
                return false;
            }
        };
    }

}
