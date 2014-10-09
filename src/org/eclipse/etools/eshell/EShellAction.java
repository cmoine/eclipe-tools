package org.eclipse.etools.eshell;

import static java.text.MessageFormat.format;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.etools.Activator;
import org.eclipse.etools.eshell.preferences.EShellPreferencesConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class EShellAction implements IObjectActionDelegate, EShellPreferencesConstants {

    private IStructuredSelection currentSelection;
    private File[] files;

    public void run(IAction action) {
        IPreferenceStore store=Activator.getDefault().getPreferenceStore();
        for (File file : files) {
            Object[] strs=new Object[] { file.getParent(), file.getPath() };
            String cmd=null;
            if (action.getId().endsWith("shellOpen")) { //$NON-NLS-1$
                cmd=format(store.getString(OPEN_COMMAND), strs);
            } else if (action.getId().endsWith("shellExplore")) { //$NON-NLS-1$
                cmd=format(store.getString(EXPLORE_COMMAND), strs);
            }
            try {
                Runtime.getRuntime().exec(cmd);
            } catch (IOException e) {
                Activator.log(IStatus.ERROR, "Failed running", e); //$NON-NLS-1$
            }
        }
    }

    public void selectionChanged(IAction action, ISelection selection) {
        this.currentSelection=selection instanceof IStructuredSelection ? (IStructuredSelection) selection : null;
    }

    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        // TODO Auto-generated method stub

    }

    public boolean isEnabled() {
        boolean enabled=false;
        if (this.currentSelection != null) {
            Object[] selectedObjects=this.currentSelection.toArray();
            if (selectedObjects.length >= 1) {
                //                this.resource=new Resource[selectedObjects.length];
                files=new File[selectedObjects.length];
                for (int i=0; i < selectedObjects.length; i++) {
                    files[i]=ResourceUtils.getResource(selectedObjects[i]);
                    if (files[i] != null)
                        enabled=true;
                }
            }
        }
        return enabled;
    }
}
