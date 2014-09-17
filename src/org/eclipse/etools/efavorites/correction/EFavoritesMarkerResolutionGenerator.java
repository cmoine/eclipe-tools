package org.eclipse.etools.efavorites.correction;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

public class EFavoritesMarkerResolutionGenerator implements IMarkerResolutionGenerator {
    public EFavoritesMarkerResolutionGenerator() {
        System.out.println("EFavoritesMarkerResolutionGenerator.EFavoritesMarkerResolutionGenerator()");
    }

    public IMarkerResolution[] getResolutions(IMarker marker) {
        // TODO Auto-generated method stub
        return new IMarkerResolution[0];
    }
}
