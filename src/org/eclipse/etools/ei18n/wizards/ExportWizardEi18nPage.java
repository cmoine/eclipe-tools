package org.eclipse.etools.ei18n.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.etools.Activator;
import org.eclipse.etools.SelectionUtils;
import org.eclipse.etools.ei18n.EI18NImage;
import org.eclipse.etools.ei18n.extensions.IImpex;
import org.eclipse.etools.ei18n.extensions.ImpexExtension;
import org.eclipse.etools.ei18n.extensions.ImpexExtensionManager;
import org.eclipse.etools.ei18n.util.EI18NConstants;
import org.eclipse.etools.ei18n.util.EI18NUtil;
import org.eclipse.etools.ei18n.util.MappingPreference;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.BidiUtils;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.dialogs.WizardDataTransferPage;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

/**
 * ExportWizardei18nPage1
 *
 * @author Quentin Lefevre
 */
@SuppressWarnings("restriction")
public class ExportWizardEi18nPage extends WizardDataTransferPage {
    private final static String STORE_DESTINATION_NAMES_ID="ExportWizardEi18nPage.STORE_DESTINATION_NAMES_ID"; //$NON-NLS-1$

    private static final String FILE="file"; //$NON-NLS-1$

    //    private static final String SELECT_DESTINATION_MESSAGE=DataTransferMessages.FileExport_selectDestinationMessage;
    private static final String SELECT_DESTINATION_TITLE=DataTransferMessages.FileExport_selectDestinationTitle;

    private final IStructuredSelection selection;
    private ComboViewer comboViewer;
    private Combo destinationNameField;

    private CheckboxTreeViewer viewer;

    private Button destinationBrowseButton;

    public ExportWizardEi18nPage(IStructuredSelection selection) {
        super(ExportWizardEi18nPage.class.getName());
        this.selection=selection;
    }

    public void handleEvent(Event event) {
        Widget source=event.widget;

        if (source == destinationBrowseButton) {
            handleDestinationBrowseButtonPressed();
        }

        updatePageCompletion();
    }

