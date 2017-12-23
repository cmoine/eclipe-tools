package org.eclipse.etools.eshell.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.etools.Activator;
import org.eclipse.jface.preference.IPreferenceStore;

public class EShellPreferenceInitializer extends AbstractPreferenceInitializer implements EShellPreferencesConstants {
    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store=Activator.getDefault().getPreferenceStore();
        store.setDefault(OPEN_COMMAND, "cmd.exe /C start \"Etools: {0}\" /D \"{0}\" cmd.exe /K"); //$NON-NLS-1$
    }
}
