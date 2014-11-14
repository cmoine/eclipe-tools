package org.eclipse.etools;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.views.markers.internal.MarkerType;
import org.eclipse.ui.views.markers.internal.MarkerTypesModel;

import com.google.common.base.Joiner;

public class EFavoritesMarkerPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {
    private static final Map<Integer, String> IDS=new HashMap<Integer, String>();

    static {
        for (Field field : IProblem.class.getDeclaredFields()) {
            try {
                IDS.put((Integer) field.get(null), field.getName());
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            }
        }
    }

    @Override
    protected Control createContents(Composite parent) {
        IMarker marker=(IMarker) getElement().getAdapter(IMarker.class);
        FormText text=new FormText(parent, SWT.NONE);
        List<String> strs=new ArrayList<String>();
        try {
            MarkerTypesModel typesModel=MarkerTypesModel.getInstance();
            MarkerType type=typesModel.getType(marker.getType());
            strs.add("Marker type: " + marker.getType());

            strs.add("Supertypes: " + toString(type.getAllSupertypes()));
            strs.add("Subtypes: " + toString(type.getAllSubTypes()));

            Integer problemId=(Integer) marker.getAttribute(IJavaModelMarker.ID);
            if (problemId != null) {
                strs.add("Problem id: " + problemId);
                int problemIdWithoutCategory=problemId & IProblem.IgnoreCategoriesMask;
                strs.add("Problem id without category: " + problemIdWithoutCategory);
                strs.add("Constant: IProblem." + IDS.get(problemId));
            }
            //            str="Marker type: " + marker.getType() + "</p><p>Supertypes: " + toString(type.getAllSupertypes()) + "</p><p>Subtypes: "
            //                    + toString(type.getAllSubTypes()) + "</p><p>Problem id: " + problemId + "</p><p>Problem id without category: "
            //                    + problemIdWithoutCategory + "</p><p>Constant: IProblem." + IDS.get(problemId);
        } catch (CoreException e) {
            strs.add(e.getMessage());
        }
        text.setText("<html><p>" + Joiner.on("</p><p>").join(strs) + "</p></html>", true, false);
        return text;
    }

    private String toString(MarkerType[] types) {
        String[] strs=new String[types.length];
        int i=0;
        for (MarkerType subType : types) {
            strs[i++]=subType.getId();
        }
        return Joiner.on(',').join(strs);
    }
}
