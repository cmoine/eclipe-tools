package org.eclipse.etools.ei18n.actions;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.etools.Activator;
import org.eclipse.etools.ei18n.builder.I18NNature;

public class ToggleNatureAction extends CommonAction {
	public ToggleNatureAction() {
        updateText();
    }

    private void updateText() {
        setText(getNatureIndex() != -1 ? "Remove i18n Nature" : "Add i18n Nature"); //$NON-NLS-1$ //$NON-NLS-2$
    }

	@Override
	public void run() {
        //		for (Iterator<Object> it=((IStructuredSelection) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection()).iterator(); it
        //				.hasNext();) {
        //			Object element=it.next();
        //			IProject project=null;
        //			if (element instanceof IProject) {
        //				project=(IProject) element;
        //			} else if (element instanceof IAdaptable) {
        //				project=(IProject) ((IAdaptable) element).getAdapter(IProject.class);
        //			}
        //			if (project != null) {
        //				toggleNature(project, false);
        //			}
        //		}
        try {
            IProjectDescription description=getProject().getDescription();
            String[] natures=description.getNatureIds();
            int index=getNatureIndex();
            if (index != -1) {
                // Remove the nature
                String[] newNatures=new String[natures.length - 1];
                System.arraycopy(natures, 0, newNatures, 0, index);
                System.arraycopy(natures, index + 1, newNatures, index, natures.length - index - 1);
                description.setNatureIds(newNatures);
                getProject().setDescription(description, null);
            } else {
                // Add the nature
                description.setNatureIds(ArrayUtils.add(natures, I18NNature.NATURE_ID));
                getProject().setDescription(description, null);
            }
            updateText();
        } catch (CoreException e) {
            Activator.log(IStatus.ERROR, "Error while toggling project nature of " + getProject(), e); //$NON-NLS-1$
        }
	}

    private int getNatureIndex() {
        try {
            IProjectDescription description=getProject().getDescription();
            String[] natures=description.getNatureIds();

            for (int i=0; i < natures.length; ++i) {
                if (I18NNature.NATURE_ID.equals(natures[i])) {
                    return i;
                    // Remove the nature
                    //              if (force)
                    //                  return;

                    //                String[] newNatures=new String[natures.length - 1];
                    //                System.arraycopy(natures, 0, newNatures, 0, i);
                    //                System.arraycopy(natures, i + 1, newNatures, i, natures.length - i - 1);
                    //                description.setNatureIds(newNatures);
                    //                project.setDescription(description, null);
                    //                return;
                }
            }
        } catch (CoreException e) {
            Activator.log(IStatus.ERROR, "Failed to check nature", e); //$NON-NLS-1$
        }
        return -1;
    }
}
