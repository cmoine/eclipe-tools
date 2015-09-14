package org.eclipse.etools.visitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.etools.SelectionUtils;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.wizards.NewClassWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

public class VisitorWizard extends Wizard {

	private IPackageFragment fragment;
	private Multimap<String, String> multimap= HashMultimap.create();
	private Map<String, String> supertypes=Maps.newHashMap();
	private NewClassWizardPage newClassWizardPage;
	private VisitorGenWizardPage visitorGenWizardPage;

	public VisitorWizard(ExecutionEvent event) throws JavaModelException {
		List<IResource> resources=SelectionUtils.getResources(HandlerUtil.getActiveMenuSelection(event));

        List<ICompilationUnit> cus=SelectionUtils.getCUs(resources);
//        multimap = HashMultimap.create();

        for(ICompilationUnit cu: cus) {
        	IType[] types = cu.getTypes();
        	if(types.length>0) {
        		IType type = types[0];
        		if(fragment==null) {
        			fragment=type.getPackageFragment();
        		}

        		ITypeHierarchy hierarchy = type.newSupertypeHierarchy(new NullProgressMonitor());
        		IType superclass = type;

        		while(true) {
        			String fullyQualifiedName = superclass.getFullyQualifiedName();
        			for(IType t: hierarchy.getSuperInterfaces(superclass)) {
        				multimap.put(t.getFullyQualifiedName(), fullyQualifiedName);
        			}
        			superclass=hierarchy.getSuperclass(superclass);
        			if(superclass==null) {
        				//						multimap.put(Object.class.getName(), fullyQualifiedName);
        				break;
        			}

        			multimap.put(superclass.getFullyQualifiedName(), fullyQualifiedName);
        			if(supertypes.put(fullyQualifiedName, superclass.getFullyQualifiedName())!=null) {
        				System.out.println("VisitorWizard.VisitorWizard()");
        			}
        		}
        	}
        }
	}

	@Override
	public void addPages() {
		visitorGenWizardPage = new VisitorGenWizardPage(multimap);
		addPage(visitorGenWizardPage);
		newClassWizardPage = new NewClassWizardPage() {
			@Override
			protected void createCommentControls(Composite composite, int nColumns) {
			}
		};
		IJavaElement tmp=fragment;
		while(!(tmp instanceof IPackageFragmentRoot)) {
			tmp=tmp.getParent();
		}
		newClassWizardPage.setPackageFragmentRoot((IPackageFragmentRoot) tmp, true);
		newClassWizardPage.setPackageFragment(fragment, true);
		newClassWizardPage.setEnclosingTypeSelection(false, false);
		newClassWizardPage.setMethodStubSelection(false, false, false, false);
		newClassWizardPage.setAddComments(false, false);
		newClassWizardPage.setTypeName("ModelSwitch", true);
		addPage(newClassWizardPage);
	}


//	private void addParameterizedType(IType type) {
//		try {
//			ICompilationUnit cu = type.getCompilationUnit();
////			CompilationUnit parse = CompilationUnitUtil.parse(cu);
//			ASTParser parser=ASTParser.newParser(AST.JLS3);
//			parser.setKind(ASTParser.K_COMPILATION_UNIT);
//			parser.setSource(cu /* "public class ModelSwitch<T> {\n}".toCharArray() */);
//			parser.setResolveBindings(true);
//			CompilationUnit parse = (CompilationUnit) parser.createAST(null); // parse
//			
//			final AST ast = parse.getAST();
//			
//			final TypeDeclaration[] decl=new TypeDeclaration[1];
//			parse.accept(new ASTVisitor() {
//				@Override
//				public boolean visit(TypeDeclaration node) {
//					decl[0]=node;
////					node.resolveBinding().getTypeParameters()
////					TypeParameter typeParameter = ast.newTypeParameter();
////					typeParameter.setName(ast.newSimpleName("T"));
////					node.typeParameters().add(typeParameter);
//					// [ChildProperty[org.eclipse.jdt.core.dom.TypeDeclaration,javadoc], ChildListProperty[org.eclipse.jdt.core.dom.TypeDeclaration,modifiers], 
//					// SimpleProperty[org.eclipse.jdt.core.dom.TypeDeclaration,interface], ChildProperty[org.eclipse.jdt.core.dom.TypeDeclaration,name],
//					// ChildListProperty[org.eclipse.jdt.core.dom.TypeDeclaration,typeParameters], ChildProperty[org.eclipse.jdt.core.dom.TypeDeclaration,superclassType],
//					// ChildListProperty[org.eclipse.jdt.core.dom.TypeDeclaration,superInterfaceTypes], ChildListProperty[org.eclipse.jdt.core.dom.TypeDeclaration,bodyDeclarations]]
////					node.getStructuralProperty(ParameterizedType.TYPE_ARGUMENTS_PROPERTY)getProperty("typeParameters")
//					return super.visit(node);
//				}
//				
//				@Override
//				public boolean visit(ParameterizedType node) {
//					// TODO Auto-generated method stub
//					return super.visit(node);
//				}
//				
////				public boolean visit(org.eclipse.jdt.core.dom.TypeDeclarationStatement node) {
//////					node.set
////					return super.visit(node);
////				}
//			});
//			
////			ast.newTy
////			ParameterizedType parameterizedType = ast.newParameterizedType(ast.newTySimpleType(ast.newName("T")));
////			TypeDeclaration typeDeclaration = ast.newTypeDeclaration();
////			typeDeclaration.
//			
//			
////			decl[0].set
////			rewrite.replace(decl[0], parameterizedType, null);
//			
////			parameterizedType.setType(type);
//			
//			ASTRewrite rewrite = ASTRewrite.create(ast);
//			TypeParameter typeParameter = ast.newTypeParameter();
//			typeParameter.setName(ast.newSimpleName("T"));
//			rewrite.getListRewrite(decl[0], TypeDeclaration.TYPE_PARAMETERS_PROPERTY).insertLast(typeParameter, null);// , typeParameter, null);
//			
//			String source = type.getSource();
//			Document document = new Document(source);
//			
//			TextEdit edits=rewrite.rewriteAST(document, cu.getJavaProject().getOptions(true));
//
//////			decl[0].get
//			
////			ParameterizedType parameterizedType = ast.newParameterizedType(ast.newSimpleType(ast.newName("T")));
//
////			rewrite.getListRewrite(parameterizedType, ParameterizedType.TYPE_ARGUMENTS_PROPERTY).insertLast(typeParameter, null);// , typeParameter, null);
////			rewrite.replace(decl[0], parameterizedType, null);
//			
//			edits.apply(document);
//			String newSource=document.get();
//
//			// update of the compilation unit
//			cu.getBuffer().setContents(newSource);
//		} catch (Exception e) {
//			throw Throwables.propagate(e);
//		}
//	}
	
