package org.eclipse.etools;

import org.eclipse.etools.ei18n.preferences.EI18NPreferenceConstants;
import org.eclipse.etools.ei18n.preferences.EI18NPreferencePage;
import org.eclipse.etools.eshell.preferences.EShellPreferencePage;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferenceLinkArea;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

public class EtoolsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage, EI18NPreferenceConstants {
    public EtoolsPreferencePage() {
    }

	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
	
	@Override
	protected void createFieldEditors() {
		addField(new StringFieldEditor(EtoolsPreferenceConstants.LOGGER_TEMPLATE, "Logger to use in Templates: ", getFieldEditorParent()));
		
		addField(new FieldEditor() {
			{
				createControl(getFieldEditorParent());
			}

			@Override
			public int getNumberOfControls() {
				return 2;
			}
			
			@Override
			protected void doStore() {
			}
			
			@Override
			protected void doLoadDefault() {
			}
			
			@Override
			protected void doLoad() {
			}
			
			@Override
			protected void doFillIntoGrid(Composite parent, int numColumns) {
				Label label = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
				label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, numColumns, 1));
				
				new PreferenceLinkArea(parent, SWT.NONE, EI18NPreferencePage.ID, "See <a>''{0}''</a> for translated messages",//$NON-NLS-1$
						(IWorkbenchPreferenceContainer) getContainer(), null).getControl().setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, numColumns, 1));
				
				new PreferenceLinkArea(parent, SWT.NONE, EShellPreferencePage.ID, "See <a>''{0}''</a> for shell commands",//$NON-NLS-1$
						(IWorkbenchPreferenceContainer) getContainer(), null).getControl().setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, numColumns, 1));
			}
			
			@Override
			protected void adjustForNumColumns(int numColumns) {
			}
		});
    }
}
