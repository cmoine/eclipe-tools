package org.eclipse.etools;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.etools"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	private static class MyImageRegistry extends ImageRegistry {
        private final ImageRegistry delegate;
        private final Bundle bundle;

        public MyImageRegistry(Bundle bundle, ImageRegistry delegate) {
            this.bundle=bundle;
            this.delegate=delegate;
        }

        @Override
        public void dispose() {
            delegate.dispose();
        }

        @Override
        public boolean equals(Object obj) {
            return delegate.equals(obj);
        }

        @Override
        public Image get(String key) {
            Image result=delegate.get(key);
            if (result == null) {
                ImageDescriptor descriptor=ImageDescriptor.createFromURL(Activator.getURL(bundle, key));
                put(key, descriptor);
                result=delegate.get(key);
            }
            return result;
        }

        @Override
        public ImageDescriptor getDescriptor(String key) {
            ImageDescriptor result=delegate.getDescriptor(key);
            if (result == null) {
                ImageDescriptor descriptor=ImageDescriptor.createFromURL(Activator.getURL(bundle, key));
                put(key, descriptor);
                result=delegate.getDescriptor(key);
            }
            return result;
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public void put(String key, Image image) {
            delegate.put(key, image);
        }

        @Override
        public void put(String key, ImageDescriptor descriptor) {
            delegate.put(key, descriptor);
        }

        @Override
        public void remove(String key) {
            delegate.remove(key);
        }

        @Override
        public String toString() {
            return delegate.toString();
        }
    }
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
    public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
    public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

    public IDialogSettings getOrCreateDialogSettings(Class<?> clazz) {
        String sectionName=clazz.getName();
        IDialogSettings section=getDialogSettings().getSection(sectionName);
        if (section == null)
            section=getDialogSettings().addNewSection(sectionName);

        return section;
    }

    public static void logInfo(String message, Throwable e) {
        log(IStatus.INFO, message, e);
    }

    public static void logWarning(String message, Throwable e) {
        log(IStatus.WARNING, message, e);
    }

    public static void logError(String message, Throwable e) {
        log(IStatus.ERROR, message, e);
    }

    private static void log(int severity, String message, Throwable e) {
        getDefault().getLog().log(new Status(severity, PLUGIN_ID, message, e));
    }

    @Override
    protected ImageRegistry createImageRegistry() {
        ImageRegistry result=super.createImageRegistry();
        return new MyImageRegistry(getBundle(), result);
    }

    public static URL getURL(String path) {
        if (getDefault() == null)
            return null;

        return getURL(getDefault().getBundle(), path);
    }

    public static URL getURL(Bundle bundle, String path) {
        if (bundle == null || !isReady(bundle.getState()))
            return null;

        // look for the image (this will check both the plugin and fragment
        // folders
        URL fullPathString=FileLocator.find(bundle, new Path(path), null);
        if (fullPathString == null) {
            try {
                fullPathString=new URL(path);
            } catch (MalformedURLException e) {
                return null;
            }
        }

        return fullPathString;
    }

    private static boolean isReady(int bundleState) {
        return (bundleState & (Bundle.RESOLVED | Bundle.STARTING | Bundle.ACTIVE | Bundle.STOPPING)) != 0;
    }
}
