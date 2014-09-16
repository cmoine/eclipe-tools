package org.eclipse.etools.ei18n.util;



public final class CompilationUnitUtil {
	private CompilationUnitUtil() {
	}

    //	public static List<ICompilationUnit> getCompilationUnits(IStructuredSelection selection) throws JavaModelException {
    //		// try {
    //		List<ICompilationUnit> cus=Lists.newArrayList();
    //		for (Iterator<Object> it=selection.iterator(); it.hasNext();) {
    //			Object element=it.next();
    //			if ((element instanceof IJavaElement))
    //				cus.addAll(collectCompilationUnits((IJavaElement) element));
    //		}
    //		return cus;
    //		// } catch (JavaModelException e) {
    //		// ErrorDialog.openError(this.part.getSite().getShell(), "Find Unused Members", "Problem collecting compilation units", e.getStatus());
    //		// return;
    //		// }
    //	}
    //
    //	private static List<ICompilationUnit> collectCompilationUnits(IJavaElement current) throws JavaModelException {
    //		List<ICompilationUnit> cus=Lists.newArrayList();
    //		if (((current instanceof IJavaProject)) || ((current instanceof IPackageFragmentRoot)) || (current instanceof IPackageFragment)) {
    //			IJavaElement[] children=((IParent) current).getChildren();
    //			// for (int i=0; i < children.length; i++)
    //			// collectCompilationUnits(children[i], res);
    //			// } else if ((current instanceof IPackageFragment)) {
    //			// IJavaElement[] children=((IParent) current).getChildren();
    //			for (int i=0; i < children.length; i++) {
    //				List<ICompilationUnit> compilationUnits=collectCompilationUnits(children[i]);
    //				if (compilationUnits == null)
    //					return null;
    //				else
    //					cus.addAll(compilationUnits);
    //			}
    //		} else if ((current instanceof ICompilationUnit)) {
    //			cus.add((ICompilationUnit) current);
    //		} else
    //			return null;
    //
    //		return cus;
    //	}
    //
    //    public static boolean isValid(ICompilationUnit cu) {
    //        try {
    //            return cu.getTypes().length == 1 && EI18NConstants.NLS_CLASS_NAME.equals(cu.getTypes()[0].getSuperclassName());
    //        } catch (Exception e) {
    //            Activator.log(IStatus.ERROR, "Failed to analyze Compulation Unit " + cu, e); //$NON-NLS-1$
    //        }
    //        return false;
    //    }
    //
    //    public static void addField(ICompilationUnit icu, final String key) throws JavaModelException, MalformedTreeException, BadLocationException {
    //        IBuffer buffer=icu.getBuffer();
    //        String source=buffer.getContents();
    //        Document document=new Document(source);
    //        TextEdit edits=addField(document, icu, key);
    //        // computation of the new source code
    //        edits.apply(document);
    //        String newSource=document.get();
    //
    //        // update of the compilation unit
    //        buffer.setContents(newSource);
    //
    //        buffer.save(new NullProgressMonitor(), false);
    //    }
    //
    //    public static TextEdit addField(IDocument document, ICompilationUnit icu, final String key) throws JavaModelException, MalformedTreeException,
    //            BadLocationException {
    //        ASTParser parser=ASTParser.newParser(AST.JLS4);
    //        parser.setKind(ASTParser.K_COMPILATION_UNIT);
    //        parser.setResolveBindings(true);
    //        parser.setSource(icu);
    //        CompilationUnit cu=(CompilationUnit) parser.createAST(null); // parse
    //
    //        final AST ast=cu.getAST();
    //        final ASTRewrite rewrite=ASTRewrite.create(ast);
    //    
    //        cu.accept(new ASTVisitor() {
    //            @Override
    //            public boolean visit(TypeDeclaration node) {
    //                VariableDeclarationFragment variableDeclarationFragment=ast.newVariableDeclarationFragment();
    //                variableDeclarationFragment.setName(ast.newSimpleName(key));
    //                FieldDeclaration newFieldDeclaration=ast.newFieldDeclaration(variableDeclarationFragment);
    //                newFieldDeclaration.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
    //                newFieldDeclaration.modifiers().add(ast.newModifier(ModifierKeyword.STATIC_KEYWORD));
    //                newFieldDeclaration.setType(ast.newSimpleType(ast.newSimpleName("String"))); //$NON-NLS-1$
    //    
    //                ListRewrite listRewrite=rewrite.getListRewrite(node, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
    //                FieldDeclaration[] fields=node.getFields();
    //                if (fields == null || fields.length == 0)
    //                    listRewrite.insertFirst(newFieldDeclaration, null);
    //                else
    //                    listRewrite.insertAfter(newFieldDeclaration, fields[fields.length - 1], null);
    //    
    //                return true;
    //            }
    //        });
    //    
    //        return rewrite.rewriteAST(document, cu.getJavaElement().getJavaProject().getOptions(true));
    //    }
    //
    //    public static boolean isValid(IField field) throws JavaModelException {
    //        return "QString;".equals(field.getTypeSignature()) && !Flags.isFinal(field.getFlags()) && Flags.isStatic(field.getFlags()); //$NON-NLS-1$
    //    }
    //
    //    public static boolean isValid(FieldDeclaration field) {
    //        if (!(field.getType() instanceof SimpleType))
    //            return false;
    //
    //        SimpleType type=(SimpleType) field.getType();
    //        return "String".equals(type.toString()) && !Flags.isFinal(field.getModifiers()) && Flags.isStatic(field.getModifiers()); //$NON-NLS-1$
    //    }
    //
    //    public static TextEdit removeField(IDocument document, ICompilationUnit icu, final String key) throws JavaModelException, MalformedTreeException,
    //            BadLocationException {
    //        ASTParser parser=ASTParser.newParser(AST.JLS4);
    //        parser.setKind(ASTParser.K_COMPILATION_UNIT);
    //        parser.setResolveBindings(true);
    //        parser.setSource(icu);
    //        CompilationUnit cu=(CompilationUnit) parser.createAST(null); // parse
    //
    //        final AST ast=cu.getAST();
    //        final ASTRewrite rewrite=ASTRewrite.create(ast);
    //
    //        cu.accept(new ASTVisitor() {
    //            @Override
    //            public boolean visit(TypeDeclaration node) {
    //                ListRewrite listRewrite=rewrite.getListRewrite(node, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
    //                for (FieldDeclaration field : node.getFields()) {
    //                    if (isValid(field) && key.equals(((VariableDeclarationFragment) field.fragments().get(0)).getName().getIdentifier())) {
    //                        listRewrite.remove(field, null);
    //                        break;
    //                    }
    //                }
    //
    //                return true;
    //            }
    //        });
    //
    //        return rewrite.rewriteAST(document, cu.getJavaElement().getJavaProject().getOptions(true));
    //    }
}
