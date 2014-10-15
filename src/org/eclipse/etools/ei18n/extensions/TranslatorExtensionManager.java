package org.eclipse.etools.ei18n.extensions;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.etools.Activator;
import org.eclipse.etools.ei18n.services.ITranslatorService;

import com.google.common.collect.Lists;

public class TranslatorExtensionManager {
    private static final String EXTENSION_ID="org.eclipse.etools.ei18n.translator"; //$NON-NLS-1$

    /**
     * ApplicationExtension unique id
     */
    private static final String ATTR_ID="id"; //$NON-NLS-1$

    private static final String ATTR_NAME="name"; //$NON-NLS-1$
    private static final String ATTR_DESCRIPTION="description"; //$NON-NLS-1$

    private static final String ATTR_CLASS="class"; //$NON-NLS-1$

    private static TranslatorExtensionManager instance;

    private final List<TranslatorExtension> impex;

    /**
     * default constructor
     */
    private TranslatorExtensionManager() {
        this.impex=Collections.unmodifiableList(loadExtension());
    }

    /**
     * Singleton
     * 
     * @return unique instance
     */
    public static synchronized TranslatorExtensionManager getInstance() {
        if (instance == null) {
            instance=new TranslatorExtensionManager();
        }
        return instance;
    }

    /**
     * getApplications
     * 
     * @return la liste des applications installees sur le poste
     */
    public List<TranslatorExtension> getApplications() {
        return impex;
    }

    /**
     * Load the extension point describing the MLS applications
     */
    private static List<TranslatorExtension> loadExtension() {
        List<TranslatorExtension> applications=Lists.newArrayList();
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
                    String desc=member.getAttribute(ATTR_DESCRIPTION);
                    ITranslatorService impexApplication=(ITranslatorService) member.createExecutableExtension(ATTR_CLASS);

                    applications.add(new TranslatorExtension(id, name, desc, impexApplication));
                } catch (CoreException e) {
                    Activator.logError("Failed loading extension", e); //$NON-NLS-1$
                }
            }
        }
        return applications;
    }
}
