package org.eclipse.etools.efavorites.correction;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.etools.Activator;
import org.eclipse.etools.efavorites.EFavoritesImage;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.text.edits.TextEdit;

public class StaticImportProposal extends AbstractJavaCompletionProposal {
    private ImportRewrite fImportRewrite;
    private ImportRewrite rewrite;
    private final IInvocationContext context;
    private SimpleName coveringNode;

    public StaticImportProposal(IInvocationContext context) {
        super(new JavaContentAssistInvocationContext(context.getCompilationUnit()));
        this.context=context;
    }

    private ImportRewrite createImportRewrite() {
        rewrite=null;
        if (context.getCompilationUnit() != null && !isInJavadoc()) {
            try {
                CompilationUnit cu=getASTRoot(context.getCompilationUnit());
                coveringNode=(SimpleName) context.getCoveringNode();

                String qualifier=null;
                String name=null;
                String str;
                if (coveringNode.getParent() instanceof MethodInvocation) {
                    str=((Name) ((MethodInvocation) coveringNode.getParent()).getExpression()).getFullyQualifiedName();
                } else {
                    str=((SimpleName) ((QualifiedName) coveringNode.getParent()).getQualifier()).getIdentifier();
                }

                IType type=context.getCompilationUnit().getAllTypes()[0];
                String[][] result=type.resolveType(str);
                if (result != null && result.length > 0) {
                    qualifier=result[0][0] + '.' + result[0][1];
                    name=coveringNode.getIdentifier();

                    rewrite=StubUtility.createImportRewrite(cu, true);
                    ContextSensitiveImportRewriteContext importContext=new ContextSensitiveImportRewriteContext(cu, fInvocationContext.getInvocationOffset(),
                            rewrite);
                    rewrite.addStaticImport(qualifier, name, true, importContext);
                    return rewrite;
                }
            } catch (CoreException x) {
                Activator.logError("Failed to add static import", x); //$NON-NLS-1$
            }
        }
        return null;
    }

    @Override
    public boolean validate(IDocument document, int offset, DocumentEvent event) {
        fImportRewrite=createImportRewrite();
        return rewrite != null;
    }

    @Override
    public void apply(IDocument document, char trigger, int offset) {
        try {
            if (fImportRewrite != null && fImportRewrite.hasRecordedChanges()) {
                AST ast=coveringNode.getParent().getAST();
                ASTRewrite rewrite=ASTRewrite.create(ast);
                if (coveringNode.getParent() instanceof MethodInvocation) {
                    MethodInvocation mi=(MethodInvocation) coveringNode.getParent();

                    MethodInvocation newMethodInvocation=ast.newMethodInvocation();
                    newMethodInvocation.setName(ast.newSimpleName(mi.getName().getIdentifier()));
                    newMethodInvocation.arguments().addAll(ASTNode.copySubtrees(newMethodInvocation.getAST(), mi.arguments()));
                    rewrite.replace(coveringNode.getParent(), newMethodInvocation, null);
                } else {
                    QualifiedName qName=(QualifiedName) coveringNode.getParent();

                    SimpleName sName=ast.newSimpleName(qName.getName().getIdentifier());
                    rewrite.replace(coveringNode.getParent(), sName, null);
                }

                TextEdit edits=rewrite.rewriteAST(document, context.getCompilationUnit().getJavaProject().getOptions(true));

                // computation of the new source code
                edits.apply(document);

                fImportRewrite.rewriteImports(new NullProgressMonitor()).apply(document, TextEdit.UPDATE_REGIONS);

                // update of the compilation unit
                String newSource=document.get();

                // update of the compilation unit
                context.getCompilationUnit().getBuffer().setContents(newSource);
            }
        } catch (Exception e) {
            Activator.logError("Failed applying", e); //$NON-NLS-1$
        }
    }

    private CompilationUnit getASTRoot(ICompilationUnit compilationUnit) {
        return SharedASTProvider.getAST(compilationUnit, SharedASTProvider.WAIT_NO, null);
    }

    @Override
    public Point getSelection(IDocument document) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getAdditionalProposalInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDisplayString() {
        return "Use static import";
    }

    @Override
    public StyledString getStyledDisplayString() {
        return new StyledString().append(getDisplayString());
    }

    @Override
    public Image getImage() {
        return EFavoritesImage.STATIC_IMPORT_16.getImage();
    }

    @Override
    public IContextInformation getContextInformation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getRelevance() {
        return 0;
    }
}
