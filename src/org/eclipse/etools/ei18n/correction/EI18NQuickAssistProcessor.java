package org.eclipse.etools.ei18n.correction;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;

public class EI18NQuickAssistProcessor implements IQuickAssistProcessor {
    public boolean hasAssists(IInvocationContext context) throws CoreException {
        ASTNode node=context.getCoveringNode();
        return node instanceof StringLiteral;
    }

    public IJavaCompletionProposal[] getAssists(IInvocationContext context, IProblemLocation[] locations) throws CoreException {
        if (hasAssists(context)) {
            //            String label=MessageFormat.format("Externalize \"{0}\"", ((StringLiteral) context.getCoveringNode()).getLiteralValue());
//            EI18NRefactoring refactoring=new EI18NRefactoring(context, label);
//            if (refactoring.checkInitialConditions(null).isOK()) {
//                LinkedProposalModel linkedProposalModel=new LinkedProposalModel();
//                refactoring.setLinkedProposalModel(linkedProposalModel);
//
//                RefactoringCorrectionProposal proposal=new RefactoringCorrectionProposal(label, context.getCompilationUnit(), refactoring, 0,
//                        EI18NImage.LOGO_16.getImage()) {
//                    @Override
//                    protected void init(Refactoring refactoring) throws CoreException {
//                        EI18NRefactoring etr=(EI18NRefactoring) refactoring;
//                        // TODO
//                        //                        etr.setConstantName(etr.guessConstantName()); // expensive
//                    }
//                };
//                proposal.setCommandId(ADD_STRING_ID);
//                proposal.setLinkedProposalModel(linkedProposalModel);
//                return new IJavaCompletionProposal[] { proposal };
//            }

            //            ICompilationUnit cu=context.getCompilationUnit();
            //            IJavaProject javaProject=cu.getJavaProject();
            //            IType type=cu.getAllTypes()[0];
            //            Set<String> excluded=Sets.newHashSet();
            //            for (IField field : type.getFields())
            //                excluded.add(field.getElementName());
            //            
            //            String stringLiteral=((StringLiteral)context.getCoveringNode()).getLiteralValue();
            //            String[] initialValue=StubUtility.getVariableNameSuggestions(NamingConventions.VK_STATIC_FINAL_FIELD, javaProject, stringLiteral, 0,
            //                    excluded, true);
            //            String fieldName=initialValue[0];
            //            
            //            AST ast=context.getASTRoot().getAST();
            //            SimpleName node=ast.newSimpleName(fieldName);
            //            ITypeBinding typeBinding=Bindings.getBindingOfParentTypeContext(context.getCoveringNode());
            //            ast.get
            EI18NCompletionProposal proposal=new EI18NCompletionProposal(context);
            return new IJavaCompletionProposal[] { proposal };
        }
        return null;
    }

    //    ExtractConstantRefactoring extractConstRefactoring= new ExtractConstantRefactoring(context.getASTRoot(), context.getSelectionOffset(), context.getSelectionLength());
    //    if (extractConstRefactoring.checkInitialConditions(new NullProgressMonitor()).isOK()) {
    //        LinkedProposalModel linkedProposalModel= new LinkedProposalModel();
    //        extractConstRefactoring.setLinkedProposalModel(linkedProposalModel);
    //        extractConstRefactoring.setCheckResultForCompileProblems(false);
    //
    //        String label= CorrectionMessages.QuickAssistProcessor_extract_to_constant_description;
    //        Image image= JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_LOCAL);
    //        int relevance;
    //        if (context.getSelectionLength() == 0) {
    //            relevance= IProposalRelevance.EXTRACT_CONSTANT_ZERO_SELECTION;
    //        } else if (problemsAtLocation) {
    //            relevance= IProposalRelevance.EXTRACT_CONSTANT_ERROR;
    //        } else {
    //            relevance= IProposalRelevance.EXTRACT_CONSTANT;
    //        }
    //        RefactoringCorrectionProposal proposal= new RefactoringCorrectionProposal(label, cu, extractConstRefactoring, relevance, image) {
    //            @Override
    //            protected void init(Refactoring refactoring) throws CoreException {
    //                ExtractConstantRefactoring etr= (ExtractConstantRefactoring) refactoring;
    //                etr.setConstantName(etr.guessConstantName()); // expensive
    //            }
    //        };
    //        proposal.setCommandId(EXTRACT_CONSTANT_ID);
    //        proposal.setLinkedProposalModel(linkedProposalModel);
    //        proposals.add(proposal);
    //    }
}
