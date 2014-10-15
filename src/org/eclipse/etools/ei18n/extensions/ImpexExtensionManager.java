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

public class ImpexExtensionManager {
    private static final String EXTENSION_ID="org.eclipse.etools.ei18n.impex"; //$NON-NLS-1$

    /**
     * ApplicationExtension unique id
     */
    private static final String ATTR_ID="id"; //$NON-NLS-1$

    private static final String ATTR_NAME="name"; //$NON-NLS-1$
    private static final String ATTR_EXTENSION="extension"; //$NON-NLS-1$

    private static final String ATTR_CLASS="class"; //$NON-NLS-1$

    private static ImpexExtensionManager instance;

    private final List<ImpexExtension> impex;

    /**
     * default constructor
     */
    private ImpexExtensionManager() {
        List<ImpexExtension> applications=loadExtension();
        this.impex=Collections.unmodifiableList(applications);
    }

    /**
     * Singleton
     * 
     * @return unique instance
     */
    public static synchronized ImpexExtensionManager getInstance() {
        if (instance == null) {
            instance=new ImpexExtensionManager();
        }
        return instance;
    }

    /**
     * getApplications
     * 
     * @return la liste des applications installees sur le poste
     */
    public List<ImpexExtension> getApplications() {
        return impex;
    }

    /**
     * Load the extension point describing the MLS applications
     */
    private static List<ImpexExtension> loadExtension() {
        List<ImpexExtension> applications=Lists.newArrayList();
        IExtensionRegistry registry=Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint=registry.getExtensionPoint(EXTENSION_ID);

        if (extensionPoint == null) {
            Activator.logInfo("Aucune application definie", null); //$NON-NLS-1$
        } else {
            IConfigurationElement[] elements=extensionPoint.getConfigurationElements();

            for (IConfigurationElement member : elements) {
                try {
                    String id=member.getAttribute(ATTR_ID);
                    String name=member.getAttribute(ATTR_NAME);
                    String fileExt=member.getAttribute(ATTR_EXTENSION);
                    IImpex impexApplication=(IImpex) member.createExecutableExtension(ATTR_CLASS);

                    ImpexExtension application=new ImpexExtension(id, name, fileExt, impexApplication);
                    applications.add(application);
                } catch (CoreException e) {
                    Activator.logError("Failed loading extension", e); //$NON-NLS-1$
                }
            }
        }
        return applications;
    }
}
