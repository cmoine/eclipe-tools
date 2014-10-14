package org.eclipse.etools.ei18n.extensions;

import java.util.Collection;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.etools.Activator;
import org.eclipse.etools.ei18n.EI18NStartup;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.template.java.CodeTemplateContext;
import org.eclipse.jdt.internal.corext.template.java.CodeTemplateContextType;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.templates.Template;

import com.google.common.collect.Sets;

public abstract class DefaultJavaMapping implements IJavaMapping {
    private ICompilationUnit cu;
    private IDocument document;

    public void init(IDocument document, IFile javaFile) {
        this.document=document;
        if (javaFile != null) {
            cu=(ICompilationUnit) JavaCore.create(javaFile);
        } else {
            cu=null;
        }
        //        this.cu=cu;
    }

    public Set<String> getKeys() {
        return getFields(true);
    }

    public Set<String> getFields(boolean valid) {
        Set<String> result=Sets.newHashSet();
        if (cu != null) {
            try {
                for (IType type : cu.getAllTypes()) {
                    for (IField field : type.getFields()) {
                        if (!valid || isValid(field)) {
                            result.add(field.getElementName());
                        }
                    }
                }
            } catch (JavaModelException e1) {
                Activator.logError("Failed to load CompilationUnit", e1); //$NON-NLS-1$
            }
        }
        return result;
    }

    protected boolean isValid(IField field) throws JavaModelException {
        //        System.out.println("DefaultJavaMapping.isValid() " + field.getElementName() + " " + field.getSource());
        return "QString;".equals(field.getTypeSignature()) && !Flags.isFinal(field.getFlags()) && Flags.isStatic(field.getFlags()) /* && checkInitializers(DeclaringType().getInitializers())*/; //$NON-NLS-1$
    }

    //    protected abstract boolean checkInitializers(IInitializer[] initializers);

    protected boolean isValid(FieldDeclaration field) {
        if (!(field.getType() instanceof SimpleType))
            return false;

        SimpleType type=(SimpleType) field.getType();
        return "String".equals(type.toString()) && !Flags.isFinal(field.getModifiers()) && Flags.isStatic(field.getModifiers()); //$NON-NLS-1$
    }

    public void syncFields(Collection<String> newFields, Collection<String> fieldsToRemove) {
        if (!newFields.isEmpty() || !fieldsToRemove.isEmpty()) {
            Set<String> fields=getFields(false);

            ASTParser parser=ASTParser.newParser(AST.JLS4);
            parser.setKind(ASTParser.K_COMPILATION_UNIT);
            parser.setResolveBindings(true);
            parser.setSource(cu);
            CompilationUnit cu=(CompilationUnit) parser.createAST(null); // parse

            final AST ast=cu.getAST();
            final ASTRewrite rewrite=ASTRewrite.create(ast);

            for (String newField : newFields) {
                if (newField != null) {
                    if (fields.contains(newField))
                        removeField(cu, ast, rewrite, newField); // Remove the previous one that is not valid
                    addField(cu, ast, rewrite, newField);
                }
            }

            for (String fieldToRemove : fieldsToRemove) {
                removeField(cu, ast, rewrite, fieldToRemove);
            }

            try {
                rewrite.rewriteAST(document, cu.getJavaElement().getJavaProject().getOptions(true)).apply(document);
            } catch (BadLocationException e) {
                Activator.logError("Failed rewriting AST", e); //$NON-NLS-1$
                throw new RuntimeException(e);
            }
            //            rewriteAst(cu, rewrite, fieldName);
            //            return rewrite.rewriteAST(document, cu.getJavaElement().getJavaProject().getOptions(true));
        }
    }

    protected void addField(final CompilationUnit cu, final AST ast, final ASTRewrite rewrite, final String fieldName) {
        cu.accept(new ASTVisitor() {

            @Override
            public boolean visit(TypeDeclaration node) {
                VariableDeclarationFragment variableDeclarationFragment=ast.newVariableDeclarationFragment();
                variableDeclarationFragment.setName(ast.newSimpleName(fieldName));

                FieldDeclaration newFieldDeclaration=createFieldDeclaration(cu, fieldName);

                //                FieldDeclaration newFieldDeclaration=ast.newFieldDeclaration(variableDeclarationFragment);
                //                newFieldDeclaration.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
                //                newFieldDeclaration.modifiers().add(ast.newModifier(ModifierKeyword.STATIC_KEYWORD));
                //                newFieldDeclaration.setType(ast.newSimpleType(ast.newSimpleName("String"))); //$NON-NLS-1$

                ListRewrite listRewrite=rewrite.getListRewrite(node, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
                FieldDeclaration[] fields=node.getFields();
                if (fields == null || fields.length == 0)
                    listRewrite.insertFirst(newFieldDeclaration, null);
                else
                    listRewrite.insertAfter(newFieldDeclaration, fields[fields.length - 1], null);

                return true;
            }

            private FieldDeclaration createFieldDeclaration(CompilationUnit cu, String fieldName) {
                try {
                    IJavaProject javaProject=cu.getTypeRoot().getJavaProject();
                    String lineDelimiter=StubUtility.getLineDelimiterUsed(javaProject);

                    Template template=StubUtility.getCodeTemplate(EI18NStartup.EI18N_FIELD_ID, javaProject);
                    CodeTemplateContext context=new CodeTemplateContext(template.getContextTypeId(), javaProject, lineDelimiter);

                    //                context.setVariable(CodeTemplateContextType.PACKAGENAME, javaElt.getElementName());
                    //                context.setVariable(CodeTemplateContextType.PROJECTNAME, JavaCore.create(folder.getProject()).getElementName());
                    context.setVariable(CodeTemplateContextType.FIELD, fieldName);

                    String body="public class Fake {" + context.evaluate(template).getString() + '}';

                    ASTParser parser=ASTParser.newParser(AST.JLS4);
                    parser.setKind(ASTParser.K_COMPILATION_UNIT);
                    parser.setResolveBindings(false);
                    parser.setSource(body.toCharArray());
                    parser.setUnitName("fake");
                    CompilationUnit compilationUnit=(CompilationUnit) parser.createAST(null);
                    final FieldDeclaration[] decls=new FieldDeclaration[] { null };
                    compilationUnit.accept(new ASTVisitor() {

                        @Override
                        public boolean visit(FieldDeclaration node) {
                            decls[0]=node;
                            return super.visit(node);
                        }
                    });
                    return decls[0];
                } catch (Exception e) {
                    Activator.logError("Failed creating field declaration", e); //$NON-NLS-1$
                }

                return null;
            }
        });
    }

    protected void removeField(CompilationUnit cu, final AST ast, final ASTRewrite rewrite, final String fieldName) {
        cu.accept(new ASTVisitor() {

            @Override
            public boolean visit(TypeDeclaration node) {
                ListRewrite listRewrite=rewrite.getListRewrite(node, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
                for (FieldDeclaration field : node.getFields()) {
                    if (isValid(field) && fieldName.equals(((VariableDeclarationFragment) field.fragments().get(0)).getName().getIdentifier())) {
                        listRewrite.remove(field, null);
                        break;
                    }
                }

                return true;
            }
        });

        //        rewriteAst(cu, rewrite, fieldName);
    }
}
