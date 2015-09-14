package org.eclipse.etools.visitor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import com.google.common.base.Joiner;

public class VisitorGenHandler extends AbstractHandler {
	public Object execute(ExecutionEvent event) throws ExecutionException {
//		List<IResource> resources = SelectionUtils.getResources(HandlerUtil.getActiveMenuSelection(event));
//		List<IResource> resources=SelectionUtils.getResources(HandlerUtil.getActiveMenuSelection(event));
//
//        List<ICompilationUnit> cus=SelectionUtils.getCUs(resources);
//        try {
//        	final Multimap<String, String> multimap=HashMultimap.create();
////        	Set<String> proceded=new HashSet<String>();
////        	Stack<IType> toProceed=new Stack<IType>();
//        	IPackageFragment fragment=null;
//        	
//			for(ICompilationUnit cu: cus) {
//				IType type = cu.getTypes()[0];
//				if(fragment==null) {
//					fragment=type.getPackageFragment();
////					folder=(IContainer) type.getCorrespondingResource();
//				}
//
//				ITypeHierarchy hierarchy = type.newSupertypeHierarchy(new NullProgressMonitor());
//				IType superclass = type;
//				String fullyQualifiedName = type.getFullyQualifiedName();
//
//				while(true) {
//					for(IType t: hierarchy.getSuperInterfaces(superclass)) {
//						multimap.put(t.getFullyQualifiedName(), fullyQualifiedName);
//					}
//					superclass=hierarchy.getSuperclass(superclass);
//					if(superclass==null) {
////						multimap.put(Object.class.getName(), fullyQualifiedName);
//						break;
//					}
//
//					multimap.put(superclass.getFullyQualifiedName(), fullyQualifiedName);
//				}
//			}
		try {
//			final String pkg=fragment.getElementName();
//			final String[] clazz=new String[1];
//			final String[] root=new String[1];

			WizardDialog dialog = new WizardDialog(HandlerUtil.getActiveShell(event), new VisitorWizard(event));
			dialog.open();
//			if(dialog.open()==Window.OK) {
//				dialog.
//			}
//			DialogExt dialog=new DialogExt(HandlerUtil.getActiveShell(event)) {
//				private TreeViewer viewer;
//				private Text classname;
//
//				@Override
//				protected Point getSizeHint() {
//					return MAX_SIZE_HINT;
//				}
//
//				@Override
//				protected Control doCreateDialogArea(Composite parent) {
//					Composite composite=new Composite(parent, SWT.NONE);
//					composite.setLayout(new GridLayout(2, false));
//					new Label(composite, SWT.NONE).setText("Supertype"); //$NON-NLS-1$
//					ComboViewer comboViewer = new ComboViewer(composite);
////					List<String> input=new ArrayList<String>();
////					for(String str: multimap.keySet()) {
////						if(!multimap.containsValue(str)) {
////							input.add(str);
////						}
////					}
//					comboViewer.setContentProvider(ArrayContentProvider.getInstance());
//					comboViewer.setInput(multimap.keySet());
//					comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
//						public void selectionChanged(SelectionChangedEvent event) {
//							viewer.setInput(((IStructuredSelection)event.getSelection()).getFirstElement());
//						}
//					});
//
//					viewer = new TreeViewer(composite);
//					class MyContentProvider implements ITreeContentProvider {
//						public void dispose() {
//							// TODO Auto-generated method stub
//							
//						}
//
//						public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
//							// TODO Auto-generated method stub
//							
//						}
//
//						public Object[] getElements(Object inputElement) {
////							return ((Collection<String>)inputElement).toArray();
////							return multimap.get((String) inputElement).toArray();
//							return getChildren(inputElement);
//						}
//
//						public Object[] getChildren(Object parentElement) {
//							return multimap.get((String) parentElement).toArray();
//						}
//
//						public Object getParent(Object element) {
//							// TODO Auto-generated method stub
//							return null;
//						}
//
//						public boolean hasChildren(Object element) {
//							return multimap.containsKey(element);
//						}
//						
//					}
//					viewer.setContentProvider(new MyContentProvider());
////					viewer.addCheckStateListener(new ICheckStateListener() {
////						public void checkStateChanged(CheckStateChangedEvent event) {
////							viewer.setSubtreeChecked(event.getElement(), event.getChecked());
////						}
////					});
//					viewer.setLabelProvider(new LabelProvider());
//					viewer.getControl().setLayoutData(GridDataFactory.fillDefaults().span(2, 1).grab(true, true).create());
//					new Label(composite, SWT.NONE).setText("Output package");
//					Text text = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
//					text.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
//					text.setText(pkg);
//					new Label(composite, SWT.NONE).setText("Output class name");
//					classname = new Text(composite, SWT.BORDER);
//					classname.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
//					classname.setText("ModelSwitch"); //$NON-NLS-1$
//					return composite;
//				}
//				
//				@Override
//				protected void okPressed() {
////					viewer.getCheckedElements()
//					root[0]=(String) viewer.getInput();
//					clazz[0]=classname.getText();
//					super.okPressed();
//				}
//			};
//			if(dialog.open()==Window.OK) {
//				fragment.createCompilationUnit(clazz[0] + JavaModelUtil.DEFAULT_CU_SUFFIX, StringUtils.EMPTY, true, null);
//				System.out.println("VisitorGenHandler.execute()");
//				
//			}
		} catch (JavaModelException e) {
			throw new ExecutionException("Failed generating visitor pattern", e); //$NON-NLS-1$
		}
		return null;
	}
	
	public String[] getAllFullyQualifiedSuperTypes(IType type) throws JavaModelException {
		String superclassName = type.getSuperclassName();
		String[] superInterfaceNames = type.getSuperInterfaceNames();
		String[] result=new String[superInterfaceNames.length+(superclassName==null ? 0 : 1)];
		int i=0;
		if(superclassName!=null)
			result[i++]=getFullyQualifiedType(type, superclassName);
		
		for(String superInterface: superInterfaceNames) {
			result[i++]=getFullyQualifiedType(type, superInterface);
		}
		
		return result;
	}

	private String getFullyQualifiedType(IType type, String supertype) throws JavaModelException {
		return Joiner.on('.').join(type.resolveType(supertype)[0]);
	}
}
