package org.eclipse.etools.ei18n;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;

public class EI18NQuickAssistProcessor implements IQuickAssistProcessor {
    public boolean hasAssists(IInvocationContext context) throws CoreException {
        ASTNode node=context.getCoveringNode();
        return node instanceof StringLiteral;
    }

    public IJavaCompletionProposal[] getAssists(IInvocationContext context, IProblemLocation[] locations) throws CoreException {
        if (hasAssists(context)) {
            EI18NCompletionProposal proposal=new EI18NCompletionProposal(context);
            return new IJavaCompletionProposal[] { proposal };
        }
        return null;
    }
}
