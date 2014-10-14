package org.eclipse.etools.eshell;

import static java.text.MessageFormat.format;

import java.io.File;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.etools.Activator;
import org.eclipse.etools.RemoveMe;
import org.eclipse.etools.eshell.preferences.EShellPreferencesConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

@RemoveMe
public class EShellAction implements IObjectActionDelegate, EShellPreferencesConstants {

    private IStructuredSelection currentSelection;
    private File file;
    private ExecutionEvent event;

    public void run(IAction action) {
        IPreferenceStore store=Activator.getDefault().getPreferenceStore();
        //        for (File file : files) {
        try {
            Object[] strs=new Object[] { file.getParent(), file.getPath() };
            String cmd=null;
            if (action.getId().endsWith("shellOpen")) { //$NON-NLS-1$
                cmd=format(store.getString(OPEN_COMMAND), strs);
                Runtime.getRuntime().exec(cmd);
                //            } else if (action.getId().endsWith("shellExplore")) { //$NON-NLS-1$
                //                cmd=format(store.getString(EXPLORE_COMMAND), strs);
                //                //                ExecutionEvent event=new ExecutionEvent();
                //                //                HandlerUtil.
                //                //                IWorkbenchWindow window=PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                //                //                IHandlerService handlerService=(IHandlerService) window.getService(IHandlerService.class);
                //                //                ICommandService cmdService=(ICommandService) window.getService(ICommandService.class);
                //                //                BundleContext bundleContext=Activator.getDefault().getBundle().getBundleContext();
                //                //                ServiceReference<IHandlerService> serviceReference=bundleContext.getServiceReference(IHandlerService.class);
                //                //                service.createExecutionEvent(service., event)executeCommand(ShowInSystemExplorerHandler.ID, new Event());
                //                //                Command command=cmdService.getCommand(ShowInSystemExplorerHandler.ID);
                //                //                handlerService.
                //                //                handlerService.createExecutionEvent(command, new Event());
                //                //                command.executeWithChecks(event);
                //                //                service.executeCommand(command, new Event());
                //                //                new ShowInSystemExplorerHandler().execute(event)
                //                new ShowInSystemExplorerHandler().execute(event);
            }
        } catch (Exception e) {
            Activator.logError("Failed running", e); //$NON-NLS-1$
        }
        //        }
    }

    public void selectionChanged(IAction action, ISelection selection) {
        this.currentSelection=selection instanceof IStructuredSelection ? (IStructuredSelection) selection : null;
    }

    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        // TODO Auto-generated method stub

    }

    //    public boolean isEnabled() {
    //        int count=0;
    //        file=null;
    //        if (this.currentSelection != null) {
    //            //            Object[] selectedObjects=this.currentSelection.toArray();
    //            //            if (selectedObjects.length >= 1) {
    //            //                files=new File[selectedObjects.length];
    //            for (Object o : currentSelection.toArray()) {
    //                File f=FileUtils.getResource(o);
    //                if (f != null) {
    //                    file=f;
    //                    //                        enabled=true;
    //                    count++;
    //                }
    //            }
    //            //            }
    //        }
    //        return file != null && count == 1;
    //    }

    public void setEvent(ExecutionEvent event) {
        this.event=event;
    }
}
