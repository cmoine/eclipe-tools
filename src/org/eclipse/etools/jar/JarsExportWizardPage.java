package org.eclipse.etools.jar;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.etools.Activator;
import org.eclipse.etools.ei18n.wizards.ExportWizardEi18nPage;
import org.eclipse.etools.util.ArrayTreeContentProvider;
import org.eclipse.etools.util.SelectionUtils;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.ui.packageview.ClassPathContainer;
//import org.eclipse.jdt.internal.ui.packageview.LibraryContainer;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerContentProvider;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerLabelProvider;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.dialogs.WizardDataTransferPage;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;

public class JarsExportWizardPage extends WizardDataTransferPage {
    private final static String STORE_DESTINATION_NAMES_ID="JarsExportWizardPage.STORE_DESTINATION_NAMES_ID"; //$NON-NLS-1$
    private static final String SELECT_DESTINATION_TITLE=DataTransferMessages.FileExport_selectDestinationTitle;
//	private Multimap<IClasspathEntry, JarPackageFragmentRoot> multimap=LinkedHashMultimap.create();
	private List<ClassPathContainer> input=new ArrayList<ClassPathContainer>();
//	private Composite composite;
	private final PackageExplorerContentProvider contentProvider=new PackageExplorerContentProvider(false);
//	private Text text;
	private CheckboxTreeViewer viewer;
//    private ComboViewer comboViewer;
    private Combo destinationNameField;
    private Button destinationBrowseButton;

