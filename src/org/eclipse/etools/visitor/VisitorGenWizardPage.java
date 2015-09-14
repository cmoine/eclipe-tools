package org.eclipse.etools.visitor;

import java.util.Set;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.google.common.collect.Multimap;

public class VisitorGenWizardPage extends WizardPage {
	private TreeViewer viewer;
	private Multimap<String, String> multimap;
	private ComboViewer comboViewer;

	public VisitorGenWizardPage(Multimap<String, String> multimap) {
		super("Please choose the supertype");
		this.multimap = multimap;
	}

	public void createControl(Composite parent) {
		Composite composite=new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		new Label(composite, SWT.NONE).setText("Supertype"); //$NON-NLS-1$
		comboViewer = new ComboViewer(composite);
//		List<String> input=new ArrayList<String>();
//		for(String str: multimap.keySet()) {
//			if(!multimap.containsValue(str)) {
//				input.add(str);
//			}
//		}
		comboViewer.setContentProvider(ArrayContentProvider.getInstance());
		Set<String> keySet = multimap.keySet();
		comboViewer.setInput(keySet);

		viewer = new TreeViewer(composite);
		class MyContentProvider implements ITreeContentProvider {
			public void dispose() {
				// TODO Auto-generated method stub
				
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// TODO Auto-generated method stub
				
			}

			public Object[] getElements(Object inputElement) {
//				return ((Collection<String>)inputElement).toArray();
//				return multimap.get((String) inputElement).toArray();
				return getChildren(inputElement);
			}

			public Object[] getChildren(Object parentElement) {
				return multimap.get((String) parentElement).toArray();
			}

			public Object getParent(Object element) {
				// TODO Auto-generated method stub
				return null;
			}

			public boolean hasChildren(Object element) {
				return multimap.containsKey(element);
			}
			
		}
		viewer.setContentProvider(new MyContentProvider());
//		viewer.addCheckStateListener(new ICheckStateListener() {
//			public void checkStateChanged(CheckStateChangedEvent event) {
//				viewer.setSubtreeChecked(event.getElement(), event.getChecked());
//			}
//		});
		viewer.setLabelProvider(new LabelProvider());
		viewer.getControl().setLayoutData(GridDataFactory.fillDefaults().span(2, 1).grab(true, true).create());
//		new Label(composite, SWT.NONE).setText("Output package");
//		Text text = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
//		text.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
//		text.setText(pkg);
//		new Label(composite, SWT.NONE).setText("Output class name");
//		classname = new Text(composite, SWT.BORDER);
//		classname.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
//		classname.setText("ModelSwitch"); //$NON-NLS-1$
//		return composite;
		comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Object firstElement = ((IStructuredSelection)event.getSelection()).getFirstElement();
//				if(firstElement!=null)
				viewer.setInput(firstElement);
			}
		});
		comboViewer.setSelection(new StructuredSelection(keySet.iterator().next()));

		setControl(composite);
	}
	
	public String getSuperType() {
		return (String) viewer.getInput();
	}

	public Multimap<String, String> getMultimap() {
		return multimap;
	}
}
