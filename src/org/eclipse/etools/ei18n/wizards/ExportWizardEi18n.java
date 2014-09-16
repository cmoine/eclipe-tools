/**
 */
package org.eclipse.etools.ei18n.wizards;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.etools.Activator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import com.google.common.collect.Lists;

/**
 * ExportWizardei18n
 * 
 * @author Quentin Lefevre
 */
public class ExportWizardEi18n extends Wizard implements IExportWizard {
    private IStructuredSelection selection;
    private ExportWizardEi18nPage mainPage;

    /*
     * (non-Javadoc) Method declared on IWizard.
     */
    @Override
    public void addPages() {
        super.addPages();
        mainPage = new ExportWizardEi18nPage(selection);
        addPage(mainPage);
    }

    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection=selection;
        List<IResource> selectedResources=computeSelectedResources(selection);
        if (!selectedResources.isEmpty()) {
            this.selection=new StructuredSelection(selectedResources);
        }

        setWindowTitle(Messages.ExportWizardei18nTitle);
        setDefaultPageImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor("icons/localizedProperties.png"));//$NON-NLS-1$
        setNeedsProgressMonitor(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        return mainPage.finish();
    }


    /**
     * Extracts and returns the <code>IResource</code>s in the given selection or the resource objects they adapts to.
     * 
     * @param originalSelection
     *            the original selection, possibly empty
     * @return list of resources (element type: <code>IResource</code>), possibly empty
     */
    private static List<IResource> computeSelectedResources(IStructuredSelection originalSelection) {
        List<IResource> resources=null;
        for (Iterator<?> e=originalSelection.iterator(); e.hasNext();) {
            Object next=e.next();
            IResource resource=null;
            if (next instanceof IResource) {
                resource=(IResource) next;
            } else if (next instanceof IAdaptable) {
                resource=(IResource) ((IAdaptable) next).getAdapter(IResource.class);
            }
            if (resource != null) {
                if (resources == null) {
                    resources=Lists.newArrayListWithCapacity(originalSelection.size());
                }
                resources.add(resource);
            }
        }
        if (resources == null) {
            return Collections.emptyList();
        }
        return resources;
    }

}
