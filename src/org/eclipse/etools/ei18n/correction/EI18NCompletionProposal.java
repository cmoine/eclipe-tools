package org.eclipse.etools.ei18n.correction;

import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.etools.Activator;
import org.eclipse.etools.ei18n.EI18NImage;
import org.eclipse.etools.ei18n.dialogs.JavaFileSelectionDialog;
import org.eclipse.etools.ei18n.dialogs.PropertiesFileSelectionDialog;
import org.eclipse.etools.ei18n.util.MappingPreference;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.NamingConventions;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.refactoring.CompilationUnitChange;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.dom.ASTNodeFactory;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.util.Resources;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.jdt.internal.ui.text.correction.CorrectionMessages;
import org.eclipse.jdt.internal.ui.text.correction.ModifierCorrectionSubProcessor;
import org.eclipse.jdt.internal.ui.text.correction.proposals.LinkedCorrectionProposal;
import org.eclipse.jdt.internal.ui.util.ExceptionHandler;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.window.Window;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;

import com.google.common.collect.Sets;

public class EI18NCompletionProposal extends LinkedCorrectionProposal /* implements IJavaCompletionProposal*/{
    private static final String ADD_STRING_ID=Activator.PLUGIN_ID + ".add.string"; //$NON-NLS-1$

    private static final String KEY_NAME="name"; //$NON-NLS-1$
    private static final String KEY_TYPE="type"; //$NON-NLS-1$
    private static final String KEY_INITIALIZER="initializer"; //$NON-NLS-1$

    private final IInvocationContext context;

    private IProject project;

    private IFile file;

    //    private CompilationUnit astRoot;
    private SimpleName node;//=fOriginalNode;

    //    private ITypeBinding fSenderBinding;

    private CompilationUnit targetCU;

    private IType type;

    private final CompilationUnit srcCU;

    private boolean fSwitchedEditor;

    private ICompilationUnit unit;

    public EI18NCompletionProposal(IInvocationContext context) {
        super(MessageFormat.format("Externalize \"{0}\"", ((StringLiteral) context.getCoveringNode()).getLiteralValue()), context.getCompilationUnit(), null,
                0, EI18NImage.LOGO_16.getImage());
        srcCU=context.getASTRoot();
        //        this.fSenderBinding=fSenderBinding;
        //        astRoot=context.getASTRoot();
        this.context=context;
        try {
            file=(IFile) context.getCompilationUnit().getCorrespondingResource();
            project=file.getProject();
        } catch (JavaModelException e) {
            Activator.log(IStatus.ERROR, "Failed to get project", e); //$NON-NLS-1$
        }
    }

    public IProject getProject() {
        return project;
    }

    public IFile getFile() {
        return file;
    }

    @Override
    public Object getAdditionalProposalInfo(IProgressMonitor monitor) {
        return null;
    }

    @Override
    protected ASTRewrite getRewrite() throws CoreException {
        boolean isInDifferentCU=false;

        //        ASTNode newTypeDecl=targetCU.findDeclaringNode(fSenderBinding);
        //        if (newTypeDecl == null) {
            //            astRoot=targetCU.ASTResolving.createQuickFixAST(getCompilationUnit(), null);
        final TypeDeclaration[] targetDecl=new TypeDeclaration[1];
        targetCU.accept(new ASTVisitor() {
            @Override
            public boolean visit(TypeDeclaration node) {
                targetDecl[0]=node;
                return super.visit(node);
            }
        });

        final TypeDeclaration[] srcDecl=new TypeDeclaration[1];
        srcCU.accept(new ASTVisitor() {
            @Override
            public boolean visit(TypeDeclaration node) {
                srcDecl[0]=node;
                return super.visit(node);
            }
        });
        //        ASTNode newTypeDecl=targetCU.getRoot();//findDeclaringNode(type.getElementName() /* fSenderBinding.getKey()*/);
        isInDifferentCU=true;
        //        }
        ImportRewrite imports=createImportRewrite(targetCU);
        ImportRewriteContext importRewriteContext=new ContextSensitiveImportRewriteContext(srcDecl[0], imports);

        if (targetDecl[0] != null) {
            AST ast=targetDecl[0].getAST();

            ASTRewrite rewrite=ASTRewrite.create(ast);

            VariableDeclarationFragment fragment=ast.newVariableDeclarationFragment();
            fragment.setName(ast.newSimpleName(node.getIdentifier()));

            Type type=evaluateVariableType(ast, imports, importRewriteContext);

            FieldDeclaration newDecl=ast.newFieldDeclaration(fragment);
            newDecl.setType(type);
            newDecl.modifiers().addAll(ASTNodeFactory.newModifiers(ast, evaluateFieldModifiers(targetDecl[0])));

            //            if (fSenderBinding.isInterface() || fVariableKind == CONST_FIELD) {
            //                fragment.setInitializer(ASTNodeFactory.newDefaultExpression(ast, type, 0));
            //            }

            ChildListPropertyDescriptor property=ASTNodes.getBodyDeclarationsProperty(targetDecl[0]);
            List<BodyDeclaration> decls=(List<BodyDeclaration>) targetDecl[0].getStructuralProperty(property);

            int maxOffset=isInDifferentCU ? -1 : node.getStartPosition();

            int insertIndex=findFieldInsertIndex(decls, newDecl, maxOffset);

            ListRewrite listRewriter=rewrite.getListRewrite(targetDecl[0], property);
            listRewriter.insertAt(newDecl, insertIndex, null);

            boolean isInterface=false;
            ModifierCorrectionSubProcessor.installLinkedVisibilityProposals(getLinkedProposalModel(), rewrite, newDecl.modifiers(), isInterface);

            addLinkedPosition(rewrite.track(newDecl.getType()), false, KEY_TYPE);
            if (!isInDifferentCU) {
                addLinkedPosition(rewrite.track(node), true, KEY_NAME);
            }
            addLinkedPosition(rewrite.track(fragment.getName()), false, KEY_NAME);

            if (fragment.getInitializer() != null) {
                addLinkedPosition(rewrite.track(fragment.getInitializer()), false, KEY_INITIALIZER);
            }
            return rewrite;
        }

        return null;
    }

