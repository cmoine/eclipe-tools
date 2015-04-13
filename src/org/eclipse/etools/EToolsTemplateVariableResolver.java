package org.eclipse.etools;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

public class EToolsTemplateVariableResolver extends TemplateVariableResolver {
    @Override
    protected String resolve(TemplateContext context) {
        if (context instanceof DocumentTemplateContext) {
            DocumentTemplateContext docContext=(DocumentTemplateContext) context;
            try {
                int line=docContext.getDocument().getLineOfOffset(docContext.getCompletionOffset());
                return Integer.toString(line + 1);
            } catch (BadLocationException e) {
                Activator.logError("Failed to resolve line number", e); //$NON-NLS-1$
            }
        }
        return super.resolve(context);
    }
}
