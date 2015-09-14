package org.eclipse.etools.ei18n.actions;

import java.util.List;

import org.apache.commons.lang3.mutable.MutableObject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.etools.Activator;
import org.eclipse.etools.RemoveMe;
import org.eclipse.etools.util.CompilationUnitUtil;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.window.Window;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import com.google.common.collect.Lists;

@RemoveMe
public class MigrateNLSAction extends CompilationUnitAction {
	public MigrateNLSAction() {
		super("Old -> New NLS system"); //$NON-NLS-1$
	}

	@Override
	public void run() {
		List<ICompilationUnit> cus=getCompilationUnits();

		if (cus != null) {
			InputDialog dialog=new InputDialog(getShell(), "Nom du bundle", "Veuillez entrer le nom du bundle � utiliser", "Messages", null); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			if (dialog.open() == Window.OK) {
				for (ICompilationUnit cu : cus) {
					try {
						// ICompilationUnit cu=(ICompilationUnit) selection;
						String source=cu.getBuffer().getContents();
						Document document=new Document(source);

						CompilationUnit parse=CompilationUnitUtil.parse(cu);
						final List<MethodInvocation> nodes=Lists.newArrayList();
						final MutableObject<PackageDeclaration> pkgDecl=new MutableObject<PackageDeclaration>();
						// MethodVisitor visitor = new MethodVisitor();
						parse.accept(new ASTVisitor() {
							@Override
							public boolean visit(MethodInvocation node) {
								if (node.getName().getFullyQualifiedName().equals("getString") // && node.arguments().size() == 1 //$NON-NLS-1$
										// && (((Expression) node.arguments().get(0)) instanceof StringLiteral)
										&& node.resolveTypeBinding().getBinaryName().equals(String.class.getName())) {
									List<Expression> arguments=node.arguments();
									if (arguments.size() == 1 || arguments.size() == 3) {
										for (Expression expr : arguments) {
											if (!(expr instanceof StringLiteral)) {
												return true;
											}
										}
										nodes.add(node);
									}
								}
								return true;
							}

							@Override
							public boolean visit(PackageDeclaration node) {
								pkgDecl.setValue(node);
								return true;
							}
						});
						AST ast=parse.getAST();
						ASTRewrite rewrite=ASTRewrite.create(ast);
						// type.setType(ast.newSimpleType(ast.newSimpleName("Messages")));
						for (MethodInvocation minv : nodes) {
							ASTNode newNode;
							if (minv.arguments().size() > 1) {
								MethodInvocation newMethodInvocation=ast.newMethodInvocation();
								newMethodInvocation.setExpression(ast.newQualifiedName(ast.newName("java.text"), ast.newSimpleName("MessageFormat"))); //$NON-NLS-1$ //$NON-NLS-2$
								newMethodInvocation.setName(ast.newSimpleName("format")); //$NON-NLS-1$
								for (Expression expr : (List<Expression>) minv.arguments()) {
									newMethodInvocation.arguments().add(createFieldAccess(ast, pkgDecl, (StringLiteral) expr, dialog.getValue()));
								}
								newNode=newMethodInvocation;
							} else {
								StringLiteral stringLiteral=(StringLiteral) minv.arguments().get(0);
								FieldAccess fieldAccess=createFieldAccess(ast, pkgDecl, stringLiteral, dialog.getValue());
								newNode=fieldAccess;
							}
							rewrite.replace(minv, newNode, null);
						}
						// computation of the text edits
						TextEdit edits=rewrite.rewriteAST(document, cu.getJavaProject().getOptions(true));

						// computation of the new source code
						edits.apply(document);
						String newSource=document.get();

						// update of the compilation unit
						cu.getBuffer().setContents(newSource);
					} catch (CoreException e) {
                        Activator.logError("Failed to patch " + cu.getResource(), e); //$NON-NLS-1$
					} catch (MalformedTreeException e) {
                        Activator.logError("Failed to parse " + cu.getResource(), e); //$NON-NLS-1$
					} catch (BadLocationException e) {
                        Activator.logError("Failed to modify " + cu.getResource(), e); //$NON-NLS-1$
					}
				}
				return;
			}
		}
		MessageDialog.openError(getShell(), "Error", "This selection is not supproted"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected FieldAccess createFieldAccess(AST ast, final MutableObject<PackageDeclaration> pkgDecl, StringLiteral stringLiteral, String bundleName) {
		FieldAccess fieldAccess=ast.newFieldAccess();
		Expression type=ast.newQualifiedName(ast.newName(pkgDecl.getValue().getName().getFullyQualifiedName()), ast.newSimpleName(bundleName));
		fieldAccess.setExpression(type);
		fieldAccess.setName(ast.newSimpleName(stringLiteral.getLiteralValue()));
		return fieldAccess;
	}
}