    private Type evaluateVariableType(AST ast, ImportRewrite imports, ImportRewriteContext importRewriteContext) {
        return imports.addImport(ast.resolveWellKnownType("java.lang.String"), ast, importRewriteContext);
        //        return (Type) ast.resolveWellKnownType("java.lang.String").;
    }

    private int evaluateFieldModifiers(ASTNode newTypeDecl) {
        return Modifier.PUBLIC | Modifier.STATIC;
    }

    private int findFieldInsertIndex(List<BodyDeclaration> decls, FieldDeclaration newDecl, int maxOffset) {
        if (maxOffset != -1) {
            for (int i=decls.size() - 1; i >= 0; i--) {
                BodyDeclaration curr=decls.get(i);
                if (maxOffset > curr.getStartPosition() + curr.getLength()) {
                    return ASTNodes.getInsertionIndex(newDecl, decls.subList(0, i + 1));
                }
            }
            return 0;
        }
        return ASTNodes.getInsertionIndex(newDecl, decls);
    }

    @Override
    public void apply(IDocument document) {
        try {

            IFile initialSelection=(IFile) getFile().getParent().findMember("messages.properties"); //$NON-NLS-1$
            PropertiesFileSelectionDialog dialog=new PropertiesFileSelectionDialog(getShell(), "Please select a property file", getProject(), initialSelection);
            if (dialog.open() == Window.OK) {
                MappingPreference mappingPreference=new MappingPreference(dialog.getFile());
                IFile javaFile;
                if (mappingPreference.getJavaFile() == null) {
                    initialSelection=(IFile) dialog.getFile().getParent().findMember("Messages.java"); //$NON-NLS-1$
                    JavaFileSelectionDialog dialog2=new JavaFileSelectionDialog(getShell(), "Please select the corresponding mapping java file", getProject(),
                            initialSelection);
                    if (dialog2.open() == Window.CANCEL)
                        return;

                    javaFile=dialog2.getFile();
                    mappingPreference.set(javaFile);
                } else {
                    javaFile=mappingPreference.getJavaFile();
                }

                // TESTS
                IJavaProject targetJavaProject=JavaCore.create(getProject());
                unit=((org.eclipse.jdt.internal.core.CompilationUnit) JavaCore.create(javaFile)).getCompilationUnit();
                targetCU=ASTResolving.createQuickFixAST(unit, null);
                type=unit.getAllTypes()[0];
                Set<String> excluded=Sets.newHashSet();
                for (IField field : type.getFields())
                    excluded.add(field.getElementName());

                String[] initialValue=StubUtility.getVariableNameSuggestions(NamingConventions.VK_STATIC_FINAL_FIELD, targetJavaProject, getStringLiteral(), 0,
                        excluded, true);
                String fieldName=initialValue[0];
                node=targetCU.getAST().newSimpleName(fieldName);
                //                fSenderBinding=Bindings.getBindingOfParentTypeContext(targetCU.getRoot());

                //                super.apply(document);
                try {
                    //                    ICompilationUnit unit=targetgetCompilationUnit();
                    IEditorPart part=null;
                    if (unit.getResource().exists()) {
                        boolean canEdit=performValidateEdit(unit);
                        if (!canEdit) {
                            return;
                        }
                        part=EditorUtility.isOpenInEditor(unit);
                        if (part == null) {
                            part=JavaUI.openInEditor(unit);
                            if (part != null) {
                                fSwitchedEditor=true;
                                document=JavaUI.getDocumentProvider().getDocument(part.getEditorInput());
                            }
                        }
                        IWorkbenchPage page=JavaPlugin.getActivePage();
                        if (page != null && part != null) {
                            page.bringToTop(part);
                        }
                        if (part != null) {
                            part.setFocus();
                        }
                    }
                    performChange(part, document);
                } catch (CoreException e) {
                    ExceptionHandler.handle(e, CorrectionMessages.CUCorrectionProposal_error_title, CorrectionMessages.CUCorrectionProposal_error_message);
                }

                //                String lineDelimiter=StubUtility.getLineDelimiterUsed(javaProject);
                //                Template template=StubUtility.getCodeTemplate(EI18NStartup.EI18N_FIELD_ID, javaProject);
                //                CodeTemplateContext ctx=new CodeTemplateContext(template.getContextTypeId(), javaProject, lineDelimiter);
                //
                //                ctx.setVariable(CodeTemplateContextType.FIELD, fieldName);
                //
                //                String decl=ctx.evaluate(template).getString();
                //
                //                IField field=type.createField(decl, null, false, null);

                //                RenameJavaElementAction renameJavaElement=new RenameJavaElementAction(getSite());
                //                renameJavaElement.run(new TextSelection(offset, length));

                //                AST ast=context.getCoveringNode().getAST();
                //                ASTRewrite astRewrite=ASTRewrite.create(ast);

                //                boolean useReducedForm=true;
                //                if (type.get().local) {
                //                    for (IImportDeclaration importDecl : context.getCompilationUnit().getImports()) {
                //                        if (importDecl.getElementName().endsWith("." + type.get().type.getElementName())) { //$NON-NLS-1$
                //                            useReducedForm=false;
                //                            break;
                //                        }
                //                    }
                //                } else {
                //                    useReducedForm=false;
                //                    for (IImportDeclaration importDecl : context.getCompilationUnit().getImports()) {
                //                        if (importDecl.getElementName().equals(type.get().type.getFullyQualifiedName())) {
                //                            useReducedForm=true;
                //                            break;
                //                        }
                //                    }
                //                }

                //                String elementName=type.getElementName();
                //                QualifiedName newStringLiteral=ast.newQualifiedName(ast.newName(elementName), ast.newSimpleName(fieldName));
                //                astRewrite.replace(context.getCoveringNode(), newStringLiteral, null);
                //                astRewrite.rewriteAST(document, context.getCompilationUnit().getJavaProject().getOptions(true)).apply(document);

                //                context.getSelectionOffset() + elementName.length() + 1

                //                RenameJavaElementAction renameFieldProcessor=new RenameJavaElementAction(getEditor());
                //                TextSelection selection=new TextSelection(document, context.getSelectionOffset() + elementName.length() + 1, 0);
                //                renameFieldProcessor.update(selection);
                //                renameFieldProcessor.run(selection);

                //                InputDialog inputDialog=new InputDialog(getShell(), getShell().getText(), "Enter a key name", initialValue[0], new IInputValidator() {
                //                    //                    private RenameFieldProcessor renameFieldProcessor=new RenameFieldProcessor
                //                    @Override
                //                    public String isValid(String newText) {
                //                        try {
                //                            renameFieldProcessor.checkNewElementName(newText);
                //                            return null;
                //                        } catch (CoreException e) {
                //                            return e.getMessage();
                //                        }
                //                    }
                //                });
                //                if (inputDialog.open() == Window.OK) {
                //                    
                //                    String value=inputDialog.getValue();
                //                }
            }
        } catch (Exception e) {
            Activator.log(IStatus.ERROR, "", e);
        }
    }

