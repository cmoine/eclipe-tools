package org.eclipse.etools.ei18n.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public final class PreferencesUtil {
    private PreferencesUtil() {
    }

    public static String getLineDelimiter() {
        return getLineDelimiter(null);
    }

    public static String getLineDelimiter(IFile file) {
        String value=null;
        if (file != null) {
            value=getStoredValue(getPreferences(file.getProject()));
        }
        if (value == null) {
            value=getStoredValue(Platform.getPreferencesService().getRootNode().node(DefaultScope.SCOPE));
        }
        return value != null ? value : System.getProperty(Platform.PREF_LINE_SEPARATOR);
    }

    private static String getStoredValue(Preferences node) {
        try {
            // be careful looking up for our node so not to create any nodes as side effect
            if (node.nodeExists(Platform.PI_RUNTIME))
                return node.node(Platform.PI_RUNTIME).get(Platform.PREF_LINE_SEPARATOR, null);
        } catch (BackingStoreException e) {
            // ignore
        }
        return null;
    }

    private static Preferences getPreferences(IProject project) {
        if (project != null) {
            return Platform.getPreferencesService().getRootNode().node(ProjectScope.SCOPE).node(project.getName());
        }

        return Platform.getPreferencesService().getRootNode().node(InstanceScope.SCOPE);
    }
}
