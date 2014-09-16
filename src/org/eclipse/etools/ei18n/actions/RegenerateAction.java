package org.eclipse.etools.ei18n.actions;

import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.etools.Activator;

public class RegenerateAction extends CompilationUnitAction {
	public RegenerateAction() {
		super("Regenerate"); //$NON-NLS-1$
	}

	@Override
	public void run() {
		List<ICompilationUnit> cus=getCompilationUnits();
		if (cus != null) {
			for (ICompilationUnit cu : cus) {
				try {
					IType type=cu.getTypes()[0];
					Properties props=getBundle(cu.getResource(), type);
					for (Object key : props.keySet()) {
						createField(type, (String) key);
					}
				} catch (JavaModelException e) {
					Activator.log(IStatus.ERROR, "Failed creating field ", e); //$NON-NLS-1$
				} catch (CoreException e) {
					Activator.log(IStatus.ERROR, "Failed opening bundle file", e); //$NON-NLS-1$
				}
			}
		}
		MessageDialog.openError(getShell(), "Error", "This selection is not supproted"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected void createField(IType type, String fieldName) throws JavaModelException {
		IField field=type.getField(fieldName);
		if (field == null || !field.exists()) {
			type.createField("public static String " + fieldName + ";", null, false, new NullProgressMonitor()); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
}