    @Override
    protected TextChange createTextChange() throws CoreException {
        ICompilationUnit cu=unit;
        String name=getName();
        TextChange change;
        if (!cu.getResource().exists()) {
            String source;
            try {
                source=cu.getSource();
            } catch (JavaModelException e) {
                JavaPlugin.log(e);
                source=new String(); // empty
            }
            Document document=new Document(source);
            document.setInitialLineDelimiter(StubUtility.getLineDelimiterUsed(cu));
            change=new DocumentChange(name, document);
        } else {
            CompilationUnitChange cuChange=new CompilationUnitChange(name, cu);
            cuChange.setSaveMode(TextFileChange.LEAVE_DIRTY);
            change=cuChange;
        }
        TextEdit rootEdit=new MultiTextEdit();
        change.setEdit(rootEdit);

        // initialize text change
        IDocument document=change.getCurrentDocument(new NullProgressMonitor());
        addEdits(document, rootEdit);
        return change;
    }

    private boolean performValidateEdit(ICompilationUnit unit) {
        IStatus status=Resources.makeCommittable(unit.getResource(), JavaPlugin.getActiveWorkbenchShell());
        if (!status.isOK()) {
            String label=CorrectionMessages.CUCorrectionProposal_error_title;
            String message=CorrectionMessages.CUCorrectionProposal_error_message;
            ErrorDialog.openError(JavaPlugin.getActiveWorkbenchShell(), label, message, status);
            return false;
        }
        return true;
    }

