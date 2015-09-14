package org.eclipse.etools.ei18n.actions;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.etools.Activator;
import org.eclipse.etools.RemoveMe;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import com.google.common.collect.Lists;

@RemoveMe
public abstract class CompilationUnitAction extends CommonAction {
	protected CompilationUnitAction(String text) {
        setText(text);
	}

	protected List<ICompilationUnit> getCompilationUnits() {
		Object selection=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
		if (selection instanceof IStructuredSelection) {
			List<ICompilationUnit> cus=Lists.newArrayList();
			for (Iterator<Object> it=((IStructuredSelection) selection).iterator(); it.hasNext();) {
				Object element=it.next();
				if (element instanceof IJavaElement)
					cus.addAll(collectCompilationUnits((IJavaElement) element));
			}
			return cus;
		} else if (selection instanceof TextSelection) {
			IEditorPart activeEditor=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
			ICompilationUnit cu=(ICompilationUnit) EditorUtility.getEditorInputJavaElement(activeEditor, false);
            // TODO CME
            //			if (cu != null && CompilationUnitUtil.isValid(cu))
            //				return Arrays.asList(cu);
		}
		return null;
	}

	private static List<ICompilationUnit> collectCompilationUnits(IJavaElement current) {
		List<ICompilationUnit> cus=Lists.newArrayList();
		if (current instanceof IJavaProject || current instanceof IPackageFragmentRoot || current instanceof IPackageFragment) {
			try {
				IJavaElement[] children=((IParent) current).getChildren();
				for (int i=0; i < children.length; i++) {
					List<ICompilationUnit> compilationUnits=collectCompilationUnits(children[i]);
					if (compilationUnits == null)
						return null;
					else
						cus.addAll(compilationUnits);
				}
			} catch (JavaModelException e) {
                Activator.logError("Failed to find children of " + current, e); //$NON-NLS-1$
			}
            // TODO CME
            //		} else if (current != null && current instanceof ICompilationUnit && CompilationUnitUtil.isValid((ICompilationUnit) current)) {
            //			cus.add((ICompilationUnit) current);
		}

		return cus;
	}

    protected Properties getBundle(IResource res, IType type) throws JavaModelException, CoreException {
        // TODO
        //		InputStream is=null;
        //		try {
        //            if (EI18NConstants.NLS_CLASS_NAME.equals(type.getSuperclassName())) { 
        //				IFile file=lookupBundle(res, type);
        //				if (file != null) {
        //					Properties props=new Properties();
        //					is=file.getContents();
        //					props.load(is);
        //					return props;
        //				}
        //			}
        //		} catch (IOException e) {
        //			Activator.log(IStatus.ERROR, "Failed reading bundle file", e); //$NON-NLS-1$
        //		} finally {
        //			IOUtils.closeQuietly(is);
        //		}
		return null;
	}

	protected IFile lookupBundle(IResource resource, IType type) throws JavaModelException {
		String bundleName=getBundleName(type);
		bundleName=StringUtils.replace(bundleName, ".", "/") + ".properties"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		// search in the same directory
		IResource member=resource.getProject().findMember(bundleName);
		if (member != null)
			return (IFile) member;

		// search in the root directory
		// if()
		// IProject project=resource.getProject();
		// if (project instanceof IJavaProject) {

		// IJavaProject javaProject=(IJavaProject) project;
		for (IClasspathEntry entry : type.getJavaProject().getRawClasspath()) {
			if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				member=resource.getWorkspace().getRoot().findMember(entry.getPath().append(bundleName));
				if (member != null)
					return (IFile) member;
			}
		}
		type.getJavaProject().getPackageFragments();
		// member=project.findMember(bundleName);
		// if (member != null)
		// return (IFile) member;
		// }

		return null;
	}

	private String getBundleName(IType type) throws JavaModelException {
		String bundleName=(String) type.getField("BUNDLE_NAME").getConstant(); //$NON-NLS-1$
		bundleName=StringUtils.removeStart(bundleName, "\""); //$NON-NLS-1$
		bundleName=StringUtils.removeEnd(bundleName, "\""); //$NON-NLS-1$
		return bundleName;
	}
}
