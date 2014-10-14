package org.eclipse.etools.ei18n.markers;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.etools.Activator;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

public class MarkerResolutionGenerator implements IMarkerResolutionGenerator {
	public class QuickFix implements IMarkerResolution {
		private final String locale;
		private final IResource iResource;

		public QuickFix(IResource iResource, String locale) {
			this.iResource=iResource;
			this.locale=locale;
		}


		public String getLabel() {
			return "Create '" + getRequiredFile() + "'"; //$NON-NLS-1$ //$NON-NLS-2$
		}

		private String getRequiredFile() {
			String result=iResource.getFullPath().toString();
			return StringUtils.substringBeforeLast(result, ".") + "_" + locale + "." + StringUtils.substringAfterLast(result, "."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}


		public void run(IMarker marker) {
			String requiredFile=getRequiredFile();
			try {
				IFile file=iResource.getProject().getWorkspace().getRoot().getFile(new Path(requiredFile));
				file.create(new NullInputStream(0), true, new NullProgressMonitor());
			} catch (CoreException e) {
                Activator.logError("Failed to create " + requiredFile, e); //$NON-NLS-1$
			}
		}
	}


	public IMarkerResolution[] getResolutions(IMarker marker) {
		try {
			String locale=(String) marker.getAttribute(IMarkerConstants.LOCALE_ATT);
			if (locale != null) {
				return new IMarkerResolution[] { new QuickFix(marker.getResource(), locale), };
			}
			return new IMarkerResolution[0];
		} catch (CoreException e) {
			return new IMarkerResolution[0];
		}
	}

}
