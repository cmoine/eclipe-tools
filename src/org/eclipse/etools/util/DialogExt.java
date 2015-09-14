package org.eclipse.etools.util;

import org.eclipse.etools.Activator;
import org.eclipse.jface.dialogs.DialogTray;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public abstract class DialogExt extends TitleAreaDialog {
    private final String message;
    private final String title;

    public static final Point MAX_SIZE_HINT=new Point(1200, 800);
    public static final Point DEFAULT_SIZE_HINT=new Point(500, 500);
    public static final Point SMALL_SIZE_HINT=new Point(300, 300);
    public static final Point LANDSCAPE_SIZE_HINT=new Point(800, 500);
    public static final Point MAX_LANDSCAPE_SIZE_HINT=new Point(1500, 700);

    public DialogExt(Shell parentShell) {
        this(parentShell, parentShell.getText(), null);
    }

    public DialogExt(Shell parentShell, String title, String message) {
        super(parentShell);
        this.title=title;
        this.message=message;
    }

    public DialogExt(Shell parentShell, String title) {
        this(parentShell, title, null);
    }

    @Override
    public void openTray(DialogTray tray) throws IllegalStateException, UnsupportedOperationException {
        super.openTray(tray);
        initializeBounds();
    }

    @Override
    public void closeTray() throws IllegalStateException {
        super.closeTray();
        initializeBounds();
    }

    @Override
    protected final Control createDialogArea(Composite parent) {
        Composite composite=(Composite) super.createDialogArea(parent);
        GridData layoutData=new GridData(SWT.FILL, SWT.FILL, true, true);
        layoutData.widthHint=getSizeHint().x;
        layoutData.heightHint=getSizeHint().y;
        doCreateDialogArea(composite).setLayoutData(layoutData);
        if (title != null)
            setTitle(title);
        if (message != null)
            setMessage(message);
        return composite;
    }

    protected Point getSizeHint() {
        return DEFAULT_SIZE_HINT;
    }

    @Override
    protected IDialogSettings getDialogBoundsSettings() {
        return Activator.getDefault().getOrCreateDialogSettings(getClass());
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    protected void initializeBounds() {
        super.initializeBounds();
        if (getParentShell() != null) {
            Shell shell=getShell();
            shell.setText(getParentShell().getText());
        }
    }

    protected abstract Control doCreateDialogArea(Composite parent);
}
