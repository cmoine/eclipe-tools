package org.eclipse.etools.ei18n.extensions;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.etools.Activator;

import com.google.common.collect.Lists;

public class JavaMappingExtensionManager {
    private static final String EXTENSION_ID="com.cmoine.ei18n.javaMapping"; //$NON-NLS-1$

    private static JavaMappingExtensionManager instance;

    public static class JavaMappingExtension {
        private static final String ATTR_ID="id"; //$NON-NLS-1$
        private static final String ATTR_NAME="name"; //$NON-NLS-1$
        private static final String ATTR_CLASS="class"; //$NON-NLS-1$

        private final String id;
        private final String name;
        private final IJavaMapping javaMapping;

        public JavaMappingExtension(IConfigurationElement member) throws CoreException {
            id=member.getAttribute(ATTR_ID);
            name=member.getAttribute(ATTR_NAME);
            javaMapping=(IJavaMapping) member.createExecutableExtension(ATTR_CLASS);
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public IJavaMapping getJavaMapping() {
            return javaMapping;
        }
    }

    private final List<JavaMappingExtension> extensions;

    /**
     * default constructor
     */
    private JavaMappingExtensionManager() {
        extensions=Collections.unmodifiableList(loadExtension());
    }

    /**
     * Singleton
     * 
     * @return unique instance
     */
    public static synchronized JavaMappingExtensionManager getInstance() {
        if (instance == null) {
            instance=new JavaMappingExtensionManager();
        }
        return instance;
    }

    /**
     * getApplications
     * 
     * @return la liste des applications installees sur le poste
     */
    public List<JavaMappingExtension> getAll() {
        return extensions;
    }

    public JavaMappingExtension find(String id) {
        for (JavaMappingExtension ext : getAll()) {
            if (ext.getId().equals(id)) {
                return ext;
            }
        }
        return null;
    }

    /**
     * Load the extension point describing the MLS applications
     */
    private static List<JavaMappingExtension> loadExtension() {
        List<JavaMappingExtension> applications=Lists.newArrayList();
        IExtensionRegistry registry=Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint=registry.getExtensionPoint(EXTENSION_ID);

        if (extensionPoint == null) {
            Activator.logInfo("Aucune application definie", null); //$NON-NLS-1$
        } else {
            IConfigurationElement[] elements=extensionPoint.getConfigurationElements();

            for (IConfigurationElement member : elements) {
                try {
                    applications.add(new JavaMappingExtension(member));
                } catch (CoreException e) {
                    Activator.logError("Failed loading extension", e); //$NON-NLS-1$
                }
            }
        }
        return applications;
    }
}
