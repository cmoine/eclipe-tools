package org.eclipse.etools.ei18n.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.etools.ei18n.extensions.ImpexExtension;
import org.eclipse.etools.ei18n.extensions.ImpexExtensionManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.internal.wizards.datatransfer.ArchiveFileExportOperation;
import org.eclipse.ui.internal.wizards.datatransfer.WizardArchiveFileResourceExportPage1;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * ExportWizardei18nPage1
 *
 * @author Quentin Lefevre
 */
@SuppressWarnings("restriction")
public class ExportWizardEi18nPage extends WizardArchiveFileResourceExportPage1 {

	/**
     * Button id for a "Select all" button (value 42).
     */
    public static int SELECT_ALL_ID = 42;

	private final IStructuredSelection initialResourceSelection;

    private ComboViewer comboViewer;

    // private Button excelButton;

	public ExportWizardEi18nPage(IStructuredSelection selection) {
		super(selection);
		initialResourceSelection = selection;
	}

	/** (non-Javadoc)
     * Method declared on IDialogPage.
     */
	@Override
    public void createControl(Composite parent) {
        initializeDialogUnits(parent);

        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
                | GridData.HORIZONTAL_ALIGN_FILL));
        composite.setFont(parent.getFont());

        createResourcesGroup(composite);
        // createButtonsGroup(composite);
        createRefreshButton(composite);

        createOptionsGroup(composite);

        createDestinationGroup(composite);

        restoreResourceSpecificationWidgetValues(); // ie.- local
        restoreWidgetValues(); // ie.- subclass hook
        if (initialResourceSelection != null) {
			setupBasedOnInitialSelections();
		}

        updateWidgetEnablements();
        setPageComplete(determinePageCompletion());
        setErrorMessage(null);	// should not initially have error message

        setControl(composite);

        giveFocusToDestination();
        handleTypesEditButtonPressed();
    }


	/**
     *	Create the export options specification widgets.
     */
    @Override
	protected void createOptionsGroupButtons(Group optionsGroup) {
        optionsGroup.setLayout(new GridLayout(2, false));
        new Label(optionsGroup, SWT.NONE).setText("Export format: "); //$NON-NLS-1$
        comboViewer=new ComboViewer(optionsGroup);
        comboViewer.setContentProvider(ArrayContentProvider.getInstance());
        comboViewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                ImpexExtension ext=(ImpexExtension) element;
                return ext.getName();
            }
        });
        List<ImpexExtension> applications=ImpexExtensionManager.getInstance().getApplications();
        comboViewer.setInput(applications);
        comboViewer.setSelection(new StructuredSelection(applications.get(0)));
        // excelButton=new Button(optionsGroup, SWT.CHECK);
        //        excelButton.setText("Excel format export"); //$NON-NLS-1$
    }

    /**
     *	Answer the suffix that files exported from this wizard should have.
     *	If this suffix is a file extension (which is typically the case)
     *	then it must include the leading period character.
     *
     */
    @Override
	protected String getOutputSuffix() {
        ImpexExtension ext=getSelectedImpex();
        return ext == null ? null : "." + ext.getFileExtension(); //$NON-NLS-1$
    }

    private ImpexExtension getSelectedImpex() {
        return comboViewer == null ? null : (ImpexExtension) ((IStructuredSelection) comboViewer.getSelection()).getFirstElement();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.wizards.datatransfer.WizardFileSystemResourceExportPage1#destinationEmptyMessage()
     */
    @Override
    protected String destinationEmptyMessage() {
        return Messages.DestinationEmptyMessage;
    }

    /**
     *  Export the passed resource and recursively export all of its child resources
     *  (iff it's a container).  Answer a boolean indicating success.
     */
    @Override
    protected boolean executeExportOperation(ArchiveFileExportOperation op) {
        op.setCreateLeadupStructure(true);
        op.setUseCompression(true);

        if (!executeOperation(op))
            return true;

        IStatus status=op.getStatus();
        if (!status.isOK()) {
            ErrorDialog.openError(getContainer().getShell(), StringUtils.EMPTY, null, status);
            return false;
        }

        return true;
    }

    private boolean executeOperation(IRunnableWithProgress op) {
        try {
            getContainer().run(true, true, op);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
            displayErrorDialog(e.getTargetException());
            return false;
        }
        return true;
    }

    /**
     * The Finish button was pressed.  Try to do the required work now and answer
     * a boolean indicating success.  If false is returned then the wizard will
     * not close.
     * @returns boolean
     */
    @Override
    public boolean finish() {
    	@SuppressWarnings("rawtypes")
		List resourcesToExport = getWhiteCheckedResources();

        if (!ensureTargetIsValid()) {
			return false;
		}
        //Save dirty editors if possible but do not stop if not all are saved
        saveDirtyEditors();

        return executeOperation(getSelectedImpex().getApplication().getExportOperation(Iterables.filter(resourcesToExport, IFile.class), getDestinationValue()));
    }

    /**
     *	Open an appropriate destination browser so that the user can specify a source
     *	to import from
     */
    @Override
    protected void handleDestinationBrowseButtonPressed() {
        FileDialog dialog = new FileDialog(getContainer().getShell(), SWT.SAVE | SWT.SHEET);
        dialog.setFilterExtensions(new String[] { getSelectedImpex().getApplication().getFileExtension(), "*.*" }); //$NON-NLS-1$ 
        dialog.setText(StringUtils.EMPTY);
        String currentSourceString = getDestinationValue();
        int lastSeparatorIndex = currentSourceString
                .lastIndexOf(File.separator);
        if (lastSeparatorIndex != -1) {
			dialog.setFilterPath(currentSourceString.substring(0,
                    lastSeparatorIndex));
		}
        String selectedFileName = dialog.open();

        if (selectedFileName != null) {
            setErrorMessage(null);
            setDestinationValue(selectedFileName);
        }
    }

    /**
     * Returns this page's collection of currently-specified resources to be
     * exported. This returns both folders and files - for just the files use
     * getSelectedResources.
     *
     * @return a collection of resources currently selected
     * for export (element type: <code>IResource</code>)
     */
    @Override
    protected List getWhiteCheckedResources() {
    	@SuppressWarnings("unchecked")
		List<?> result=Lists.newArrayList(super.getWhiteCheckedResources());
    	for (Iterator<?> iterator=result.iterator(); iterator.hasNext();) {
			Object object=iterator.next();
			if(object instanceof IResource && ((IResource)object).getFullPath().toString().contains("/bin/")) //$NON-NLS-1$
				iterator.remove();
		}
    	return result;
    }

    /**
     * Returns whether the extension of the given resource name is an extension that
     * has been specified for export by the user.
     *
     * @param resourceName the resource name
     * @return <code>true</code> if the resource name is suitable for export based
     *   upon its extension
     */
    @Override
    protected boolean hasExportableExtension(String resourceName) {
    	if(StringUtils.isNotEmpty(resourceName)){
			resourceName=resourceName.toLowerCase();
			if (resourceName.endsWith(".properties") && (resourceName.startsWith("messages") || resourceName.startsWith("plugin"))) { //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
    			return true;
    		}
    	}
        return false;
    }

    /**
     * Queries the user for the resource types that are to be exported and returns
     * these types as an array.
     *
     * @return the resource types selected for export (element type:
     *   <code>String</code>), or <code>null</code> if the user canceled the
     *   selection
     */
    @Override
    protected Object[] queryResourceTypesToExport() {
        return new Object[0];
    }

    /**
     * Creates the buttons for selecting specific types or selecting all or none of the
     * elements.
     *
     * @param parent the parent control
     */
    protected void createRefreshButton(Composite parent) {

        Font font = parent.getFont();

        // top level group
        Composite buttonComposite = new Composite(parent, SWT.NONE);
        buttonComposite.setFont(parent.getFont());

        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.makeColumnsEqualWidth = true;
        buttonComposite.setLayout(layout);
        buttonComposite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
                | GridData.HORIZONTAL_ALIGN_FILL));

        // types edit button
        Button selectAllButton = createButton(buttonComposite,
        		SELECT_ALL_ID, Messages.selectAllMessagesProperties, false);

        SelectionListener listener = new SelectionAdapter() {
            @Override
			public void widgetSelected(SelectionEvent e) {
            	if (initialResourceSelection != null) {
        			setupBasedOnInitialSelections();
        		}
                handleTypesEditButtonPressed();
            }
        };
        selectAllButton.addSelectionListener(listener);
        selectAllButton.setFont(font);
        setButtonLayoutData(selectAllButton);
    }

}
