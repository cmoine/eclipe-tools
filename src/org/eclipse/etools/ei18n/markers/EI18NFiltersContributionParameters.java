package org.eclipse.etools.ei18n.markers;

import java.util.Map;

import org.eclipse.ui.views.markers.FiltersContributionParameters;

import com.google.common.collect.Maps;

public class EI18NFiltersContributionParameters extends FiltersContributionParameters {
	private static Map<Object, Object> map;

    static {
        map=Maps.newHashMap();
        map.put(IMarkerConstants.SOURCE_TYPE_FILTER, IMarkerConstants.MARKER_TYPE); 
    }

	@Override
	public Map<?, ?> getParameterValues() {
		return map;
	}
}