    @Override
    protected boolean didOpenEditor() {
        return fSwitchedEditor;
    }

        // OLD TEST
        //        final boolean isInterface=false;
        //
        //        NewClassCreationWizard creationWizard=new NewClassCreationWizard(getProject(), isInterface, "Messages") {
        //            @Override
        //            public void addPages() {
        //                if (isInterface) {
        //                    fMainPage=new MyNewInterfaceWizardPage();
        //                } else {
        //                    fMainPage=new MyNewClassWizardPage();
        //                    ((NewClassWizardPage) fMainPage).setMethodStubSelection(false, false, false, false);
        //                }
        //
        //                PackageFragment packageFragment=(PackageFragment) context.getCompilationUnit().getParent();
        //                fMainPage.setPackageFragment(packageFragment, false);
        //                //                while (!(packageFragment instanceof IPackageFragmentRoot))
        //                //                    packageFragment=(IPackageFragment) packageFragment.getPrParent();
        //                fMainPage.setPackageFragmentRoot(packageFragment.getPackageFragmentRoot(), false);
        //                fMainPage.setEnclosingTypeSelection(false, false);
        //                fMainPage.setTypeName("Messages", true);
        //                addPage(fMainPage);
        //                //                super.addPages();
        //                // TODO 
        //                //                fMainPage.setPackageFragment(pack, canBeModified);
        //            }
        //        };
        //
        //        new WizardDialog(getShell(), creationWizard).open();

