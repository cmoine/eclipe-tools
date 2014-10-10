package org.eclipse.etools;

import org.eclipse.etools.ei18n.preferences.EI18NPreferenceConstants;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class EtoolsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, EI18NPreferenceConstants {
    public EtoolsPreferencePage() {
    }

	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

    @Override
    protected Control createContents(Composite parent) {
        return new Composite(parent, SWT.NONE);
    }
}
