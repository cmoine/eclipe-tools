package org.eclipse.etools.eshell;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.etools.RemoveMe;

@RemoveMe
public class EShellPropertyTester extends PropertyTester {
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        //        if ("hasResourceSelection".equals(property) && receiver instanceof IWorkbenchPart) {
        //            return hasResourceSelection((IWorkbenchPart) receiver) != null;
        //        }
        return false;
    }

    //    public static EShellAction hasResourceSelection(IWorkbenchPart part) {
    //        ISelection selection=FileUtils.getResourceSelection(part);
    //        if (selection != null) {
    //            EShellAction action=new EShellAction();
    //            action.selectionChanged(null, selection);
    //            if (action.isEnabled())
    //                return action;
    //        }
    //        return null;
    //    }
}
