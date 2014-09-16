package org.eclipse.etools.ei18n.markers;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.views.markers.MarkerFieldFilter;
import org.eclipse.ui.views.markers.MarkerItem;
import org.eclipse.ui.views.markers.MarkerSupportConstants;

import org.eclipse.etools.Activator;

public class EI18NMarkerFieldFilter extends MarkerFieldFilter {
	private String markerType;
	private String contains;

	@Override
	public boolean select(MarkerItem item) {
		try {
			return matchesType(item) && matchesContains(item);
		} catch (CoreException e) {
            Activator.log(IStatus.ERROR, "Failed to get marker type", e); //$NON-NLS-1$
			return false;
		}
	}

	private boolean matchesContains(MarkerItem item) {
		return contains==null ? true : item.getAttributeValue(IMarker.MESSAGE, StringUtils.EMPTY).startsWith(contains);
	}

	private boolean matchesType(MarkerItem item) throws CoreException {
		return markerType==null ? true : item.getMarker().getType().equals(markerType);
	}
	
	@Override
	public void initialize(Map values) {
		markerType = (String) values.get(IMarkerConstants.SOURCE_TYPE_FILTER);
		contains = (String) values.get(MarkerSupportConstants.CONTAINS_KEY);
	}

	@Override
	public void saveSettings(IMemento memento) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadSettings(IMemento memento) {
		// TODO Auto-generated method stub

	}
}
