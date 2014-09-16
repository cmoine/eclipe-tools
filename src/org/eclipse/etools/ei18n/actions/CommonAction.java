package org.eclipse.etools.ei18n.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.google.common.collect.Iterables;

public class CommonAction extends Action {
	private Shell shell;
    private final IProject project;

    protected CommonAction() {
        super();
        Iterable<?> iterator=((IStructuredSelection) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection()).toList();
        project=Iterables.get(Iterables.filter(iterator, IProject.class), 0);
	}

	@Override
	public void runWithEvent(Event event) {
        shell=((Button) event.widget).getShell();
		run();
	}

    protected IProject getProject() {
        return project;
    }

	public Shell getShell() {
		return shell;
	}
}
