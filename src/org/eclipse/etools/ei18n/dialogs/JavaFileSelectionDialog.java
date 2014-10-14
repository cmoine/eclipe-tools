package org.eclipse.etools.ei18n.dialogs;

import java.io.ByteArrayInputStream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.etools.Activator;
import org.eclipse.etools.ei18n.EI18NStartup;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.template.java.CodeTemplateContext;
import org.eclipse.jdt.internal.corext.template.java.CodeTemplateContextType;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;

public final class JavaFileSelectionDialog extends AbstractFileSelectionDialog {
    public JavaFileSelectionDialog(Shell parent, String message, IProject project, IFile initialSelection) {
        super(parent, message, project);
        //        super(parent, new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT), new StandardJavaElementContentProvider());
        setValidator(new ISelectionStatusValidator() {
            public IStatus validate(Object[] selection) {
                if (selection.length > 0 && selection[0].getClass() == org.eclipse.jdt.internal.core.CompilationUnit.class)
                    return new Status(IStatus.OK, Activator.PLUGIN_ID, ""); //$NON-NLS-1$
                else
                    return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "You must select a compilation unit");
            }
        });
        //        setComparator(new JavaElementComparator());
        //        setTitle(getShell().getText());
        //                setTitle(NewWizardMessages.NewFContainerWizardPage_ChooseSourceContainerDialog_title);
        //        setMessage(message);
        addFilter(new ViewerFilter() {
            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                //                            if (element.getClass() == JarPackageFragmentRoot.class)
                //                                return false;

                if (element instanceof IPackageFragmentRoot)
                    return !((IPackageFragmentRoot) element).isArchive();

                if (element instanceof IPackageFragment)
                    return true;

                return element.getClass() == org.eclipse.jdt.internal.core.CompilationUnit.class;
            }
        });
        if (initialSelection != null) {
            setInitialSelection(JavaCore.create(initialSelection));
        }
    }

    @Override
    public IFile getFile() {
        try {
            return (IFile) ((org.eclipse.jdt.internal.core.CompilationUnit) getFirstResult()).getCorrespondingResource();
        } catch (JavaModelException e) {
            Activator.logError("failed finding corresponding IResource", e); //$NON-NLS-1$
            return null;
        }
    }

    @Override
    protected void createButtonPressed() {
        try {
            IPackageFragment packageFragment=(IPackageFragment) ((IStructuredSelection) getTreeViewer().getSelection()).getFirstElement();
            final IFolder folder=(IFolder) packageFragment.getCorrespondingResource();
            InputDialog dialog=new InputDialog(getShell(), getShell().getText(), "Type a class name please", "Messages.java", new IInputValidator() {
                public String isValid(String newText) {
                    if (!newText.endsWith(".java"))
                        return "File must ends with '.java'";

                    IResource member=folder.findMember(newText);
                    if (member != null && member.exists())
                        return "File already exists";

                    return null;
                }
            });
            if (dialog.open() == Window.OK) {
                IFile file=folder.getFile(dialog.getValue());
                file.create(new ByteArrayInputStream(getContents(StringUtils.substringBeforeLast(dialog.getValue(), "."), folder)), true,
                        new NullProgressMonitor());
                IJavaElement newObj=JavaCore.create(file);
                getTreeViewer().add(packageFragment, newObj);
                getTreeViewer().setSelection(new StructuredSelection(newObj));
            }
        } catch (Exception e) {
            Activator.logError("Failed creating button pressed", e); //$NON-NLS-1$
        }
    }

    private byte[] getContents(String typeName, IFolder folder) {
        try {
            IJavaElement javaElt=JavaCore.create(folder);
            IJavaProject javaProject=javaElt.getJavaProject();
            String lineDelimiter=StubUtility.getLineDelimiterUsed(javaProject);
            Template template=StubUtility.getCodeTemplate(EI18NStartup.EI18N_CLASS_ID, javaProject);
            CodeTemplateContext context=new CodeTemplateContext(template.getContextTypeId(), javaProject, lineDelimiter);

            context.setVariable(CodeTemplateContextType.PACKAGENAME, javaElt.getElementName());
            context.setVariable(CodeTemplateContextType.PROJECTNAME, JavaCore.create(folder.getProject()).getElementName());
            context.setVariable(CodeTemplateContextType.TYPENAME, typeName);

            String body=context.evaluate(template).getString();

            return body.getBytes(getProject().getDefaultCharset());
        } catch (Exception e) {
            Activator.logError("Failed getting contents", e); //$NON-NLS-1$
        }
        return ArrayUtils.EMPTY_BYTE_ARRAY;
    }
}