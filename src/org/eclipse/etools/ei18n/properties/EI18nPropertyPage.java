package org.eclipse.etools.ei18n.properties;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.etools.Activator;
import org.eclipse.etools.ei18n.actions.ToggleNatureAction;
import org.eclipse.etools.ei18n.extensions.JavaMappingExtensionManager;
import org.eclipse.etools.ei18n.extensions.JavaMappingExtensionManager.JavaMappingExtension;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.dialogs.PropertyPage;

public class EI18nPropertyPage extends PropertyPage {
    private ComboViewer comboViewer;

    public static final QualifiedName QNAME=new QualifiedName(EMPTY, "PROJECT_KIND"); //$NON-NLS-1$

    private Composite composite;

    public static JavaMappingExtension getExtension(IProject project) {
        try {
            JavaMappingExtension extension=JavaMappingExtensionManager.getInstance().find(project.getPersistentProperty(QNAME));
            if (extension != null)
                return extension;
        } catch (CoreException e1) {
            Activator.logWarning("Failed to get property", e1); //$NON-NLS-1$
        }
        List<JavaMappingExtension> all=JavaMappingExtensionManager.getInstance().getAll();
        if (!all.isEmpty())
            return all.get(0);

        // Should never happen
        return null;
    }

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	@Override
    protected Control createContents(Composite parent) {
        composite=new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

        comboViewer=new ComboViewer(composite);
        comboViewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                JavaMappingExtension ext=(JavaMappingExtension) element;
                return ext.getName();
            }
        });
        comboViewer.setContentProvider(ArrayContentProvider.getInstance());
        List<Object> input=new ArrayList<Object>(JavaMappingExtensionManager.getInstance().getAll());
        comboViewer.setInput(input);
        JavaMappingExtension extension=getExtension(getProject());
        comboViewer.setSelection(new StructuredSelection(extension));

        createButton(composite, new ToggleNatureAction());

		return composite;
	}

    private void createButton(Composite parent, final IAction action) {
        final Button button=new Button(parent, SWT.NONE);
        sync(button, action);
        action.addPropertyChangeListener(new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                sync(button, action);
            }
        });
        button.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                action.runWithEvent(event);
            }
        });
    }

    private void sync(Button button, IAction action) {
        button.setText(action.getText());
        button.setEnabled(action.isEnabled());
        composite.layout(true);
    }

    private JavaMappingExtension getSelectedExtension() {
        return (JavaMappingExtension) ((IStructuredSelection) comboViewer.getSelection()).getFirstElement();
    }

    private IProject getProject() {
        return (IProject) getElement();
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();
        try {
            getProject().setPersistentProperty(QNAME, null);
        } catch (CoreException e) {
            Activator.logWarning("Failed to restore to default", e); //$NON-NLS-1$
        }
    }

    @Override
    public boolean performOk() {
        // store the value in the owner text field
        try {
            getProject().setPersistentProperty(QNAME, getSelectedExtension().getId());
        } catch (CoreException e) {
            return false;
        }
        return true;
    }

}