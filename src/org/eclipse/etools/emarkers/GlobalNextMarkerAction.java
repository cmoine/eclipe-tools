package org.eclipse.etools.emarkers;

import org.eclipse.ui.texteditor.AnnotationPreference;

public class GlobalNextMarkerAction extends GlobalMarkerAction {

    @Override
    protected void next() {
        count++;
    }

    @Override
    protected String getKey(AnnotationPreference pref) {
        return pref.getIsGoToNextNavigationTargetKey();
    }
}
