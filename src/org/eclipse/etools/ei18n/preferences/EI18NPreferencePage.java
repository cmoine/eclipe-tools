package org.eclipse.etools.ei18n.preferences;

import org.eclipse.etools.Activator;
import org.eclipse.etools.ei18n.util.Escapers;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class EI18NPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
    public static final String ID=EI18NPreferencePage.class.getName();

    public EI18NPreferencePage() {
        super(GRID);
    }

    public void init(IWorkbench workbench) {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    @Override
    protected void createFieldEditors() {
        Escapers[] encodings=Escapers.values();
        String[][] entryNamesAndValues=new String[encodings.length][];
        for (int i=0; i < entryNamesAndValues.length; i++) {
            entryNamesAndValues[i]=new String[] { encodings[i].name(), Integer.toString(encodings[i].ordinal()) };
        }
        addField(new ComboFieldEditor(EI18NPreferenceConstants.EI18N_ENCODING, "Default encoding :", entryNamesAndValues, getFieldEditorParent()));
        Group group=new Group(getFieldEditorParent(), SWT.NONE);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
        group.setText("BING Translator credentials");
        addField(new StringFieldEditor(EI18NPreferenceConstants.BING_CLIENT_ID, "Client ID :", group));
        addField(new StringFieldEditor(EI18NPreferenceConstants.BING_SECRET_KEY, "Secret Key :", group));
    }
}