	public JarsExportWizardPage(IStructuredSelection selection) {
		super("Export JARs");
//		List<JarPackageFragmentRoot> input=new ArrayList<JarPackageFragmentRoot>();
		for(IResource res: SelectionUtils.getResources(selection)) {
			if(res instanceof IProject) {
				try {
					IJavaProject javaProject = JavaCore.create((IProject)res);
					for(IClasspathEntry entry: javaProject.getRawClasspath()) {
						if(entry.getEntryKind()==IClasspathEntry.CPE_CONTAINER && !entry.getPath().toString().startsWith("org.eclipse.jdt.launching.JRE_CONTAINER")) {
							ClassPathContainer container=new ClassPathContainer(javaProject, entry);
							input.add(container);
						}
					}
				} catch (CoreException e) {
					Activator.logWarning("Not a Java Project", e); //$NON-NLS-1$
				} catch(RuntimeException e) {
					Activator.logWarning("Not a Java Project", e); //$NON-NLS-1$
				}
			}
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
    
    protected String getDestinationValue() {
        return destinationNameField.getText().trim();
    }

    @Override
    protected boolean allowNewContainerName() {
        // TODO Auto-generated method stub
        return false;
    }

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		composite.setLayout(layout);
		
		createOptionsGroup(composite);
        ((GridData) composite.getChildren()[0].getLayoutData()).horizontalSpan=2;
        composite.getChildren()[0].setLayoutData(GridDataFactory.fillDefaults().grab(true, true).span(layout.numColumns, 1).create());
		

//		Tree tree = viewer.getTree();
//		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, layout.numColumns, 1));
		
//		new Label(composite, SWT.NONE).setText("Output directory:");
//		text = new Text(composite, SWT.BORDER);
//		text.addModifyListener(new ModifyListener() {
//			public void modifyText(ModifyEvent e) {
//				validate();
//			}
//		});
//		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
//		Button button = new Button(composite, SWT.NONE);
//		button.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				DirectoryDialog dialog=new DirectoryDialog(text.getShell());
//				String dir = dialog.open();
//				if(dir!=null) {
//					text.setText(dir);
//				}
//			}
//		});
//		button.setText("..."); //$NON-NLS-1$
		{
            Label destinationLabel=new Label(composite, SWT.NONE);
            destinationLabel.setText(DataTransferMessages.FileExport_selectDestinationTitle);

            // destination name entry field
            destinationNameField=new Combo(composite, SWT.SINGLE | SWT.BORDER);
            destinationNameField.addListener(SWT.Modify, this);
            destinationNameField.addListener(SWT.Selection, this);
            GridData data=new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
            data.widthHint=SIZING_TEXT_FIELD_WIDTH;
            destinationNameField.setLayoutData(data);
            //        destinationNameField.setFont(font);
            // BidiUtils.applyBidiProcessing(destinationNameField, FILE);

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
	protected void createOptionsGroupButtons(Group parent) {
		parent.setLayout(new GridLayout(2, false));
		viewer = new CheckboxTreeViewer(parent);
		viewer.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		viewer.setContentProvider(new ArrayTreeContentProvider() {
			@Override
			public Object[] getChildren(Object parentElement) {
				if(parentElement instanceof ClassPathContainer) {
					IAdaptable[] children = ((ClassPathContainer) parentElement).getChildren();
					List<JarPackageFragmentRoot> result=new ArrayList<JarPackageFragmentRoot>();
					for(IAdaptable adaptable: children) {
						if(adaptable instanceof JarPackageFragmentRoot) {
							result.add((JarPackageFragmentRoot) adaptable);
						}
					}
					return result.toArray();
//				} else if (parentElement instanceof JarPackageFragmentRoot) {
//					return ArrayUtils.EMPTY_OBJECT_ARRAY;
				}
				return super.getChildren(parentElement);
			}
			
			@Override
			public boolean hasChildren(Object element) {
				return element instanceof ClassPathContainer;
			}
		});
		viewer.addCheckStateListener(new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
                viewer.setSubtreeChecked(event.getElement(), event.getChecked());
            }
        });
		viewer.setLabelProvider(new PackageExplorerLabelProvider(contentProvider));
		viewer.setInput(input);
		for(ClassPathContainer cpc: input)
			viewer.setSubtreeChecked(cpc, true);
		viewer.expandAll();
		viewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				validate();
			}
		});
	}

    public void handleEvent(Event event) {
        Widget source=event.widget;

        if (source == destinationBrowseButton) {
            handleDestinationBrowseButtonPressed();
        }

        updatePageCompletion();
    }

    protected void handleDestinationBrowseButtonPressed() {
        DirectoryDialog dialog=new DirectoryDialog(getContainer().getShell());
        dialog.setText(SELECT_DESTINATION_TITLE);
//        dialog.setFilterExtensions(new String[] { "*." + getSelectedImpex().getFileExtension(), "*.*" }); //$NON-NLS-1$ //$NON-NLS-2$
        dialog.setFilterPath(getDestinationValue());
        String selectedDirectoryName=dialog.open();

        if (selectedDirectoryName != null) {
            setErrorMessage(null);
            //            setDestinationValue(selectedDirectoryName);
            destinationNameField.setText(selectedDirectoryName);
        }
    }


	protected void validate() {
		setErrorMessage(null);
		String outputDir = destinationNameField.getText();
		if(outputDir.isEmpty()) {
			setErrorMessage(DataTransferMessages.FileExport_destinationEmpty);
			return;
		}
		if(!new File(outputDir).exists()) {
			setErrorMessage("You must define an existing output directory");
			return;
		}
		if(getJars().isEmpty()) {
			setErrorMessage(DataTransferMessages.FileImport_sourceEmpty);
		}
	}

	public boolean finish() {
		saveWidgetValues();

		final File dir=new File(getDestinationValue());
		final List<JarPackageFragmentRoot> jars = getJars();

		return executeOperation(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				monitor.beginTask("Copying", jars.size());
				for(JarPackageFragmentRoot jar: jars) {
					File file=jar.getPath().toFile();
					try {
						FileUtils.copyFileToDirectory(file, dir);
					} catch (IOException e) {
						throw new InvocationTargetException(e, "Failed copying "+file);
					}
				}
				monitor.done();
			}
		});
	}

	private List<JarPackageFragmentRoot> getJars() {
		List<JarPackageFragmentRoot> jars=new ArrayList<JarPackageFragmentRoot>();
		for(Object o: viewer.getCheckedElements()) {
			if(o instanceof JarPackageFragmentRoot)
				jars.add((JarPackageFragmentRoot) o);
		}
		return jars;
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
}
