package org.eclipse.etools.jar;

import org.eclipse.etools.ei18n.wizards.Messages;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

public class JarsExportWizard extends Wizard implements IExportWizard {
//	private List<JarPackageFragmentRoot> input=new ArrayList<JarPackageFragmentRoot>();
	private IStructuredSelection selection;
	private JarsExportWizardPage page;
	
	@Override
	public void addPages() {
		page = new JarsExportWizardPage(selection);
		addPage(page);
	}
	
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection=selection;

        setWindowTitle(Messages.ExportWizardei18nTitle);
//        setDefaultPageImageDescriptor(JavaPluginImages.Activator.getDefault().getImageRegistry().getDescriptor("icons/localizedProperties.png"));//$NON-NLS-1$
        setNeedsProgressMonitor(true);
	}

	@Override
	public boolean performFinish() {
		return page.finish();
	}

}
