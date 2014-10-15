package org.eclipse.etools.ei18n.wizards;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME="org.eclipse.etools.ei18n.wizards.messages"; //$NON-NLS-1$
	
	public static String ExportWizardei18nTitle;
	public static String DestinationEmptyMessage;
	public static String selectAllMessagesProperties;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
