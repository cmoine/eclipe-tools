package org.eclipse.etools.ei18n.synchronize;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.ZipException;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.etools.Activator;
import org.eclipse.etools.RemoveMe;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ISynchronizeScope;
import org.eclipse.team.ui.synchronize.ModelSynchronizeParticipantActionGroup;
import org.eclipse.team.ui.synchronize.SubscriberParticipant;
import org.eclipse.team.ui.synchronize.SynchronizePageActionGroup;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;

@RemoveMe
public class EI18NSynchronizeParticipant extends SubscriberParticipant /* ScopableSubscriberParticipant implements ISynchronizeParticipant */{
	public static final String ID=EI18NSynchronizeParticipant.class.getName();

	public EI18NSynchronizeParticipant(ISynchronizeScope scope, File file) throws ZipException, IOException {
		super(scope);
        Subscriber subscriber=null;
        if (FilenameUtils.isExtension(file.getName(), Arrays.asList("zip", "ei18n"))) { //$NON-NLS-1$ //$NON-NLS-2$
            subscriber=new ZIPSubscriber(scope.getRoots(), file);
        } else if (FilenameUtils.isExtension(file.getName(), "xls")) { //$NON-NLS-1$
            subscriber=new XLSSubscriber(scope.getRoots(), file);
        }
        if (subscriber == null) {
            MessageDialog.openError(PlatformUI.getWorkbench().getModalDialogShellProvider().getShell(), "Error",
                    "Invalid file extension: " + FilenameUtils.getExtension(file.getName()));
            return;
        }

        setSubscriber(subscriber);
		try {
			setInitializationData(TeamUI.getSynchronizeManager().getParticipantDescriptor(ID));
			setSecondaryId(Long.toString(System.currentTimeMillis()));
		} catch (CoreException e) {
            Activator.logError("Failed to synchronize", e); //$NON-NLS-1$
		}
	}

	@Override
	protected void initializeConfiguration(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration configuration) {
		super.initializeConfiguration(configuration);
		configuration.addMenuGroup(ISynchronizePageConfiguration.P_TOOLBAR_MENU, ModelSynchronizeParticipantActionGroup.MERGE_ACTION_GROUP);
		configuration.addActionContribution(createMergeActionGroup());
	}

	protected SynchronizePageActionGroup createMergeActionGroup() {
		return new EI18NSynchronizeParticipantActionGroup();
	}

	public void refresh(IResource[] resources, IWorkbenchPartSite site) {
		refresh(resources, getShortTaskName(), getLongTaskName(resources), site);
	}
}
