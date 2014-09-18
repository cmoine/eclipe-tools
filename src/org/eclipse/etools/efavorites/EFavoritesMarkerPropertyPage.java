package org.eclipse.etools.efavorites;

import java.lang.reflect.Field;
import java.util.HashMap;
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

public class EFavoritesMarkerPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {
    private static final Map<Integer, String> IDS=new HashMap<Integer, String>();

    static {
        for (Field field : IProblem.class.getDeclaredFields()) {
            try {
                IDS.put((Integer) field.get(null), field.getName());
                //                if (((Integer) ).intValue() == value) {
                //                    return field.getName();
                //                }
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            }
        }
    }

    @Override
    protected Control createContents(Composite parent) {
        IMarker marker=(IMarker) getElement().getAdapter(IMarker.class);
        FormText text=new FormText(parent, SWT.NONE);
        String str;
        try {
            int problemId=((Integer) marker.getAttribute(IJavaModelMarker.ID)).intValue();
            int problemIdWithoutCategory=problemId & IProblem.IgnoreCategoriesMask;
            str="Problem id: " + problemId + "</p><p>Problem id without category: " + problemIdWithoutCategory + "</p><p>Constant: IProblem."
                    + IDS.get(problemId);
        } catch (CoreException e) {
            str=e.getMessage();
        }
        text.setText("<html><p>" + str + "</p></html>", true, false);
        return text;
    }
}
