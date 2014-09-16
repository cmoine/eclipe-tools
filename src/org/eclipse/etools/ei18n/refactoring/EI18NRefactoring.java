package org.eclipse.etools.ei18n.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalModel;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class EI18NRefactoring extends Refactoring {
    private LinkedProposalModel linkedProposalModel;
    private final String label;

    public EI18NRefactoring(IInvocationContext context, String label) {
        this.label=label;
        setValidationContext(context);
    }

    @Override
    public String getName() {
        return label;
    }

    @Override
    public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        RefactoringStatus refactoringStatus=new RefactoringStatus();
        if (!(getContext().getCoveringNode() instanceof StringLiteral)) {
            refactoringStatus.addError("Not a String Label"); //$NON-NLS-1$
        }
        return refactoringStatus;
    }

    private AssistContext getContext() {
        return (AssistContext) getValidationContext();
    }

    @Override
    public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        return new RefactoringStatus();
    }

    @Override
    public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        // TODO Auto-generated method stub
        return null;
    }

    public void setLinkedProposalModel(LinkedProposalModel linkedProposalModel) {
        this.linkedProposalModel=linkedProposalModel;
    }
}
