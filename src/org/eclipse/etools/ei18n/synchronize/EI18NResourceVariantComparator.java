package org.eclipse.etools.ei18n.synchronize;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.etools.Activator;
import org.eclipse.etools.RemoveMe;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.IResourceVariantComparator;

@RemoveMe
public class EI18NResourceVariantComparator implements IResourceVariantComparator {

	public boolean compare(IResource local, IResourceVariant remote) {
		return true;
	}


	public boolean compare(IResourceVariant base, IResourceVariant remote) {
		try {
			return IOUtils.contentEquals(base.getStorage(new NullProgressMonitor()).getContents(), remote.getStorage(new NullProgressMonitor()).getContents());
		} catch (TeamException e) {
            Activator.logError("Failed to compare " + base + " with " + remote, e); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (IOException e) {
            Activator.logError("Failed to read " + base + " or " + remote + " for comparison", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} catch (CoreException e) {
            Activator.logError("Failed to read " + base + " or " + remote + " for comparison", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		return false;
	}


	public boolean isThreeWay() {
		return true;
	}
}
