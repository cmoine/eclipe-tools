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
        //        setValidator(new ISelectionStatusValidator() {
        //            @Override
        //            public IStatus validate(Object[] selection) {
        //                if (selection.length > 0 && selection[0].getClass() == org.eclipse.jdt.internal.core.CompilationUnit.class)
        //                    return new Status(IStatus.OK, Activator.PLUGIN_ID, ""); //$NON-NLS-1$
        //                else
        //                    return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "You must select a compilation unit");
        //            }
        //        });
        setComparator(new JavaElementComparator());
        setTitle(parent.getText());
        //                setTitle(NewWizardMessages.NewFContainerWizardPage_ChooseSourceContainerDialog_title);
        setMessage(message);
        //        addFilter(new ViewerFilter() {
        //            @Override
        //            public boolean select(Viewer viewer, Object parentElement, Object element) {
        //                //                            if (element.getClass() == JarPackageFragmentRoot.class)
        //                //                                return false;
        //
        //                if (element instanceof IPackageFragmentRoot)
        //                    return !((IPackageFragmentRoot) element).isArchive();
        //
        //                if (element instanceof IPackageFragment)
        //                    return true;
        //
        //                return element.getClass() == org.eclipse.jdt.internal.core.CompilationUnit.class;
        //            }
        //        });
        IJavaProject input=JavaCore.create(project);

        setInput(input);

        //        if (initialSelection != null) {
            //                    try {
            //                        for (IPackageFragmentRoot fragmentRoot : input.getAllPackageFragmentRoots()) {
            //                            IResource rootFolder=fragmentRoot.getCorrespondingResource();
            //                            if (rootFolder != null) {
            //                                IPath iPath=javaFile.getFullPath().makeRelativeTo(rootFolder.getFullPath());//removeFirstSegments(2);//makeRelativeTo(getProject().getFullPath());
            //                                if (iPath != null)
            //                                    setInitialSelection(input.findElement(iPath));
            //                            }
            //                        }
            //                    } catch (JavaModelException e1) {
            //                        Activator.log(IStatus.ERROR, "Failed to set initial selection", e1); //$NON-NLS-1$
            //                    }
        //            setInitialSelection(JavaCore.create(initialSelection));
        //        }
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