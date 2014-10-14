package org.eclipse.etools.eshell;

import static java.text.MessageFormat.format;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.etools.Activator;
import org.eclipse.etools.eshell.preferences.EShellPreferencesConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class EShellCommand extends AbstractHandler implements EShellPreferencesConstants {
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IResource item=getResource(event);
        if (item == null) {
            return null;
        }

        try {
            IPreferenceStore store=Activator.getDefault().getPreferenceStore();
            File file=getSystemExplorerPath(item);

            if (file.isFile())
                file=file.getParentFile();

            String cmd=format(store.getString(OPEN_COMMAND), file.getAbsolutePath());
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            Activator.logError("Failed opening command line", e); //$NON-NLS-1$
        }

        return null;
    }

    private IResource getResource(ExecutionEvent event) {
        IResource resource=getSelectionResource(event);
        if (resource == null) {
            resource=getEditorInputResource(event);
        }
        return resource;
    }

    private IResource getSelectionResource(ExecutionEvent event) {
        ISelection selection=HandlerUtil.getCurrentSelection(event);
        if (selection == null || selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
            return null;
        }

        Object selectedObject=((IStructuredSelection) selection).getFirstElement();
        IResource item=(IResource) org.eclipse.ui.internal.util.Util.getAdapter(selectedObject, IResource.class);
        return item;
    }

    private IResource getEditorInputResource(ExecutionEvent event) {
        IWorkbenchPart activePart=HandlerUtil.getActivePart(event);
        if (!(activePart instanceof IEditorPart)) {
            return null;
        }
        IEditorInput input=((IEditorPart) activePart).getEditorInput();
        if (input instanceof IFileEditorInput) {
            return ((IFileEditorInput) input).getFile();
        }
        return (IResource) input.getAdapter(IResource.class);
    }

    private File getSystemExplorerPath(IResource resource) throws IOException {
        IPath location=resource.getLocation();
        if (location == null)
            return null;
        return location.toFile();
    }

}