	@Override
	public boolean performFinish() {
		try {
			IPackageFragment packageFragment = newClassWizardPage.getPackageFragment();
			IPath path = packageFragment.getPath();
			IFolder iFolder=ResourcesPlugin.getWorkspace().getRoot().getFolder(path);
			if(!iFolder.exists())
				iFolder.create(false, true, null);
			String contents=new StringBuffer("package ").append(packageFragment.getElementName()).append(";\n") //  //$NON-NLS-1$//$NON-NLS-2$
					.append('\n')	//
					.append("public class ").append(newClassWizardPage.getTypeName()).append("<T> {\n")  //$NON-NLS-1$ //$NON-NLS-2$
					.append('}').toString();
			ICompilationUnit cu = newClassWizardPage.getPackageFragment().createCompilationUnit(newClassWizardPage.getTypeName()+".java", contents, true, null);
			IType type=cu.getTypes()[0]; 

			List<String> statements=new ArrayList<String>();
			Collection<String> collection = visitorGenWizardPage.getMultimap().get(visitorGenWizardPage.getSuperType());
			for(String t: collection) {
				String methodContent = "\tif(object instanceof " + t + ") {\n" //
						+ "\t\t"+t+" cast=("+t+")object;\n" //
						+ "\t\tT result=null;\n"; //
//						+ "\t\tT result=case"+name+"(cast);\n" //
				String t2=t;
				while(t2!=null && !t2.equals(visitorGenWizardPage.getSuperType())) {
					String name=StringUtils.substringAfterLast(t2, ".");
					methodContent+="\t\tif (result == null) result = case"+name+"(cast);\n"; //
					t2=supertypes.get(t2);
				}
				methodContent += "\t\treturn null;"  //
						+ "\t\n}";
				statements.add(methodContent);
			}
			type.createMethod("public T doSwitch("+visitorGenWizardPage.getSuperType()+" object) {\n"+Joiner.on('\n').join(statements)+"\treturn null;\n}", null, true, null);
			for(String t: collection) {
				String name=StringUtils.substringAfterLast(t, ".");
				String methodContent = "protected T case"+name+"("+t+" object) {\n"	//
						+"\treturn null;\n"	//
						+"}";
				type.createMethod(methodContent, null, true, null);
			}
			type.createMethod("protected T caseDefault() {\n\treturn null;\n}", null, true, null);
			
			IResource resource = cu.getCorrespondingResource();
			if (resource != null) {
				selectAndReveal(resource);
				IProject fProject = resource.getProject();
				if (fProject.hasNature(JavaCore.NATURE_ID)) {
					IJavaProject jProject = JavaCore.create(fProject);
					IJavaElement jElement = jProject.findElement(resource.getProjectRelativePath().removeFirstSegments(1));
					if (jElement != null)
						JavaUI.openInEditor(jElement);
				} else if (resource instanceof IFile) {
					IWorkbenchPage page = PDEPlugin.getActivePage();
					IDE.openEditor(page, (IFile) resource, true);
				}
			}
		} catch (CoreException e) {
			PDEPlugin.logException(e);
		}
		return true;
	}

	protected void selectAndReveal(IResource newResource) {
		BasicNewResourceWizard.selectAndReveal(newResource, PDEPlugin.getActiveWorkbenchWindow());
	}
}
