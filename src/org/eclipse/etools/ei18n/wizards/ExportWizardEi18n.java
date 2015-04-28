/**
 */
package org.eclipse.etools.ei18n.wizards;

import org.eclipse.etools.Activator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

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
}
