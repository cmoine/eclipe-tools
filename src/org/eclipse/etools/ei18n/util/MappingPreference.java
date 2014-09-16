package org.eclipse.etools.ei18n.util;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

import org.eclipse.etools.Activator;
import com.google.common.collect.Lists;

public class MappingPreference {
    private final IEclipsePreferences prefs;
    private final String keyFile;
    //    private final String keyExt;
    private final IFile propertyFile;

    public MappingPreference(IFile propertyFile) {
        this.propertyFile=propertyFile;
        prefs=new ProjectScope(propertyFile.getProject()).getNode(Activator.PLUGIN_ID);
        keyFile=propertyFile.toString();
        //        keyExt='B' + propertyFile.toString();
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

    //    public JavaMappingExtensionManager.JavaMappingExtension getExtension() {
    //        String value=prefs.get(keyExt, null);
    //        if (value != null) {
    //            return JavaMappingExtensionManager.getInstance().find(value);
    //        }
    //        return null;
    //    }

    public void set(IFile file) throws BackingStoreException {
        prefs.put(keyFile, file.getFullPath().makeRelativeTo(file.getProject().getFullPath()).toString());
        prefs.flush();
    }

    //    public void set(JavaMappingExtensionManager.JavaMappingExtension ext) throws BackingStoreException {
    //        prefs.put(keyExt, ext.getId());
    //        prefs.flush();
    //    }

    public static List<MappingPreference> list(IProject project) {
        IEclipsePreferences prefs=new ProjectScope(project).getNode(Activator.PLUGIN_ID);
        List<MappingPreference> result=Lists.newArrayList();
        try {
            for (String key : prefs.keys()) {
                //                if (key.startsWith("A")) { //$NON-NLS-1$
                //                String path=key.substring(1);
                IResource member=project.findMember(key);
                if (member != null && member.exists()) {
                    result.add(new MappingPreference((IFile) member));
                }
                //                }
            }
        } catch (BackingStoreException e) {
            Activator.log(IStatus.ERROR, "Failed to read project preferences", e);
        }
        return Collections.unmodifiableList(result);
    }
}
