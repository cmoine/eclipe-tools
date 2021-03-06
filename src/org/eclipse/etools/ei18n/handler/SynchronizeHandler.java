package org.eclipse.etools.ei18n.handler;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IResource;
import org.eclipse.etools.Activator;
import org.eclipse.etools.ei18n.synchronize.EI18NSynchronizeParticipant;
import org.eclipse.etools.ei18n.util.EI18NFile;
import org.eclipse.etools.util.SelectionUtils;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
import org.eclipse.team.ui.synchronize.ISynchronizeScope;
import org.eclipse.team.ui.synchronize.ResourceScope;
import org.eclipse.team.ui.synchronize.SubscriberParticipant;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class SynchronizeHandler extends AbstractHandler implements IHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection sel=HandlerUtil.getActiveMenuSelectionChecked(event);
        File file=EI18NFile.promptFile(HandlerUtil.getActiveShell(event));
        if (file != null) {
            try {
                List<IResource> resources=SelectionUtils.getResources(sel);

                // First check if there is an existing matching participant
                IResource[] resourcesArray=resources.toArray(new IResource[] {});
                EI18NSynchronizeParticipant participant=(EI18NSynchronizeParticipant) SubscriberParticipant.getMatchingParticipant(
                        EI18NSynchronizeParticipant.ID, resourcesArray);
                // If there isn't, create one and add to the manager
                if (participant == null) {
                    ISynchronizeScope scope;
                    scope=new ResourceScope(resourcesArray);
                    participant=new EI18NSynchronizeParticipant(scope, file);
                    TeamUI.getSynchronizeManager().addSynchronizeParticipants(new ISynchronizeParticipant[] { participant });
                }
                participant.refresh(resourcesArray, PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart().getSite());
            } catch (ZipException e) {
                Activator.logError("Failed to synchronize", e); //$NON-NLS-1$
            } catch (IOException e) {
                Activator.logError("Failed to synchronize", e); //$NON-NLS-1$
            }
        }
        return null;
    }

}
