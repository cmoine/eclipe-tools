package org.eclipse.etools.ei18n.util;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.internal.preferences.DefaultPreferences;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.etools.Activator;
import org.eclipse.etools.ei18n.preferences.EI18NPreferenceConstants;
import org.osgi.service.prefs.BackingStoreException;

import com.google.common.collect.Lists;

public class MappingPreference {
    private final IEclipsePreferences prefs;
    private final String keyFile;
    private final IStorage propertyFile;
    private final String encodingKeyFile;

    public MappingPreference(IStorage propertyFile) {
        this.propertyFile=propertyFile;
        if (isEditable())
            prefs=new ProjectScope(getPropertyFile().getProject()).getNode(Activator.PLUGIN_ID);
        else
            prefs=new DefaultPreferences();

        keyFile=propertyFile.toString();
        encodingKeyFile='E' + keyFile;
    }

    public boolean isEditable() {
        return propertyFile instanceof IFile;
    }

    public IFile getPropertyFile() {
        return (IFile) propertyFile;
    }

    public IFile getJavaFile() {
        String value=prefs.get(keyFile, null);
        if (StringUtils.isNotEmpty(value)) {
            IFile file=(IFile) getPropertyFile().getProject().findMember(value);
            if (file != null) {
                return file;
            }
        }
        return null;
    }

    public boolean isManaged() {
        String value=prefs.get(keyFile, null);
        return value != null;
    }

    public void setEncoding(Escapers enc) throws BackingStoreException {
        prefs.put(encodingKeyFile, Integer.toString(enc.ordinal()));
        prefs.flush();
    }

    public Escapers getEncoding() {
        String value=prefs.get(encodingKeyFile, null);
        if (value != null) {
            try {
                int val=Integer.parseInt(value);
                return Escapers.values()[val];
            } catch (NumberFormatException e) {
                Activator.logWarning("Wrong integer for encoding " + value, e); //$NON-NLS-1$
            }
        }
        return Escapers.values()[Activator.getDefault().getPreferenceStore().getInt(EI18NPreferenceConstants.EI18N_ENCODING)];
    }

    public void set(IFile file) throws BackingStoreException {
        prefs.put(keyFile, file.getFullPath().makeRelativeTo(file.getProject().getFullPath()).toString());
        prefs.flush();
    }

    public void manage() throws BackingStoreException {
        prefs.put(keyFile, EMPTY);
        prefs.flush();
    }

    public void unmanage() throws BackingStoreException {
        prefs.remove(keyFile);
        prefs.flush();
    }

    public static List<MappingPreference> list(IProject project) {
        IEclipsePreferences prefs=new ProjectScope(project).getNode(Activator.PLUGIN_ID);
        List<MappingPreference> result=Lists.newArrayList();
        try {
            for (String key : prefs.keys()) {
                IResource member=project.getWorkspace().getRoot().findMember(key.substring(1));
                if (member != null && member.exists()) {
                    result.add(new MappingPreference((IFile) member));
                }
            }
        } catch (BackingStoreException e) {
            Activator.logError("Failed to read project preferences", e); //$NON-NLS-1$
        }
        return Collections.unmodifiableList(result);
    }
}
