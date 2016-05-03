package org.eclipse.etools.jpa;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.ui.dialogs.WorkspaceResourceDialog;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.etools.SelectionUtils;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import com.google.common.collect.Maps;

public class EcoreGeneration extends AbstractHandler {
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			IFile file=WorkspaceResourceDialog.openNewFile(HandlerUtil.getActiveShellChecked(event), "JPA -> Ecore", "Please select a destination file", new Path("ecore.ecore"), null);
			if(file!=null) {
				if(file.exists()) {
					if(!MessageDialog.openConfirm(HandlerUtil.getActiveShellChecked(event), "JPA -> Ecore", "Do you want to overwrite the file ?"))
						return null;
					file.delete(true, null);
				}
				ResourceSet rSet=new ResourceSetImpl();
				final Resource res=rSet.createResource(URI.createFileURI(file.getName()));

				final Map<String, EClassItem> map=createStructure(event, res);

				final List<Runnable> opposites=new ArrayList<Runnable>();
				for(final EClassItem item: map.values()) {
					for(final FieldItem fieldItem: item.fields) {
						final IField field=fieldItem.field;
//						if(Flags.isStatic(field.getFlags()) || Flags.isFinal(field.getFlags()))
//							continue;

//						final String typeSignature = Signature.getSignatureSimpleName(field.getTypeSignature());
//						String[][] resolveType = item.type.resolveType(typeSignature);
//						final IType type=resolveType==null ? null : item.cu.getJavaProject().findType(resolveType[0][0] + '.' + resolveType[0][1]);

						final IAnnotation manyToOne = field.getAnnotation("ManyToOne");
						final IAnnotation oneToMany = field.getAnnotation("OneToMany");
						if(exists(manyToOne) || exists(oneToMany)) {
							final EReference ref=EcoreFactory.eINSTANCE.createEReference();
							ref.setLowerBound(0);
							final EClassItem target = map.get(fieldItem.typeSignature);
							if(exists(oneToMany)) {
								for(final IMemberValuePair pair: oneToMany.getMemberValuePairs()) {
									if("mappedBy".equals(pair.getMemberName())) {
										opposites.add(new Runnable() {
											public void run() {
												ref.setEOpposite((EReference) ((EClass) target.clazz).getEStructuralFeature((String) pair.getValue()));
											}
										});
										pair.getValue();
									}
								}
							}
							ref.setUpperBound(exists(oneToMany) ? EReference.UNBOUNDED_MULTIPLICITY : 1);
							ref.setName(field.getElementName());
							ref.setEType(target.clazz);
							((EClass) item.clazz).getEStructuralFeatures().add(ref);
							continue;
						}

						EAttribute att=EcoreFactory.eINSTANCE.createEAttribute();
						att.setName(field.getElementName());
						((EClass) item.clazz).getEStructuralFeatures().add(att);
						if(String.class.getName().equals(fieldItem.typeSignature)) {
							att.setEType(EcorePackage.eINSTANCE.getEString());
						} else if(Date.class.getName().equals(fieldItem.typeSignature) || java.sql.Date.class.getName().equals(fieldItem.typeSignature)) {
							att.setEType(EcorePackage.eINSTANCE.getEDate());
						} else if(int.class.getName().equals(fieldItem.typeSignature)) {
							att.setEType(EcorePackage.eINSTANCE.getEInt());
						} else if(Integer.class.getName().equals(fieldItem.typeSignature)) {
							att.setEType(EcorePackage.eINSTANCE.getEIntegerObject());
						} else if(long.class.getName().equals(fieldItem.typeSignature)) {
							att.setEType(EcorePackage.eINSTANCE.getELong());
						} else if(Long.class.getName().equals(fieldItem.typeSignature)) {
							att.setEType(EcorePackage.eINSTANCE.getELongObject());
						} else if(boolean.class.getName().equals(fieldItem.typeSignature)) {
							att.setEType(EcorePackage.eINSTANCE.getEBoolean());
						} else if(Boolean.class.getName().equals(fieldItem.typeSignature)) {
							att.setEType(EcorePackage.eINSTANCE.getEBooleanObject());
						} else if(fieldItem.type.isEnum()) {
							att.setEType(fieldItem.e);
//							EEnum clazz = EcoreFactory.eINSTANCE.createEEnum();
//							clazz.setName(type.getElementName());
//							IField[] fields = type.getFields();
//							for (int i = 0; i < fields.length; i++) {
//								IField value = fields[i];
//								EEnumLiteral literal=EcoreFactory.eINSTANCE.createEEnumLiteral();
//								literal.setName(value.getElementName());
//								literal.setLiteral(value.getElementName());
//								literal.setValue(i);
//							}
							
						} else
							throw new UnsupportedOperationException("Unsupported type: " + fieldItem.typeSignature);
					}
				}
				runAll(opposites);

				ByteArrayOutputStream fos=new ByteArrayOutputStream();
				res.save(fos, null);
				file.create(new ByteArrayInputStream(fos.toByteArray()), true, null);
			}
		} catch (Throwable e) {
			throw new ExecutionException("Internal error", e); //$NON-NLS-1$
		}
		return null;
	}

	private Map<String, EClassItem> createStructure(ExecutionEvent event, final Resource res) throws ExecutionException, JavaModelException {
		final Map<String, EClassItem> map=Maps.newHashMap();
		List<Runnable> inheritance=new ArrayList<Runnable>();
		for(final ICompilationUnit cu: SelectionUtils.getCUs(SelectionUtils.getResources(HandlerUtil.getCurrentSelectionChecked(event)))) {
			IType type = cu.getAllTypes()[0];
			final EPackage pkg=getPackage(res, type.getPackageFragment());
			final EClass clazz = EcoreFactory.eINSTANCE.createEClass();
			clazz.setInterface(type.isInterface());
			clazz.setAbstract(Flags.isAbstract(type.getFlags()) || type.isInterface());
			clazz.setName(type.getElementName());
			EClassItem item = new EClassItem(cu, type, clazz);
			map.put(type.getFullyQualifiedName(), item);
			
			for(IField field: type.getFields()) {
				if(Flags.isStatic(field.getFlags()) || Flags.isFinal(field.getFlags()))
					continue;

				FieldItem fieldItem = new FieldItem(field);
				if(fieldItem.type!=null && fieldItem.type.isEnum()) {
					EEnum e = EcoreFactory.eINSTANCE.createEEnum();
					e.setName(type.getElementName());
					IField[] fields = type.getFields();
					for (int i = 0; i < fields.length; i++) {
						IField value = fields[i];
						EEnumLiteral literal=EcoreFactory.eINSTANCE.createEEnumLiteral();
						literal.setName(value.getElementName());
						literal.setLiteral(value.getElementName());
						literal.setValue(i);
						e.getELiterals().add(literal);
					}
					fieldItem.e=e;
					getPackage(res, fieldItem.type.getPackageFragment()).getEClassifiers().add(e);
					map.put(fieldItem.typeSignature, new EClassItem(cu, type, e));
				}
				item.fields.add(fieldItem);
			}

			if(type.getAnnotation("Entity")!=null) {
				final List<String> supertypes=new ArrayList<String>();
				if(type.getSuperclassName()!=null) {
					String[][] resolveType = type.resolveType(type.getSuperclassName());
					supertypes.add(resolveType[0][0] + '.' + resolveType[0][1]);
				}
				for(String s: type.getSuperInterfaceNames()) {
					String[][] resolveType = type.resolveType(s);
					supertypes.add(resolveType[0][0] + '.' + resolveType[0][1]);
				}
				if(!supertypes.isEmpty()) {
					inheritance.add(new Runnable() {
						public void run() {
							for(String s: supertypes) {
								EClassItem e = map.get(s);
								if(e==null) {
									System.out.println("EcoreGeneration.execute(...).new Runnable() {...}.run() " + s);
								} else {
									if(e.clazz.eResource()==null)
										pkg.getEClassifiers().add(e.clazz);
									clazz.getESuperTypes().add((EClass) e.clazz);
								}
							}
						}
					});
				}
				pkg.getEClassifiers().add(clazz);
			}
		}

		runAll(inheritance);

		return map;
	}

	private EPackage getPackage(Resource res, IPackageFragment packageFragment) {
		List list=res.getContents();
		EPackage result=null;
		for(String str: StringUtils.split(packageFragment.getElementName(), '.')) {
			EPackage pkg=null;
			for(Object o: list) {
				if(o instanceof EPackage && ((EPackage)o).getName().equals(str))
					pkg=(EPackage) o;
			}
			if(pkg==null) {
				pkg=EcoreFactory.eINSTANCE.createEPackage();
				pkg.setName(str);
				list.add(pkg);
			}
			result=pkg;
			list=pkg.getESubpackages();
		}
		return result;
	}

	private void runAll(List<Runnable> references) {
		for(Runnable runnable: references) {
			runnable.run();
		}
	}

	private boolean exists(IAnnotation annotation) {
		return annotation!=null && annotation.exists();
	}
}
