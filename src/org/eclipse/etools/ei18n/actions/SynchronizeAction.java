package org.eclipse.etools.ei18n.actions;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.etools.Activator;
import org.eclipse.etools.RemoveMe;
import org.eclipse.etools.ei18n.handler.SynchronizeHandler;
import org.eclipse.etools.ei18n.synchronize.EI18NSynchronizeParticipant;
import org.eclipse.etools.ei18n.util.EI18NFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
import org.eclipse.team.ui.synchronize.ISynchronizeScope;
import org.eclipse.team.ui.synchronize.ResourceScope;
import org.eclipse.team.ui.synchronize.SubscriberParticipant;
import org.eclipse.ui.PlatformUI;

import com.google.common.collect.Lists;

/**
 * @author cmoine
 * @deprecated {@link SynchronizeHandler} 
 */
@Deprecated
@RemoveMe
public class SynchronizeAction extends CommonAction {
	public SynchronizeAction() {
        setText("Synchronize Ei18n"); //$NON-NLS-1$
	}

	@Override
	public void run() {
		File file=EI18NFile.promptFile(getShell());
		if (file != null) {
			try {
				List<IResource> resources=Lists.newArrayList();
				for (Object obj : ((IStructuredSelection) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection()).toList()) {
					if (obj instanceof IResource) {
						resources.add((IResource) obj);
					}
					if (obj instanceof IAdaptable) {
						IResource res=(IResource) ((IAdaptable) obj).getAdapter(IResource.class);
						if (res != null)
							resources.add(res);
					}
				}

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
	}
}
