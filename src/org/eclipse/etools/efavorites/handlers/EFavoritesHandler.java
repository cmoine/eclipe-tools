package org.eclipse.etools.efavorites.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.etools.efavorites.EFavoritesImage;
import org.eclipse.etools.efavorites.actions.EFavoritesAction;
import org.eclipse.etools.efavorites.model.EFavorite;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.handlers.HandlerUtil;

public class EFavoritesHandler extends AbstractHandler {
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell=HandlerUtil.getActiveShell(event);
        ListDialog dialog=new ListDialog(shell) {
            @Override
            protected void constrainShellSize() {
                super.constrainShellSize();
                getShell().setImage(EFavoritesImage.ENABLED_STAR_16.getImage());
                getTableViewer().addPostSelectionChangedListener(new ISelectionChangedListener() {
                    public void selectionChanged(SelectionChangedEvent event) {
                        getButton(OK).setEnabled(true);
                    }
                });
            }
        };
        dialog.setTitle("EFavorites");
        dialog.setMessage("Please select a favorite:");
        dialog.setContentProvider(ArrayContentProvider.getInstance());
        dialog.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return ((EFavorite) element).getName();
            }

            @Override
            public Image getImage(Object element) {
                return ((EFavorite) element).getImage();
            }
        });
        String[] favRaw=EFavoritesAction.SETTINGS.getArray(EFavoritesAction.FAVORITES);
        EFavorite[] input=new EFavorite[favRaw.length];
        for (int i=0; i < favRaw.length; i++) {
            String str=favRaw[i];
            input[i]=new EFavorite(str);
        }
        dialog.setInput(input);
        if (dialog.open() == Window.OK && dialog.getResult().length > 0) {
            ((EFavorite) dialog.getResult()[0]).open();
        }
        for (EFavorite fileImage : input)
            fileImage.dispose();
        return null;
    }
}
