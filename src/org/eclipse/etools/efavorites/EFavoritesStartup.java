package org.eclipse.etools.efavorites;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.etools.Activator;
import org.eclipse.etools.efavorites.actions.EFavoritesAction;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class EFavoritesStartup implements IStartup {
    public void earlyStartup() {
        // FIXME NPE when exits
        //        configure();
    }

    protected void configure() {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                Shell shell=PlatformUI.getWorkbench().getModalDialogShellProvider().getShell();
                shell.addControlListener(new ControlListener() {

                    public void controlResized(ControlEvent e) {
                        // TODO Auto-generated method stub

                    }

                    public void controlMoved(ControlEvent e) {
                        // TODO Auto-generated method stub

                    }
                });
                final PreferenceDialog dialog=PreferencesUtil.createPreferenceDialogOn(shell, null, null, null);
                Shell shell2=dialog.getShell();
                shell2.addListener(SWT.Show, new Listener() {
                    private final IDialogSettings settings=Activator.getDefault().getDialogSettings();

                    public void handleEvent(Event event) {
                        Tree tree=dialog.getTreeViewer().getTree();
                        Menu menu=tree.getMenu();
                        if (menu == null)
                            menu=new Menu(tree);
                        else
                            new MenuItem(menu, SWT.SEPARATOR);

                        final MenuItem item=new MenuItem(menu, SWT.NONE);
                        item.setText("Favorite");
                        menu.addListener(SWT.Show, new Listener() {
                            public void handleEvent(Event event) {
                                String id=getSelectedId();
                                item.setEnabled(id != null);
                                if (id != null) {
                                    item.setImage(ArrayUtils.contains(settings.getArray(EFavoritesAction.PREFS_FAVORITES), id) ? EFavoritesImage.ENABLED_STAR_16
                                            .getImage() : EFavoritesImage.DISABLED_STAR_16.getImage());
                                }
                            }
                        });
                        item.addListener(SWT.Selection, new Listener() {
                            public void handleEvent(Event event) {
                                String id=getSelectedId();
                                int index=-1;
                                int i=0;
                                String[] favorites=settings.getArray(EFavoritesAction.PREFS_FAVORITES);
                                if (favorites != null) {
                                    for (String str : favorites) {
                                        if (str.equals(id)) {
                                            index=i;
                                            break;
                                        }
                                        i++;
                                    }
                                } else
                                    favorites=new String[] {};
                                if (index != -1) {
                                    // Remove favorite
                                    settings.put(EFavoritesAction.PREFS_FAVORITES, ArrayUtils.remove(favorites, index));
                                } else {
                                    // Add favorite
                                    settings.put(EFavoritesAction.PREFS_FAVORITES, ArrayUtils.add(favorites, id));
                                }
                            }
                        });
                        tree.setMenu(menu);
                    }

                    private String getSelectedId() {
                        PreferenceNode selection=(PreferenceNode) ((IStructuredSelection) dialog.getTreeViewer().getSelection()).getFirstElement();
                        return selection != null ? selection.getId() : null;
                    }
                });
                dialog.getShell().addDisposeListener(new DisposeListener() {
                    public void widgetDisposed(DisposeEvent e) {
                        configure();
                    }
                });
            }
        });
    }
}
