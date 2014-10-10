package org.eclipse.etools.eshell;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class EShellCommand extends AbstractHandler {
    private final IAction ACTION=new Action() {
    };

    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchPart activePart=HandlerUtil.getActivePart(event);
        EShellAction action=EShellPropertyTester.hasResourceSelection(activePart);
        if (action != null) {
            ACTION.setId(event.getCommand().getId());
            action.run(ACTION);
        }
        return null;
    }

}
