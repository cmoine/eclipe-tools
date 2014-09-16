package org.eclipse.etools.ei18n;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import org.eclipse.jdt.internal.corext.template.java.CodeTemplateContextType;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.ui.IStartup;

public class EI18NStartup implements IStartup {
    public static final String EI18N_CLASS_CONTEXT="ei18nClass_context"; //$NON-NLS-1$
    public static final String EI18N_CLASS_ID="org.eclipse.jdt.ui.text.codetemplates.ei18nClass"; //$NON-NLS-1$

    public static final String EI18N_FIELD_CONTEXT="ei18nField_context"; //$NON-NLS-1$
    public static final String EI18N_FIELD_ID="org.eclipse.jdt.ui.text.codetemplates.ei18nField"; //$NON-NLS-1$

    public void earlyStartup() {
        JavaPlugin.getDefault().getCodeTemplateContextRegistry().addContextType(new CodeTemplateContextType(EI18N_CLASS_CONTEXT) {
            {
                //                addResolver(new CodeTemplateVariableResolver(PACKAGE_DECLARATION, EMPTY));
                addResolver(new CodeTemplateVariableResolver(PACKAGENAME, EMPTY));
                addResolver(new CodeTemplateVariableResolver(TYPENAME, EMPTY));
            }
        });
        JavaPlugin.getDefault().getCodeTemplateContextRegistry().addContextType(new CodeTemplateContextType(EI18N_FIELD_CONTEXT) {
            {
                //                addResolver(new CodeTemplateVariableResolver(PACKAGE_DECLARATION, EMPTY));
                addResolver(new CodeTemplateVariableResolver(FIELD, EMPTY));
            }
        });
    }
}
