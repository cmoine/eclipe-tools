package org.eclipse.etools.eshell;

import static java.text.MessageFormat.format;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.etools.Activator;
import org.eclipse.etools.eshell.preferences.EShellPreferencesConstants;
import org.eclipse.etools.util.SelectionUtils;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.handlers.HandlerUtil;

import com.google.common.collect.Iterables;

public class EShellCommand extends AbstractHandler implements EShellPreferencesConstants {
    public Object execute(ExecutionEvent event) throws ExecutionException {
    	List<IResource> resources = SelectionUtils.getResources(HandlerUtil.getShowInSelectionChecked(event));
        IResource item=Iterables.getFirst(resources, null); //getResource(event);
        if (item == null) {
        	Activator.logWarning("No resource found in the selection to open a command line window", null); //$NON-NLS-1$
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

    private File getSystemExplorerPath(IResource resource) throws IOException {
        IPath location=resource.getLocation();
        if (location == null)
            return null;
        return location.toFile();
    }
}
