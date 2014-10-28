package org.eclipse.etools.emarkers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.etools.Activator;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.views.markers.ExtendedMarkersView;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.views.markers.internal.MarkerType;
import org.eclipse.ui.views.markers.internal.MarkerTypesModel;

abstract class GlobalMarkerAction extends Action implements IMenuCreator, IWorkbenchWindowPulldownDelegate2, IHandler {
    protected static int count=0;
    private int lastSize=10;

    private static final String[] MARKER_TYPES=new String[] { "org.eclipse.core.resources.bookmark", "org.eclipse.jdt.core.problem" }; //$NON-NLS-1$ //$NON-NLS-2$

    private static final String PREFIX="emarkers."; //$NON-NLS-1$

    private static final IPreferenceStore STORE=Activator.getDefault().getPreferenceStore();

    public void init(IWorkbenchWindow window) {
    }

    public void run(IAction action) {
        List<IMarker> markers=new ArrayList<IMarker>(lastSize);
        for (String type : MARKER_TYPES) {
            if (STORE.getBoolean(PREFIX + type)) {
                try {
                    for (IMarker marker : ResourcesPlugin.getWorkspace().getRoot().findMarkers(type, true, IResource.DEPTH_INFINITE)) {
                        markers.add(marker);
                    }
                } catch (CoreException e) {
                    Activator.logError("Failed to get markers of type " + type, e); //$NON-NLS-1$
                }
            }
        }

        if (markers.size() > 0) {
            next();

            if (count >= markers.size())
                count=0;
            if (count < 0)
                count=markers.size() - 1;

            ExtendedMarkersView.openMarkerInEditor(markers.get(count), PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage());
            return;
        }
        lastSize=markers.size();
    }

    protected abstract void next();

    protected abstract String getKey(AnnotationPreference pref);

    public Menu getMenu(Control parent) {
        Menu menu=new Menu(parent);
        MarkerTypesModel typesModel=MarkerTypesModel.getInstance();
        for (final String markerType : MARKER_TYPES) {
            MarkerType type=typesModel.getType(markerType);
            final MenuItem item=new MenuItem(menu, SWT.CHECK);
            item.setSelection(STORE.getBoolean(PREFIX + markerType));
            item.setText(type.getLabel());
            item.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    STORE.setValue(PREFIX + markerType, item.getSelection());
                }
            });
        }
        return menu;
    }

    public Menu getMenu(Menu parent) {
        // TODO Auto-generated method stub
        return null;
    }

    public void dispose() {
        // TODO Auto-generated method stub

    }

    public void selectionChanged(IAction action, ISelection selection) {
        // TODO Auto-generated method stub

    }

    public void addHandlerListener(IHandlerListener handlerListener) {
        // TODO Auto-generated method stub

    }

    public void removeHandlerListener(IHandlerListener handlerListener) {
        // TODO Auto-generated method stub

    }

    public Object execute(ExecutionEvent event) throws ExecutionException {
        run(null);
        return null;
    }
}