        // VERY OLD TEST
        //        try {
        //            final AtomicReference<MyType> type=new AtomicReference<MyType>();
        //            IPackageFragment pkgFragment=getPackageFragment(context.getCompilationUnit());
        //
        //            List<MyType> types=collectTypes(pkgFragment);
        //
        //            {
        //                if (types.isEmpty()) {
        //                    ICompilationUnit compilationUnit=pkgFragment.createCompilationUnit("Messages.java", //$NON-NLS-1$
        //                            StringUtils.replace(MESSAGES_CONTENT, "${package.name}", pkgFragment.getElementName()), false, //$NON-NLS-1$
        //                            new NullProgressMonitor());
        //                    type.set(new MyType(compilationUnit.getTypes()[0], true));
        //                } else if (types.size() == 1) {
        //                    type.set(types.get(0));
        //                } else {
        //                    final ListDialog dialog=new ListDialog(getShell());
        //                    dialog.setMessage("Choose the destination bundle class:"); //$NON-NLS-1$
        //                    dialog.setContentProvider(ArrayContentProvider.getInstance());
        //                    class MyLabelProvider extends LabelProvider implements IFontProvider {
        //                        private Font italicFont;
        //
        //                        @Override
        //                        public String getText(Object element) {
        //                            return ((MyType) element).type.getFullyQualifiedName();
        //                        }
        //
        //                        @Override
        //                        public Font getFont(Object element) {
        //                            if (((MyType) element).local)
        //                                return italicFont();
        //                            return null;
        //                        }
        //
        //                        @Override
        //                        public void dispose() {
        //                            super.dispose();
        //                            if (italicFont != null)
        //                                italicFont.dispose();
        //                        }
        //
        //                        private Font italicFont() {
        //                            if (italicFont == null) {
        //                                Font font=dialog.getTableViewer().getTable().getFont();
        //                                FontData[] fontData=font.getFontData();
        //                                fontData[0].setStyle(fontData[0].getStyle() ^ SWT.BOLD);
        //                                italicFont=new Font(font.getDevice(), fontData);
        //                            }
        //                            return italicFont;
        //                        }
        //                    }
        //                    dialog.setHelpAvailable(false);
        //                    dialog.setLabelProvider(new MyLabelProvider());
        //                    dialog.setInput(types);
        //                    dialog.setInitialElementSelections(Arrays.asList(types.get(0)));
        //                    if (dialog.open() == Window.CANCEL)
        //                        return;
        //
        //                    type.set((MyType) dialog.getResult()[0]);
        //                }
        //            }
        //            final List<Translation> translations=type.get().getTranslations();
        //
        //            InputDialog dialog=new InputDialog(getShell(), StringUtils.EMPTY, "Enter a key name", StringUtils.EMPTY, new IInputValidator() { //$NON-NLS-1$
        //                        @Override
        //                        public String isValid(String newText) {
        //                            if (newText.isEmpty())
        //                                return "You must enter a value"; //$NON-NLS-1$
        //                            if (!SourceVersion.isName(newText))
        //                                return "Not a valid Java identifier"; //$NON-NLS-1$
        //                            for (Translation t : translations) {
        //                                if (t.props.containsKey(newText))
        //                                    return "This key already exists in " + t.file; //$NON-NLS-1$
        //                            }
        //                            if (type.get().type.getField(newText).exists())
        //                                return "This key already exists in " + type.get().type.getResource(); //$NON-NLS-1$
        //                            return null;
        //                        }
        //            });
        //            if (dialog.open() == Window.CANCEL)
        //                return;
        //
        //            final String key=dialog.getValue();
        //
        //            final TitleAreaDialog lDialog=new TitleAreaDialog(getShell()) {
        //                private TableViewer viewer;
        //                private TableColumn fileTblColumn;
        //                private TableColumn translationTblColumn;
        //
        //                @Override
        //                protected Control createDialogArea(Composite container) {
        //                    setTitle(MessageFormat.format("Externalize key ''{0}''", key)); //$NON-NLS-1$
        //                    setMessage("Enter a translation for each bundle file"); //$NON-NLS-1$
        //                    setHelpAvailable(false);
        //
        //                    viewer=createTableViewer(container);
        //
        //                    Table table=viewer.getTable();
        //                    table.addTraverseListener(new TraverseListener() {
        //                        @Override
        //                        public void keyTraversed(TraverseEvent e) {
        //                            if (e.detail == SWT.TRAVERSE_RETURN) {
        //                                e.doit=false;
        //                                e.detail=SWT.TRAVERSE_NONE;
        //                            }
        //                        }
        //                    });
        //                    table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        //
        //                    table.setHeaderVisible(true);
        //                    table.setLinesVisible(true);
        //                    fileTblColumn=new TableColumn(table, SWT.NONE);
        //                    fileTblColumn.setText(FILE_COLUMN);
        //                    fileTblColumn.setWidth(getWidth(FILE_COLUMN, 200));
        //
        //                    translationTblColumn=new TableColumn(table, SWT.NONE);
        //                    translationTblColumn.setText(TRANSLATION_COLUMN);
        //                    translationTblColumn.setWidth(getWidth(TRANSLATION_COLUMN, 500));
        //
        //                    final TextCellEditor fileTextCellEditor=new TextCellEditor(table) {
        //                        @Override
        //                        protected void doSetFocus() {
        //                            super.doSetFocus();
        //                            if (text != null) {
        //                                int indexOf=text.getText().indexOf('.');
        //                                if (indexOf != -1) {
        //                                    text.setSelection(indexOf);
        //                                }
        //                            }
        //                        }
        //                    };
        //                    fileTextCellEditor.setValidator(new ICellEditorValidator() {
        //                        @Override
        //                        public String isValid(Object value) {
        //                            String str=(String) value;
        //                            if (StringUtils.isEmpty(str))
        //                                return null;
        //
        //                            // Check that value does not already exists
        //                            List<Translation> transactions=getTranslations2();
        //                            for (Translation translation : transactions) {
        //                                if (translation.file != null && translation.file.getName().equals(str))
        //                                    return "Name " + str + " already exists.";
        //                            }
        //                            if ((!EI18NConstants.LOCALE_PATTERN.matcher(str).matches()) && (!EI18NConstants.PATTERN.matcher(str).matches()))
        //                                return "Does not match property file pattern";
        //
        //                            return null;
        //                        }
        //                    });
        //                    fileTextCellEditor.addListener(new ICellEditorListener() {
        //                        @Override
        //                        public void applyEditorValue() {
        //                            setErrorMessage(null);
        //                        }
        //
        //                        @Override
        //                        public void cancelEditor() {
        //                            setErrorMessage(null);
        //                        }
        //
        //                        @Override
        //                        public void editorValueChanged(boolean oldValidState, boolean newValidState) {
        //                            setErrorMessage(fileTextCellEditor.getErrorMessage());
        //                        }
        //                    });
        //                    // TODO CME
        //                    //                    CellEditor comboBoxCellEditor=new TranslationCellEditor(table) {
        //                    //                        @Override
        //                    //                        protected Map<String, IFile> getStringToTranslate() {
        //                    //                            Translation selection=getSelectedTranslation();
        //                    //                            Map<String, IFile> toTranslate=Maps.newHashMap();
        //                    //                            for (Translation translation : getTranslations2()) {
        //                    //                                if (StringUtils.isNotEmpty(translation.value) && selection != translation)
        //                    //                                    toTranslate.put(translation.value, translation.file);
        //                    //                            }
        //                    //                            return toTranslate;
        //                    //                        }
        //                    //
        //                    //                        @Override
        //                    //                        protected IFile getSelectedFile() {
        //                    //                            return getSelectedTranslation().file;
        //                    //                        }
        //                    //
        //                    //                        private Translation getSelectedTranslation() {
        //                    //                            return (Translation) ((IStructuredSelection) getTableViewer().getSelection()).getFirstElement();
        //                    //                        }
        //                    //                    };
        //                    //                    getTableViewer().setCellEditors(new CellEditor[] { fileTextCellEditor, comboBoxCellEditor });
        //                    //                    getTableViewer().setColumnProperties(new String[] { FILE_COLUMN, TRANSLATION_COLUMN });
        //                    //                    getTableViewer().setCellModifier(new ICellModifier() {
        //                    //                        @Override
        //                    //                        public void modify(Object element, String property, Object value) {
        //                    //                            Object data=((TableItem) element).getData();
        //                    //                            Translation translation=(Translation) data;
        //                    //                            String strVal=(String) value;
        //                    //                            if (property == TRANSLATION_COLUMN) {
        //                    //                                translation.value=strVal;
        //                    //                            } else {
        //                    //                                if (StringUtils.isEmpty(strVal)) {
        //                    //                                    translation.file=null;
        //                    //                                    // TODO shift further values
        //                    //                                    // List<Translation> transactions=getTranslations2();
        //                    //                                    // getTableViewer().remove(transactions.remove(transactions.size() - 1));
        //                    //                                } else {
        //                    //                                    translation.file=((IFolder) getPackageFragment(type.get().type.getCompilationUnit()).getResource()).getFile(strVal);
        //                    //                                    Translation emptyTransaction=new Translation(null);
        //                    //                                    getTranslations2().add(emptyTransaction);
        //                    //                                    getTableViewer().add(emptyTransaction);
        //                    //                                }
        //                    //                            }
        //                    //                            getTableViewer().update(data, new String[] { property });
        //                    //                        }
        //                    //
        //                    //                        @Override
        //                    //                        public Object getValue(Object element, String property) {
        //                    //                            if (property == TRANSLATION_COLUMN) {
        //                    //                                return ((Translation) element).value;
        //                    //                            } else {
        //                    //                                String fileName=((Translation) element).file == null ? StringUtils.EMPTY : ((Translation) element).file.getName();
        //                    //                                if (fileName.isEmpty())
        //                    //                                    return getTranslations2().get(0).file.getName();
        //                    //                                else
        //                    //                                    return fileName;
        //                    //                            }
        //                    //                        }
        //                    //
        //                    //                        @Override
        //                    //                        public boolean canModify(Object element, String property) {
        //                    //                            return (property == TRANSLATION_COLUMN && !((Translation) element).props.containsKey(key)) || (property == FILE_COLUMN);
        //                    //                        }
        //                    //                    });
        //                    getTableViewer().refresh();
        //                    return table;
        //                }
        //
        //                private int getWidth(String key, int defaultValue) {
        //                    try {
        //                        int width=getDialogBoundsSettings().getInt(key);
        //                        if (width != -1)
        //                            return width;
        //                    } catch (NumberFormatException ex) {
        //                    }
        //                    return defaultValue;
        //                }
        //
        //                @Override
        //                public boolean close() {
        //                    getDialogBoundsSettings().put(FILE_COLUMN, fileTblColumn.getWidth());
        //                    getDialogBoundsSettings().put(TRANSLATION_COLUMN, translationTblColumn.getWidth());
        //                    return super.close();
        //                }
        //
        //                protected TableViewer createTableViewer(Composite container) {
        //                    final TableViewer tableViewer=new TableViewer(container, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        //                    tableViewer.setContentProvider(ArrayContentProvider.getInstance());
        //                    class MyLabelProvider extends LabelProvider implements ITableLabelProvider, ITableFontProvider, ITableColorProvider {
        //                        private Font italicFont;
        //
        //                        @Override
        //                        public Image getColumnImage(Object element, int columnIndex) {
        //                            Translation translation=(Translation) element;
        //                            if (translation.file == null)
        //                                return null;
        //
        //                            if (columnIndex == 0) {
        //                                Matcher matcher=EI18NConstants.LOCALE_PATTERN.matcher(translation.file.getName());
        //                                if (matcher.matches()) {
        //                                    Locale locale=LocaleUtils.toLocale(matcher.group(1));
        //                                    return EI18NImage.getImage(locale);
        //                                }
        //                                return EI18NImage.LOGO_16.getImage();
        //                            } else {
        //                                if (translation.props.containsKey(key))
        //                                    return EI18NImage.VALIDATE_16.getImage();
        //                            }
        //
        //                            return null;
        //                        }
        //
        //                        @Override
        //                        public String getColumnText(Object element, int columnIndex) {
        //                            Translation translation=(Translation) element;
        //                            if (columnIndex == 0)
        //                                return translation.file == null ? "New" : translation.file.getName(); //$NON-NLS-1$
        //                            else
        //                                return translation.file == null ? StringUtils.EMPTY : translation.value;
        //                        }
        //
        //                        @Override
        //                        public Color getBackground(Object element, int columnIndex) {
        //                            return null;
        //                        }
        //
        //                        @Override
        //                        public Color getForeground(Object element, int columnIndex) {
        //                            Translation transalation=(Translation) element;
        //                            if (transalation.file == null)
        //                                return tableViewer.getControl().getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
        //
        //                            return null;
        //                        }
        //
        //                        @Override
        //                        public Font getFont(Object element, int columnIndex) {
        //                            Translation transalation=(Translation) element;
        //                            if (transalation.file == null)
        //                                return italicFont();
        //                            return null;
        //                        }
        //
        //                        private Font italicFont() {
        //                            if (italicFont == null)
        //                                italicFont=FontUtil.derivate(tableViewer.getTable().getFont(), SWT.ITALIC);
        //
        //                            return italicFont;
        //                        }
        //
        //                        @Override
        //                        public void dispose() {
        //                            super.dispose();
        //                            FontUtil.safeDispose(italicFont);
        //                        }
        //                    }
        //                    tableViewer.setLabelProvider(new MyLabelProvider());
        //                    tableViewer.setInput(translations);
        //                    return tableViewer;
        //                }
        //
        //                private TableViewer getTableViewer() {
        //                    return viewer;
        //                }
        //
        //                @Override
        //                public void setErrorMessage(String newErrorMessage) {
        //                    super.setErrorMessage(newErrorMessage);
        //                    getButton(IDialogConstants.OK_ID).setEnabled(newErrorMessage == null);
        //                }
        //
        //                protected List<Translation> getTranslations2() {
        //                    return (List<Translation>) getTableViewer().getInput();
        //                }
        //
        //                @Override
        //                protected IDialogSettings getDialogBoundsSettings() {
        //                    return Activator.getDefault().getOrCreateDialogSettings(getClass());
        //                }
        //            };
        //            // lDialog.setContentProvider(ArrayContentProvider.getInstance());
        //
        //            // lDialog.setDialogBoundsSettings(Activator.getDefault().getOrCreateDialogSettings(lDialog.getClass()), Dialog.DIALOG_PERSISTLOCATION
        //            // | Dialog.DIALOG_PERSISTSIZE);
        //            // lDialog.setLabelProvider(new MyLabelProvider());
        //            // lDialog.setInput(translations);
        //            if (lDialog.open() == Window.CANCEL)
        //                return;
        //
        //            for (Translation t : translations) {
        //                if (t.file != null) {
        //                    if (!t.file.exists())
        //                        t.file.create(new ByteArrayInputStream(ArrayUtils.EMPTY_BYTE_ARRAY), false, new NullProgressMonitor());
        //
        //                    if (!t.props.containsKey(key)) {
        //                        append(key, t);
        //                    } else {
        //                        MessageDialog.openError(getShell(), StringUtils.EMPTY, "The key " + key + " already exist in file " + t.file); //$NON-NLS-1$ //$NON-NLS-2$
        //                    }
        //                }
        //            }
        //
        //            // TODO CME
        //            //            CompilationUnitUtil.addField(type.get().type.getCompilationUnit(), key);
        //            AST ast=context.getCoveringNode().getAST();
        //            ASTRewrite astRewrite=ASTRewrite.create(ast);
        //            boolean useReducedForm=true;
        //            if (type.get().local) {
        //                for (IImportDeclaration importDecl : context.getCompilationUnit().getImports()) {
        //                    if (importDecl.getElementName().endsWith("." + type.get().type.getElementName())) { //$NON-NLS-1$
        //                        useReducedForm=false;
        //                        break;
        //                    }
        //                }
        //            } else {
        //                useReducedForm=false;
        //                for (IImportDeclaration importDecl : context.getCompilationUnit().getImports()) {
        //                    if (importDecl.getElementName().equals(type.get().type.getFullyQualifiedName())) {
        //                        useReducedForm=true;
        //                        break;
        //                    }
        //                }
        //            }
        //
        //            QualifiedName newStringLiteral=ast.newQualifiedName(
        //                    ast.newName(useReducedForm ? type.get().type.getElementName() : type.get().type.getFullyQualifiedName()),
        //                    ast.newSimpleName(key));
        //            astRewrite.replace(context.getCoveringNode(), newStringLiteral, null);
        //            astRewrite.rewriteAST(document, context.getCompilationUnit().getJavaProject().getOptions(true)).apply(document);
        //
        //            // if (!imports.isEmpty()) {
        //            // ImportRewrite importRewrite=ImportRewrite.create(context.getCompilationUnit(), false);
        //            // importRewrite.addImport("blabla");
        //            // importRewrite.rewriteImports(new NullProgressMonitor()).apply(document);
        //            // }
        //        } catch (JavaModelException e) {
        //            Activator.log(ERROR, "Failed to quick assist", e); //$NON-NLS-1$
        //        } catch (MalformedTreeException e) {
        //            Activator.log(ERROR, "Failed to quick assist", e); //$NON-NLS-1$
        //        } catch (BadLocationException e) {
        //            Activator.log(ERROR, "Failed to quick assist", e); //$NON-NLS-1$
        //        } catch (CoreException e) {
        //            Activator.log(ERROR, "Failed to quick assist", e); //$NON-NLS-1$
        //        }

