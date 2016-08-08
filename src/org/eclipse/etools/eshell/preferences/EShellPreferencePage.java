package org.eclipse.etools.eshell.preferences;

import org.eclipse.etools.Activator;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class EShellPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage, EShellPreferencesConstants {
    public static final String ID=EShellPreferencePage.class.getName();

    public EShellPreferencePage() {
        super(GRID);
    }

    public void init(IWorkbench workbench) {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    @Override
    protected void createFieldEditors() {
        addField(new StringFieldEditor(OPEN_COMMAND, "Open command line:", getFieldEditorParent()));
    }
}
