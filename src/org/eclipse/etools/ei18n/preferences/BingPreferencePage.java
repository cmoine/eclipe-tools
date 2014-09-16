package org.eclipse.etools.ei18n.preferences;

import org.eclipse.etools.Activator;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class BingPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
    public BingPreferencePage() {
        super(GRID);
    }

    public void init(IWorkbench workbench) {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    @Override
    protected void createFieldEditors() {
        addField(new StringFieldEditor(EI18NPreferenceConstants.BING_CLIENT_ID, "Client ID :", getFieldEditorParent()));
        addField(new StringFieldEditor(EI18NPreferenceConstants.BING_SECRET_KEY, "Secret Key :", getFieldEditorParent()));
    }
}
