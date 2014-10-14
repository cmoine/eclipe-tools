package org.eclipse.etools.ei18n.dialogs;

import org.apache.commons.io.input.NullInputStream;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.etools.Activator;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;

public final class PropertiesFileSelectionDialog extends AbstractFileSelectionDialog {
    public PropertiesFileSelectionDialog(Shell parent, String message, IProject project, IFile initialSelection) {
        super(parent, message, project);
        setValidator(new ISelectionStatusValidator() {
            public IStatus validate(Object[] selection) {
                if (selection.length > 0 && selection[0] instanceof IFile /* && ((IFile) selection[0]).getName().endsWith(".properties")*/)
                    return new Status(IStatus.OK, Activator.PLUGIN_ID, ""); //$NON-NLS-1$
                else
                    return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "You must select a property file");
            }
        });
        addFilter(new ViewerFilter() {
            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                if (element instanceof IPackageFragmentRoot)
                    return !((IPackageFragmentRoot) element).isArchive();

                if (element instanceof IPackageFragment)
                    return true;

                return element instanceof IFile && ((IFile) element).getName().endsWith(".properties");
            }
        });
        if (initialSelection != null)
            setInitialSelection(initialSelection);
    }

    @Override
    public IFile getFile() {
        return (IFile) getFirstResult();
    }

    @Override
    protected void createButtonPressed() {
        try {
            IAdaptable selection=(IAdaptable) ((IStructuredSelection) getTreeViewer().getSelection()).getFirstElement();
            //            IPackageFragment packageFragment=(IPackageFragment);
            final IFolder folder=(IFolder) selection.getAdapter(IFolder.class);
            InputDialog dialog=new InputDialog(getShell(), getShell().getText(), "Type a property file name please", "messages.properties",
                    new IInputValidator() {
                        public String isValid(String newText) {
                            if (!newText.endsWith(".properties"))
                                return "File must ends with '.properties'";

                            IResource member=folder.findMember(newText);
                            if (member != null && member.exists())
                                return "File already exists";

                            return null;
                        }
                    });
            if (dialog.open() == Window.OK) {
                IFile file=folder.getFile(dialog.getValue());
                file.create(new NullInputStream(0L), true, new NullProgressMonitor());
                IJavaElement newObj=JavaCore.create(file);
                getTreeViewer().add(selection, newObj);
                getTreeViewer().setSelection(new StructuredSelection(newObj));
            }
        } catch (Exception e) {
            Activator.logError("Failed creating button pressed", e); //$NON-NLS-1$
        }
    }
}