    protected void handleDestinationBrowseButtonPressed() {
        FileDialog dialog=new FileDialog(getContainer().getShell(), SWT.SAVE | SWT.SHEET);
        dialog.setText(SELECT_DESTINATION_TITLE);
        dialog.setFilterExtensions(new String[] { "*." + getSelectedImpex().getFileExtension(), "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
        dialog.setFilterPath(getDestinationValue());
        String selectedDirectoryName=dialog.open();

        if (selectedDirectoryName != null) {
            setErrorMessage(null);
            //            setDestinationValue(selectedDirectoryName);
            destinationNameField.setText(selectedDirectoryName);
        }
    }

    @Override
    protected void saveWidgetValues() {
        // update directory names history
        IDialogSettings settings=getDialogSettings();
        if (settings != null) {
            String[] directoryNames=settings.getArray(STORE_DESTINATION_NAMES_ID);
            if (directoryNames == null) {
                directoryNames=new String[0];
            }

            directoryNames=addToHistory(directoryNames, getDestinationValue());
            settings.put(STORE_DESTINATION_NAMES_ID, directoryNames);
        }
    }

    @Override
    protected IDialogSettings getDialogSettings() {
        return Activator.getDefault().getOrCreateDialogSettings(ExportWizardEi18nPage.class);
    }

    @Override
    protected void restoreWidgetValues() {
        IDialogSettings settings=getDialogSettings();
        if (settings != null) {
            String[] directoryNames=settings.getArray(STORE_DESTINATION_NAMES_ID);
            if (directoryNames == null || directoryNames.length == 0) {
                return; // ie.- no settings stored
            }

            // destination
            destinationNameField.setText(directoryNames[0]);
            for (int i=0; i < directoryNames.length; i++) {
                destinationNameField.add(directoryNames[i]);
            }
        }
    }

    @Override
    protected boolean allowNewContainerName() {
        // TODO Auto-generated method stub
        return false;
    }

    public void createControl(Composite parent) {
        Composite composite=new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(3, false));

        createOptionsGroup(composite);
        ((GridData) composite.getChildren()[0].getLayoutData()).horizontalSpan=2;
        composite.getChildren()[0].setLayoutData(GridDataFactory.fillDefaults().grab(true, true).span(3, 1).create());

        {
            Label destinationLabel=new Label(composite, SWT.NONE);
            destinationLabel.setText(DataTransferMessages.ArchiveExport_destinationLabel);

            // destination name entry field
            destinationNameField=new Combo(composite, SWT.SINGLE | SWT.BORDER);
            destinationNameField.addListener(SWT.Modify, this);
            destinationNameField.addListener(SWT.Selection, this);
            GridData data=new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
            data.widthHint=SIZING_TEXT_FIELD_WIDTH;
            destinationNameField.setLayoutData(data);
            //        destinationNameField.setFont(font);
            BidiUtils.applyBidiProcessing(destinationNameField, FILE);

            destinationBrowseButton=new Button(composite, SWT.PUSH);
            destinationBrowseButton.setText(DataTransferMessages.DataTransfer_browse);
            destinationBrowseButton.addListener(SWT.Selection, this);
            //            destinationBrowseButton.setFont(font);
            setButtonLayoutData(destinationBrowseButton);

            restoreWidgetValues();
        }

        setControl(composite);
    }

    @Override
    protected GridData setButtonLayoutData(Button button) {
        GridData data=new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        int widthHint=convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
        Point minSize=button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        data.widthHint=Math.max(widthHint, minSize.x);
        button.setLayoutData(data);
        return data;
    }

    private final Multimap<IResource, IResource> multimap=HashMultimap.create();

    private class MyContentProvider extends ArrayContentProvider implements ITreeContentProvider {
        public Object[] getChildren(Object parentElement) {
            return multimap.get((IResource) parentElement).toArray();
        }

        public Object getParent(Object element) {
            IResource res=(IResource) element;
            return res.getParent();
        }

        public boolean hasChildren(Object element) {
            return multimap.containsKey(element);
        }
    }

    @Override
    protected void createOptionsGroupButtons(Group optionsGroup) {
        optionsGroup.setLayout(new GridLayout(2, false));

        {
            //            Table table=new Table(optionsGroup, SWT.BORDER | SWT.CHECK | SWT.FULL_SELECTION);
            //            table.setLinesVisible(true);
            viewer=new CheckboxTreeViewer(optionsGroup);
            viewer.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).span(2, 1).create());
            viewer.setContentProvider(new MyContentProvider());
            viewer.setLabelProvider(new LabelProvider() {
                @Override
                public String getText(Object element) {
                    IResource file=(IResource) element;
                    return file.getName();
                }

                @Override
                public Image getImage(Object element) {
                    IResource res=(IResource) element;
                    if (res instanceof IFile)
                        return EI18NImage.getImage((IFile) res);
                    else
                        return WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider().getImage(res);
                }
            });
            viewer.addCheckStateListener(new ICheckStateListener() {
                public void checkStateChanged(CheckStateChangedEvent event) {
                    viewer.setSubtreeChecked(event.getElement(), event.getChecked());
                }
            });

            List<IResource> resources=SelectionUtils.getResources(selection);
            Set<IProject> projects=new HashSet<IProject>();
            for (IResource res : resources) {
                projects.add(res.getProject());
            }
            final Set<IFile> validFiles=new HashSet<IFile>();
            for (IProject project : projects) {
                for (MappingPreference mapping : MappingPreference.list(project)) {
                    validFiles.add(mapping.getPropertyFile());
                }
            }
            final List<IFile> files=new ArrayList<IFile>();

            for (IResource res : resources) {
                try {
                    res.accept(new IResourceVisitor() {
                        public boolean visit(IResource resource) throws CoreException {
                            if (resource instanceof IFile && EI18NConstants.LOCALE_PATTERN.matcher(resource.getName()).matches()) {
                                if (validFiles.contains(EI18NUtil.getDefaultFile((IFile) resource)))
                                    files.add((IFile) resource);
                            } else if (validFiles.contains(resource)) {
                                files.add((IFile) resource);
                            }

                            return true;
                        }
                    });
                } catch (CoreException e) {
                    Activator.logError("Failed to visit " + res, e); //$NON-NLS-1$
                }
            }

            Set<IProject> input=new HashSet<IProject>();
            for (IFile file : files) {
                IResource tmp=file;
                while (true) {
                    IResource oldTmp=tmp;
                    tmp=tmp.getParent();
                    multimap.put(tmp, oldTmp);
                    if(tmp instanceof IProject) {
                        input.add((IProject) tmp);
                        break;
                    }
                }
            }
            viewer.setInput(input);
            for (IProject project : input)
                viewer.setSubtreeChecked(project, true);
        }

        {
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
            if (!applications.isEmpty())
                comboViewer.setSelection(new StructuredSelection(applications.get(0)));
        }
        // excelButton=new Button(optionsGroup, SWT.CHECK);
        //        excelButton.setText("Excel format export"); //$NON-NLS-1$
    }

    public boolean finish() {
        //Save dirty editors if possible but do not stop if not all are saved
        saveDirtyEditors();

        saveWidgetValues();

        final File dst=new File(getDestinationValue());
        final Iterable<IFile> files=Iterables.filter(Arrays.asList(viewer.getCheckedElements()), IFile.class);
        final IImpex impex=getSelectedImpex().getApplication();
        return executeOperation(new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                impex.export(files, dst, monitor);
                Program.launch(dst.getPath());
            }
        });

    }

    private boolean executeOperation(IRunnableWithProgress op) {
        try {
            getContainer().run(true, true, op);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
            Activator.logError("Failed exporting", e); //$NON-NLS-1$
            displayErrorDialog(e.getTargetException());
            return false;
        }
        return true;
    }

    protected String getDestinationValue() {
        return destinationNameField.getText().trim();
    }

    protected boolean saveDirtyEditors() {
        return IDEWorkbenchPlugin.getDefault().getWorkbench().saveAllEditors(true);
    }

    private ImpexExtension getSelectedImpex() {
        return comboViewer == null ? null : (ImpexExtension) ((IStructuredSelection) comboViewer.getSelection()).getFirstElement();
    }
}
