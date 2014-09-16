package org.eclipse.etools.ei18n.markers;

import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.views.markers.FiltersContributionParameters;
import org.eclipse.ui.views.markers.MarkerSupportConstants;

import com.google.common.collect.Maps;

public class UnecessaryNonNlsTagFiltersContributionParameters extends FiltersContributionParameters {
    private static Map<Object, Object> map;

    static {
        map=Maps.newHashMap();
        map.put(IMarker.SOURCE_ID, IMarker.PROBLEM); 
        map.put(MarkerSupportConstants.CONTAINS_KEY, "Unnecessary $NON-NLS$ tag"); //$NON-NLS-1$
    }

    @Override
    public Map<?, ?> getParameterValues() {
        return map;
    }
}
