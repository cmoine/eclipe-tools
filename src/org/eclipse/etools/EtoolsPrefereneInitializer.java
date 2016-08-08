package org.eclipse.etools;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

public class EtoolsPrefereneInitializer extends AbstractPreferenceInitializer {
	@Override
	public void initializeDefaultPreferences() {
		Activator.getDefault().getPreferenceStore().setDefault(EtoolsPreferenceConstants.LOGGER_TEMPLATE, EtoolsPreferenceConstants.DEFAULT_LOGGER);
	}
}
