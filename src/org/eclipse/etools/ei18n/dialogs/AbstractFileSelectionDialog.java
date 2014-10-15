package org.eclipse.etools.ei18n.dialogs;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.JavaElementComparator;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.StandardJavaElementContentProvider;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;

abstract class AbstractFileSelectionDialog extends ElementTreeSelectionDialog {
    private final IProject project;

    public AbstractFileSelectionDialog(Shell parent, String message, IProject project) {
        super(parent, new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT), new StandardJavaElementContentProvider());
        this.project=project;
        setComparator(new JavaElementComparator());
        setTitle(parent.getText());
        setMessage(message);
        IJavaProject input=JavaCore.create(project);

        setInput(input);

        setHelpAvailable(false);
    }

    public IProject getProject() {
        return project;
    }

    public abstract IFile getFile();

    @Override
    protected TreeViewer createTreeViewer(Composite parent) {
        TreeViewer treeViewer=super.createTreeViewer(parent);
        treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                boolean enabled=((IStructuredSelection) event.getSelection()).getFirstElement() instanceof IPackageFragment;
                getButton(IDialogConstants.CLIENT_ID).setEnabled(enabled);
            }
        });
        return treeViewer;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CLIENT_ID, "Créer", false);
        super.createButtonsForButtonBar(parent);
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.CLIENT_ID) {
            createButtonPressed();
        } else {
            super.buttonPressed(buttonId);
        }
    }

    protected abstract void createButtonPressed();
}