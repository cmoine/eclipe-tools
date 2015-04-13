package org.eclipse.etools.ei18n;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.etools.Activator;
import org.eclipse.etools.ei18n.dialogs.JavaFileSelectionDialog;
import org.eclipse.etools.ei18n.dialogs.PropertiesFileSelectionDialog;
import org.eclipse.etools.ei18n.util.MappingPreference;
import org.eclipse.etools.ei18n.util.PreferencesUtil;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.NamingConventions;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.template.java.CodeTemplateContext;
import org.eclipse.jdt.internal.corext.template.java.CodeTemplateContextType;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.refactoring.actions.RenameJavaElementAction;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IWorkbenchPartSite;

import com.google.common.collect.Sets;

public class EI18NCompletionProposal extends AbstractJavaCompletionProposal {
    private final IInvocationContext context;
    private final String displayString;

    public EI18NCompletionProposal(IInvocationContext context) {
        this.context=context;
        displayString=MessageFormat.format("Externalize \"{0}\"", ((StringLiteral) context.getCoveringNode()).getLiteralValue());
    }

    protected Shell getShell() {
        return getSite().getShell();
    }

    private IWorkbenchPartSite getSite() {
        return getEditor().getSite();
    }

    private JavaEditor getEditor() {
        return (JavaEditor) ((AssistContext) context).getEditor();
    }

    protected String getStringLiteral() {
        return ((StringLiteral) context.getCoveringNode()).getLiteralValue();
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
        return displayString;
    }

    @Override
    public String getSortString() {
        return displayString;
    }

    @Override
    public StyledString getStyledDisplayString() {
        return new StyledString().append(getDisplayString());
    }

    @Override
    public Image getImage() {
        return EI18NImage.LOGO_16.getImage();
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

    @Override
    public boolean validate(IDocument document, int offset, DocumentEvent event) {
        return true;
    }

    @Override
    public void apply(IDocument document, char trigger, int offset) {
        try {
            ICompilationUnit callerCU=context.getCompilationUnit();
            IFile file=(IFile) callerCU.getCorrespondingResource();
            IProject project=file.getProject();
            IFile initialSelection=(IFile) file.getParent().findMember("messages.properties"); //$NON-NLS-1$
            PropertiesFileSelectionDialog dialog=new PropertiesFileSelectionDialog(getShell(), "Please select a property file", project, initialSelection);
            if (dialog.open() == Window.OK) {
                IFile messagesFile=dialog.getFile();
                MappingPreference mappingPreference=new MappingPreference(messagesFile);
                IFile javaFile;
                if (mappingPreference.getJavaFile() == null) {
                    initialSelection=(IFile) dialog.getFile().getParent().findMember("Messages.java"); //$NON-NLS-1$
                    JavaFileSelectionDialog dialog2=new JavaFileSelectionDialog(getShell(), "Please select the corresponding mapping java file", project,
                            initialSelection);
                    if (dialog2.open() == Window.CANCEL)
                        return;

                    javaFile=dialog2.getFile();
                    mappingPreference.set(javaFile);
                } else {
                    javaFile=mappingPreference.getJavaFile();
                }

                ICompilationUnit calleeCU=(ICompilationUnit) JavaCore.create(javaFile);

                AST ast=context.getCoveringNode().getParent().getAST();
                ASTRewrite rewrite=ASTRewrite.create(ast);

                Set<String> excluded=Sets.newHashSet();
                IType messagesType=calleeCU.getAllTypes()[0];
                for (IField field : messagesType.getFields())
                    excluded.add(field.getElementName());

                IJavaProject javaProject=callerCU.getJavaProject();
                String[] initialValue=StubUtility.getVariableNameSuggestions(NamingConventions.VK_STATIC_FINAL_FIELD, javaProject, getStringLiteral(), 0,
                        excluded, true);
                if (initialValue.length == 0)
                    initialValue=StubUtility.getVariableNameSuggestions(NamingConventions.VK_STATIC_FINAL_FIELD, javaProject, "string", 0, excluded, true);
                String fieldName=initialValue[0];

                String lineDelimiter=StubUtility.getLineDelimiterUsed(javaProject);
                Template template=StubUtility.getCodeTemplate(EI18NStartup.EI18N_FIELD_ID, javaProject);
                CodeTemplateContext ctx=new CodeTemplateContext(template.getContextTypeId(), javaProject, lineDelimiter);

                ctx.setVariable(CodeTemplateContextType.FIELD, fieldName);

                String decl=ctx.evaluate(template).getString();

                messagesType.createField(decl, null, false, null);

                QualifiedName qName=ast.newQualifiedName(ast.newSimpleName(messagesType.getElementName()), ast.newSimpleName(fieldName));
                rewrite.replace(context.getCoveringNode(), qName, null);

                TextEdit edits=rewrite.rewriteAST(document, javaProject.getOptions(true));

                // computation of the new source code
                edits.apply(document);

                // update of the compilation unit
                String newSource=document.get();

                // update of the compilation unit
                callerCU.getBuffer().setContents(newSource);

                // Add key to .property
                InputStream is=messagesFile.getContents(true);
                try {
                    DefaultLineTracker lineTracker=new DefaultLineTracker();
                    String string=IOUtils.toString(is);
                    lineTracker.set(string);
                    StringBuffer buf=new StringBuffer();
                    IRegion region=lineTracker.getLineInformation(lineTracker.getNumberOfLines() - 1);
                    if (!string.substring(region.getOffset(), region.getOffset() + region.getLength()).trim().isEmpty()) {
                        buf.append(PreferencesUtil.getLineDelimiter(file));
                    }
                    buf.append(fieldName);
                    buf.append('=');
                    buf.append(mappingPreference.getEncoding().encode(getStringLiteral(), messagesFile));
                    messagesFile.appendContents(new ByteArrayInputStream(buf.toString().getBytes(messagesFile.getCharset())), true, true, null);
                } finally {
                    IOUtils.closeQuietly(is);
                }

                // Run the rename refactoring
                RenameJavaElementAction renameJavaElement=new RenameJavaElementAction(getEditor());
                TextSelection selection=new TextSelection(context.getCoveringNode().getStartPosition() + messagesType.getElementName().length() + 1, 0);
                getEditor().getSelectionProvider().setSelection(selection);
                renameJavaElement.run(selection);
            }
        } catch (Exception e) {
            Activator.logError("Failed to externalize string", e); //$NON-NLS-1$
        }

    }
}
