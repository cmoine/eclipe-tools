package org.eclipse.etools;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.internal.corext.template.java.JavaContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

public class EToolsTemplateVariableResolver extends TemplateVariableResolver {
	private static final String LOGGER_TYPE = "logger"; //$NON-NLS-1$
	private static final String LINE_NUMBER_TYPE = "line_number"; //$NON-NLS-1$

	@Override
    protected String resolve(TemplateContext context) {
    	if(LOGGER_TYPE.equals(getType())) {
    		String loggerClass = Activator.getDefault().getPreferenceStore().getString(EtoolsPreferenceConstants.LOGGER_TEMPLATE);
    		if (context instanceof JavaContext) {
    			JavaContext jc= (JavaContext) context;
    			jc.addImport(loggerClass);
    		}
			return StringUtils.substringAfterLast(loggerClass, ".");
    	} else if (LINE_NUMBER_TYPE.equals(getType()) && context instanceof DocumentTemplateContext) {
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
