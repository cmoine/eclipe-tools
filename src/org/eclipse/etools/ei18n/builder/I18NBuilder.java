package org.eclipse.etools.ei18n.builder;

import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.etools.RemoveMe;
import org.eclipse.etools.ei18n.markers.IMarkerConstants;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

@RemoveMe
public class I18NBuilder extends IncrementalProjectBuilder {
	public static final String BUILDER_ID="com.cmoine.ei18n.I18NBuilder"; //$NON-NLS-1$

	private final Set<String> locales=Sets.newHashSet();
	private static final Multimap<String, IFile> MESSAGES=HashMultimap.create();

	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		locales.clear();
		// messages.clear();
        // TODO CME
        //		getProject().accept(counterVisitor, IResource.DEPTH_INFINITE, false);
        //		getProject().accept(visitor);
		return null;
	}

	private void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(IMarkerConstants.MARKER_TYPE, false, IResource.DEPTH_ONE);
		} catch (CoreException ce) {
		}
	}
}
