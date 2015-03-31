package org.eclipse.etools;

import org.eclipse.etools.ei18n.preferences.EI18NPreferenceConstants;
import org.eclipse.etools.ei18n.preferences.EI18NPreferencePage;
import org.eclipse.etools.eshell.preferences.EShellPreferencePage;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferenceLinkArea;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

public class EtoolsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, EI18NPreferenceConstants {
    public EtoolsPreferencePage() {
    }

	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

    @Override
    protected Control createContents(Composite parent) {
        Composite composite=new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        new PreferenceLinkArea(composite, SWT.NONE, EI18NPreferencePage.ID, "See <a>''{0}''</a> for translated messages",//$NON-NLS-1$
                (IWorkbenchPreferenceContainer) getContainer(), null);

        new PreferenceLinkArea(composite, SWT.NONE, EShellPreferencePage.ID, "See <a>''{0}''</a> for shell commands",//$NON-NLS-1$
                (IWorkbenchPreferenceContainer) getContainer(), null);

        return composite;
    }
}
