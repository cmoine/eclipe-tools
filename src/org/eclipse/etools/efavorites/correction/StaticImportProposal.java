package org.eclipse.etools.efavorites.correction;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.etools.Activator;
import org.eclipse.etools.efavorites.EFavoritesImage;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
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

    public StaticImportProposal(IInvocationContext context) {
        super(new JavaContentAssistInvocationContext(context.getCompilationUnit()));
        this.context=context;
    }

    private ImportRewrite createImportRewrite() {
        if (context.getCompilationUnit() != null && !isInJavadoc()) {
            try {
                CompilationUnit cu=getASTRoot(context.getCompilationUnit());
                SimpleName coveringNode=(SimpleName) context.getCoveringNode();
                //            QualifiedName node=(QualifiedName) coveringNode.getParent();
                String qualifier=null;
                String name=null;
                //                String str;
                if (coveringNode.getParent() instanceof MethodInvocation) {
                    String str=((Name) ((MethodInvocation) coveringNode.getParent()).getExpression()).getFullyQualifiedName();
                    IType type=context.getCompilationUnit().getAllTypes()[0];
                    String[][] result=type.resolveType(str);
                    if (result != null && result.length > 0) {
                        qualifier=result[0][0] + '.' + result[0][1];
                        name=coveringNode.getIdentifier();
                    }
                    //                IType type=context.getCompilationUnit().getJavaModel().getgetType(str);
                    //                if (!type.exists()) {
                    //                    type=context.getCompilationUnit().getType("java.lang." + str);
                    //                    type.resolveType(typeName)
                    //                    JavaCore.create
                    //                }
                    //                type.getFullyQualifiedName();
                } else {
                    qualifier=((SimpleName) ((QualifiedName) coveringNode.getParent()).getQualifier()).getIdentifier();
                    name=coveringNode.getIdentifier();
                }

                if (cu == null) {
                    rewrite=StubUtility.createImportRewrite(cu, true);
                    //                rewrite.addStaticImport(qualifiedTypeName, String.valueOf(fProposal.getName()), proposalKind == CompletionProposal.FIELD_IMPORT, context);
                    return rewrite;
                } else {
                    rewrite=StubUtility.createImportRewrite(cu, true);
                    ContextSensitiveImportRewriteContext importContext=new ContextSensitiveImportRewriteContext(cu, fInvocationContext.getInvocationOffset(),
                            rewrite);
                    //                importContext.findInContext("Math", "abs", ImportRewriteContext.KIND_STATIC_METHOD);
                    rewrite.addStaticImport(qualifier, name, true, importContext);
                    //                    fImportContext=new ContextSensitiveImportRewriteContext(cu, fInvocationContext.getInvocationOffset(), rewrite);
                    return rewrite;
                }
            } catch (CoreException x) {
                Activator.log(IStatus.ERROR, "Failed to add static import", x); //$NON-NLS-1$
            }
        }
        return null;
    }

    @Override
    public boolean validate(IDocument document, int offset, DocumentEvent event) {
        fImportRewrite=createImportRewrite();
        return true;
    }

    @Override
    public void apply(IDocument document, char trigger, int offset) {
        try {
            //            super.apply(document, trigger, offset);

            if (fImportRewrite != null && fImportRewrite.hasRecordedChanges()) {
                int oldLen=document.getLength();
                fImportRewrite.rewriteImports(new NullProgressMonitor()).apply(document, TextEdit.UPDATE_REGIONS);
                setReplacementOffset(getReplacementOffset() + document.getLength() - oldLen);

                //                ASTRewrite rewrite=ASTRewrite.create(getASTRoot(context.getCompilationUnit()));

            }
        } catch (Exception e) {
            Activator.log(IStatus.ERROR, "Failed applying", e); //$NON-NLS-1$
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