    //    protected IPackageFragment getPackageFragment(ICompilationUnit compilationUnit) {
    //        IJavaElement parent=compilationUnit.getParent();
    //        while (!(parent instanceof IPackageFragment))
    //            parent=parent.getParent();
    //        return (IPackageFragment) parent;
    //    }
    //
    //    protected void append(final String key, Translation t) throws CoreException {
    //        String str=key + "=" + StringEscapeUtils.escapeJava(t.value); //$NON-NLS-1$
    //        if (!t.content.endsWith(IOUtils.LINE_SEPARATOR))
    //            str=IOUtils.LINE_SEPARATOR + str;
    //
    //        t.file.appendContents(new ByteArrayInputStream(str.getBytes(Charsets.UTF_8)), false, true, new NullProgressMonitor());
    //    }
    protected Shell getShell() {
        return getSite().getShell();
    }

    private IWorkbenchPartSite getSite() {
        return getEditor().getSite();
    }

    private JavaEditor getEditor() {
        return (JavaEditor) ((AssistContext) context).getEditor();
    }

    //    protected List<MyType> collectTypes(IPackageFragment parent) throws JavaModelException {
    //        List<MyType> types=Lists.newArrayList();
    //
    //        // Messages in the same package
    //        for (IJavaElement child : parent.getChildren()) {
    //            if (child instanceof ICompilationUnit) {
    //                ICompilationUnit cu=(ICompilationUnit) child;
    //                for (IType type : cu.getTypes()) {
    //                    if (accept(type)) {
    //                        types.add(new MyType(type, true));
    //                    }
    //                }
    //            }
    //        }
    //        // Messages in import packages
    //        for (IImportDeclaration importDecl : context.getCompilationUnit().getImports()) {
    //            IType type=importDecl.getJavaProject().findType(importDecl.getElementName());
    //            if (accept(type))
    //                types.add(new MyType(type, false));
    //        }
    //        return types;
    //    }
    //
    //    protected boolean accept(IType type) throws JavaModelException {
    //        return type != null && type.getFullyQualifiedName().endsWith("Messages") && EI18NConstants.NLS_CLASS_NAME.equals(type.getSuperclassName()); //$NON-NLS-1$ 
    //    }

    //    @Override
    //    public Point getSelection(IDocument document) {
    //        // TODO Auto-generated method stub
    //        return null;
    //    }
    //
    //    @Override
    //    public String getAdditionalProposalInfo() {
    //        // TODO Auto-generated method stub
    //        return null;
    //    }
    //
    //    @Override
    //    public String getDisplayString() {
    //        return MessageFormat.format("Externalize \"{0}\"", getStringLiteral()); //$NON-NLS-1$
    //    }

    protected String getStringLiteral() {
        return ((StringLiteral) context.getCoveringNode()).getLiteralValue();
    }

    //    @Override
    //    public Image getImage() {
    //        return EI18NImage.LOGO_16.getImage();
    //    }
    //
    //    @Override
    //    public IContextInformation getContextInformation() {
    //        // TODO Auto-generated method stub
    //        return null;
    //    }
    //
    //    @Override
    //    public int getRelevance() {
    //        return 0;
    //    }
}
