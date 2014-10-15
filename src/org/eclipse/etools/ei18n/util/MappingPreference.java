package org.eclipse.etools.ei18n.util;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.etools.Activator;
import org.osgi.service.prefs.BackingStoreException;

import com.google.common.collect.Lists;

public class MappingPreference {
    private final IEclipsePreferences prefs;
    private final String keyFile;
    private final IFile propertyFile;

    public MappingPreference(IFile propertyFile) {
        this.propertyFile=propertyFile;
        prefs=new ProjectScope(propertyFile.getProject()).getNode(Activator.PLUGIN_ID);
        keyFile=propertyFile.toString();
    }

    public IFile getPropertyFile() {
        return propertyFile;
    }

    public IFile getJavaFile() {
        String value=prefs.get(keyFile, null);
        if (value != null) {
            IFile file=(IFile) propertyFile.getProject().findMember(value);
            if (file != null) {
                return file;
            }
        }
        return null;
    }

    public void set(IFile file) throws BackingStoreException {
        prefs.put(keyFile, file.getFullPath().makeRelativeTo(file.getProject().getFullPath()).toString());
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
