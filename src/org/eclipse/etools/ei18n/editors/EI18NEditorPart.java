package org.eclipse.etools.ei18n.editors;

import static java.text.MessageFormat.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.eclipse.core.runtime.IStatus.ERROR;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import javax.lang.model.SourceVersion;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.etools.Activator;
import org.eclipse.etools.ei18n.EI18NImage;
import org.eclipse.etools.ei18n.composite.EI18NComposite;
import org.eclipse.etools.ei18n.dialogs.JavaFileSelectionDialog;
import org.eclipse.etools.ei18n.extensions.IJavaMapping;
import org.eclipse.etools.ei18n.extensions.JavaMappingExtensionManager.JavaMappingExtension;
import org.eclipse.etools.ei18n.properties.EI18nPropertyPage;
import org.eclipse.etools.ei18n.util.EI18NConstants;
import org.eclipse.etools.ei18n.util.FontUtil;
import org.eclipse.etools.ei18n.util.LineProperties;
import org.eclipse.etools.ei18n.util.MappingPreference;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.propertiesfileeditor.PropertiesFileEditor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.part.MultiPageSelectionProvider;
import org.osgi.service.prefs.BackingStoreException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class EI18NEditorPart extends MultiPageEditorPart
{
    public static final String ID=EI18NEditorPart.class.getName();

    private static final String EI18N="EI18N"; //$NON-NLS-1$
    private EI18NComposite ei18nComposite;
    private int oldPageIndex=-1;

    private static class Information {
        public Information(IFile file, LineProperties lineProps, TextEditor editor, IDocument doc) {
            //            this.file=file;
            properties=lineProps;
            this.editor=editor;
            this.doc=doc;
        }

        private final LineProperties properties;
        private final IDocument doc;
        private final TextEditor editor;
    }

    private final Map<String, Information> infos=Maps.newHashMap();
    private final Set<String> newKeys=Sets.newHashSet();
    private CompilationUnitEditor javaEditor;
    private IFile javaFile;
    private Link link;
    private IFile propertyFile;
    private MappingPreference mappingPreference;

    @Override
    protected void createPages() {
        // Get all concerned files

        // ADD the main page
        Composite composite=new Composite(getContainer(), SWT.NONE);
        composite.setLayout(new GridLayout());

        link=new Link(composite, SWT.NONE);
        link.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                //                switch(e.text) {
                //                    case "change": {
                //                        break;
                //                    }
                //                }
                if ("change".equals(e.text)) { //$NON-NLS-1$
                    changeAssociation();
                } else if ("goto".equals(e.text)) { //$NON-NLS-1$
                    setActivePage(1);
                }
            }

            private void changeAssociation() {
                JavaFileSelectionDialog dialog=new JavaFileSelectionDialog(getShell(), "Please select the compilation unit it is linked with", getProject(),
                        javaFile == null ? (IFile) propertyFile.getParent().findMember("Messages.java") : javaFile);

                if (dialog.open() == Window.OK) {
                    try {
                        IFile file=(IFile) ((org.eclipse.jdt.internal.core.CompilationUnit) dialog.getFirstResult()).getCorrespondingResource();
                        //                        getProject().setPersistentProperty(qualifiedName, file.getFullPath().makeRelativeTo(getProject().getFullPath()).toString());
                        mappingPreference.set(file/*, (JavaMappingExtension) ((IStructuredSelection) comboViewer.getSelection()).getFirstElement()*/);
                        //                        prefs.put(propertyFile.toString(), file.getFullPath().makeRelativeTo(getProject().getFullPath()).toString());
                        //                        prefs.flush();
                        //                            updateLink();
                        updateJavaFile();
                    } catch (CoreException e1) {
                        Activator.log(IStatus.ERROR, "Failed to set association", e1); //$NON-NLS-1$
                    } catch (BackingStoreException e) {
                        Activator.log(IStatus.ERROR, "Failed to store mapping pref", e); //$NON-NLS-1$
                    }

                }
            }
        });
        link.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        ei18nComposite=new EI18NComposite(composite) {

            @Override
            protected void createLocale(String locale) {
                // TODO CME
                IFile newFile=propertyFile.getParent().getFile(new Path("messages_" + locale + ".properties")); //$NON-NLS-1$//$NON-NLS-2$
                try {
                    newFile.create(new NullInputStream(0L), false, new NullProgressMonitor());
                    loadPropertyTab(locale, newFile);
                    // TODO CME
                    //                    locales.add(value);
                    //                    setLocales(locales);

                    //                    for (Line key : getKeys()) {
                    //                    ei18nComposite.getViewer().refresh();//update(key, columns);
                    //                    }

                    //                    refreshColumns(locale);
                } catch (CoreException e) {
                    Activator.log(ERROR, "Failed creating file " + newFile, e); //$NON-NLS-1$
                }
            }
        };
        ei18nComposite.getViewer().getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        int mainPageIndex=addPage(composite);
        setPageText(mainPageIndex, EI18N);
        setPageImage(mainPageIndex, EI18NImage.LOGO_16.getImage());

        //        props=Lists.newArrayListWithCapacity(localFiles.size() + 1);
        //        editors=Lists.newArrayListWithCapacity(localFiles.size() + 1);

        //        if (javaFile != null)
        //            loadJavaEditorTab();
        updateJavaFile();

        try {
            IContainer folder=propertyFile.getParent();
            for (IResource res : folder.members()) {
                String name=res.getName();
                Matcher matcher;
                if (EI18NConstants.PATTERN.matcher(name).matches()) {
                    //                file=(IFile) res;
                    loadPropertyTab(EMPTY, (IFile) res);
                } else if ((matcher=EI18NConstants.LOCALE_PATTERN.matcher(name)).matches()) {
                    //                locales.add(matcher.group(1));
                    //                localFiles.add((IFile) res);
                    loadPropertyTab(matcher.group(1), (IFile) res);
                }
            }
            ei18nComposite.setLocales(infos.keySet());
        } catch (CoreException e1) {
            Activator.log(IStatus.ERROR, "Failed loading locales", e1); //$NON-NLS-1$
        }

        class ViewLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider, ITableFontProvider {
            private Font boldFont;
            private Font italicFont;

            public String getColumnText(Object obj, int index) {
                Line str=(Line) obj;
                if (str.isNew())
                    return index > 0 ? StringUtils.EMPTY : "New"; //$NON-NLS-1$
                if (index == 0)
                    return str.getString();
                else {// if (index - 1 < props.size())
                    String locale=ei18nComposite.getLocale(index);
                    if (locale != null) {
                        Information info=infos.get(locale);
                        if (info != null)
                            return info.properties.getProperty(str.getString()); // TODO
                    }
                }

                return null;
            }

            //            private String getColumnProperty(int index) {
            //                //                if(index<)
            //                Object[] properties=ei18nComposite.getViewer().getColumnProperties();
            //                return index < properties.length ? (String) properties[index] : null;
            //            }

            public Image getColumnImage(Object element, int index) {
                String locale=ei18nComposite.getLocale(index);
                if (locale != null && !((Line) element).isNew() && StringUtils.isEmpty(getColumnText(element, index)))
                    return EI18NImage.ERROR_16.getImage();

                return null;
            }

            public Color getForeground(Object element, int columnIndex) {
                if (((Line) element).isNew())
                    return getSite().getShell().getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);

                return null;
            }

            public Color getBackground(Object element, int columnIndex) {
                return null;
            }

            public Font getFont(Object element, int columnIndex) {
                if (((Line) element).isNew())
                    return italicFont();
                if (newKeys.contains(element))
                    return boldFont();

                return null;
            }

            private Font italicFont() {
                if (italicFont == null)
                    italicFont=FontUtil.derivate(ei18nComposite.getViewer().getControl().getFont(), SWT.ITALIC);

                return italicFont;
            }

            private Font boldFont() {
                if (boldFont == null)
                    boldFont=FontUtil.derivate(ei18nComposite.getViewer().getControl().getFont(), SWT.BOLD);

                return boldFont;
            }

            @Override
            public void dispose() {
                super.dispose();
                FontUtil.safeDispose(boldFont, italicFont);
            }
        }
        ei18nComposite.getViewer().setLabelProvider(new ViewLabelProvider());
        ei18nComposite.getViewer().setCellModifier(new ICellModifier() {
            public void modify(Object element, String property, Object value) {
                try {
                    Line line=(Line) ((TableItem) element).getData();
                    String key=line.getString();
                    List<DocumentChange> changes=Lists.newArrayList();
                    if (property.equals(EI18NComposite.KEY_COLUMN_PROPERTY)) {
                        // Key edition
                        String newKey=(String) value;
                        line.setString(newKey);//Length(0);
                        //                        keyBuffer.append(newKey);
                        if (!containsKey(StringUtils.EMPTY)) {
                            // Re add new item
                            //                            StringBuffer newItem=new StringBuffer();
                            Line newItem=new Line();
                            getKeys().add(newItem);
                            ei18nComposite.getViewer().add(newItem);
                        }

                        // Change in .properties file
                        for (Information info : infos.values()) {
                            LineProperties lineProperties=info.properties;
                            String val=lineProperties.getProperty(key);
                            if (val != null) {
                                DocumentChange change=new DocumentChange(EI18N, info.doc);
                                IRegion region=lineProperties.getRegion(key);
                                change.setEdit(new ReplaceEdit(region.getOffset(), region.getLength() + lineProperties.getLineDelimiter(key).length(), getLine(
                                        newKey, val)));
                                changes.add(change);
                            }
                        }

                        // Change in .java file
                        // TODO CME
                        //                        try {
                        //                            if (cu != null) {
                        //                                boolean found=false;
                        //                                all: for (IType type : cu.getAllTypes()) {
                        //                                    for (IField field : type.getFields()) {
                        //                                        if (CompilationUnitUtil.isValid(field) && field.getElementName().equals(newKey)) {
                        //                                            found=true;
                        //                                            break all;
                        //                                        }
                        //                                    }
                        //                                }
                        //                                if (!found) {
                        //                                    IDocument document=javaEditor.getDocumentProvider().getDocument(javaEditor.getEditorInput());
                        //                                    DocumentChange change=new DocumentChange(EI18N, document);
                        //                                    change.setEdit(CompilationUnitUtil.addField(document, cu, newKey));
                        //                                    changes.add(change);
                        //                                }
                        //                            }
                        //                        } catch (JavaModelException e1) {
                        //                            // TODO Auto-generated catch block
                        //                            e1.printStackTrace();
                        //                        }
                    } else {
                        // Translation edition
                        Information info=infos.get(property);
                        LineProperties lineProperties=info.properties;
                        // Change in .properties file
                        DocumentChange change=new DocumentChange(EI18N, info.doc);
                        //                        String val=((TranslationLine) value).getValue();
                        String val=(String) value;
                        if (!lineProperties.contains(key)) {
                            change.setEdit(new InsertEdit(0, getLine(key, val)));
                        } else {
                            IRegion region=lineProperties.getRegion(key);
                            change.setEdit(new ReplaceEdit(region.getOffset(), region.getLength() + lineProperties.getLineDelimiter(key).length(), getLine(key,
                                    val)));
                        }
                        changes.add(change);
                    }
                    for (DocumentChange change : changes) {
                        change.perform(new NullProgressMonitor());
                        IDocument document=change.getCurrentDocument(new NullProgressMonitor());
                        for (Information info : infos.values()) {
                            if (info.doc == document) {
                                info.properties.reload(document.get());
                                break;
                            }
                        }
                        // TODO CME
                        //                        LineProperties lineProperties=docs.inverse().get(document);
                        //                        if (lineProperties != null)
                        //                            lineProperties.reload(document.get());
                    }
                    ei18nComposite.getViewer().update(line, new String[] { property });
                    firePropertyChange(PROP_DIRTY);
                } catch (Exception e) {
                    Activator.log(ERROR, "Failed to modify source code", e); //$NON-NLS-1$
                }
            }

            protected String getLine(String key, String value) {
                return key + "=" + StringEscapeUtils.escapeJava(value) + IOUtils.LINE_SEPARATOR; //$NON-NLS-1$
            }

            public Object getValue(Object element, String property) {
                //                if (property == EI18NComposite.KEY_COLUMN_PROPERTY)
                //                    return ((Line) element).toString();

                Line key=(Line) element;
                if (key.isNew())
                    return StringUtils.EMPTY;

                Map<String, String> map=Maps.newHashMap();
                for (int i=0; i < ei18nComposite.getViewer().getColumnProperties().length; i++) {
                    String locale=ei18nComposite.getLocale(i);
                    if (locale != null) {
                        map.put(locale, StringUtils.defaultString(infos.get(locale).properties.getProperty(key.getString())));
                    }
                }

                return new EditionLine(property, StringUtils.defaultString(infos.get(property).properties.getProperty(key.getString())), map);
            }

            //            private LineProperties get(String property) {
            //                if (property.equals(EI18NComposite.DEFAULT_COLUMN_PROPERTY))
            //                    return props.get(0);
            //
            //                int index=ei18nComposite.getLocales().indexOf(property);
            //                return props.get(index + 1);
            //            }

            public boolean canModify(Object element, String property) {
                return true;
            }
        });
        //        Collection<StringBuffer> modifiableKeys=Lists.newArrayList(Iterables.transform(keys, STR2BUF));
        //        modifiableKeys.add(new StringBuffer());
        //        List<Line> input=Lists.newArrayList(Iterables.transform(keys, new Function<String, Line>() {
        //            
        //            public Line apply(String arg0) {
        //                return new Line(arg0);
        //            }
        //        }));
        //        input.add(new Line());
        //        Collections.sort(input);
        updateInput();
        //        ei18nComposite.getViewer().setSorter(new ViewerSorter() {
        //            
        //            public int category(Object element) {
        //                Line buf=(Line) element;
        //                return buf.length() == 0 ? 1 : 0;
        //            }
        //        });
        final CellEditor cellEditor=ei18nComposite.getViewer().getCellEditors()[0];
        cellEditor.setValidator(new ICellEditorValidator() {
            public String isValid(Object value) {
                if (!SourceVersion.isName((String) value))
                    return "Not a valid Java identifier"; //$NON-NLS-1$

                return containsKey((String) value) ? "Key already exists" : null; //$NON-NLS-1$
            }
        });
        cellEditor.addListener(new ICellEditorListener() {

            public void applyEditorValue() {
                setErrorMessage(null);
            }

            private void setErrorMessage(String message) {
                getEditorSite().getActionBars().getStatusLineManager().setErrorMessage(message);
            }

            public void cancelEditor() {
                setErrorMessage(null);
            }

            public void editorValueChanged(boolean oldValidState, boolean newValidState) {
                setErrorMessage(cellEditor.getErrorMessage());
            }
        });

        // ADD delete entry
        Menu menu=new Menu(ei18nComposite.getViewer().getControl());
        final MenuItem deleteItem=new MenuItem(menu, SWT.NONE);
        deleteItem.setText("Delete");
        deleteItem.setImage(EI18NImage.DELETE_16.getImage());
        deleteItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Line buf=(Line) ((IStructuredSelection) ei18nComposite.getViewer().getSelection()).getFirstElement();
                deleteItem(buf);
            }
        });
        ei18nComposite.getViewer().getControl().addMenuDetectListener(new MenuDetectListener() {

            public void menuDetected(MenuDetectEvent e) {
                deleteItem.setEnabled(false);
                if (e.y >= ei18nComposite.getViewer().getTable().getHeaderHeight()) {
                    Line selection=(Line) ((IStructuredSelection) ei18nComposite.getViewer().getSelection()).getFirstElement();
                    deleteItem.setEnabled(selection != null && !selection.isNew());
                }
            }
        });
        ei18nComposite.getViewer().getControl().setMenu(menu);
    }

    private void updateInput() {
        Set<Line> input=Sets.newTreeSet();
        for (Information info : infos.values()) {
            LineProperties prop=info.properties;
            for (String key : prop) {
                input.add(new Line(key));
            }
        }
        input.add(new Line());
        ei18nComposite.setInput(input);
        updateJavaContent();
    }

    private void updateJavaFile() {
        javaFile=mappingPreference.getJavaFile();

        // Clear previous Java Editor
        main: while (true) {
            for (int i=0; i < getPageCount(); i++) {
                IEditorPart part=getEditor(i);
                if (part instanceof CompilationUnitEditor) {
                    if (part.isDirty()) {
                        IFile adapter=(IFile) part.getEditorInput().getAdapter(IFile.class);
                        if (MessageDialog.openQuestion(getShell(), getShell().getText(),
                                format("Would you like to save ''{0}''?", adapter.getFullPath().toString()))) {
                            part.doSave(new NullProgressMonitor());
                        }
                    }
                    removePage(i);
                    // Refresh dirty state
                    firePropertyChange(PROP_DIRTY);
                    continue main;
                }
            }
            break;
        }

        //
        if (javaFile == null) {
            link.setText("<a href=\"change\">Associate</a>."/*, or create a <a href=\"new\">new</a> one"*/);
        } else {
            link.setText(format(
                    "Currently associated with Java file ''<a href=\"goto\">{0}</a>''. <a href=\"change\">Change</a>."/*, or create a <a href=\"new\">new</a> one"*/,
                    javaFile.getFullPath().toPortableString()));
            try {
                FileEditorInput editorInput=new FileEditorInput(javaFile);
                javaEditor=new CompilationUnitEditor();

                addPage(1, javaEditor, editorInput);
                setPageText(1, javaFile.getName());
                setPageImage(1, javaEditor.getTitleImage());
            } catch (PartInitException e) {
                Activator.log(ERROR, "Failed to create page for " + javaFile, e); //$NON-NLS-1$
            } catch (Exception e) {
                Activator.log(ERROR, "Error loadingg properties for " + javaFile, e); //$NON-NLS-1$
            }
        }

        //        updateJavaContent();
        JavaMappingExtension ext=getSelectedExtension();
        if (ext != null) {
            IDocument document=null;
            if (javaEditor != null)
                document=javaEditor.getDocumentProvider().getDocument(javaEditor.getEditorInput());
            ext.getJavaMapping().init(document, javaFile);
        }
    }

    private void updateJavaContent() {
        JavaMappingExtension ext=getSelectedExtension();
        if (ext != null) {
            IJavaMapping javaMapping=ext.getJavaMapping();
            Set<String> fieldsToRemove=javaMapping.getKeys();
            List<String> fieldsToAdd=new ArrayList<String>();
            for (Line line : getKeys()) {
                if (!fieldsToRemove.remove(line.getString())) {
                    fieldsToAdd.add(line.getString());
                }
            }

            javaMapping.syncFields(fieldsToAdd, fieldsToRemove);
        }
    }

    private IProject getProject() {
        return propertyFile.getProject();
    }

    protected void loadPropertyTab(String locale, IFile f) {
        try {
            //            Information info=new Information();
            LineProperties lineProps=new LineProperties(f);
            //            props.put(locale, lineProps);

            FileEditorInput editorInput=new FileEditorInput(f);
            TextEditor editor=new PropertiesFileEditor();
            //            editors.add(editor);

            int editorIndex=addPage(editor, editorInput);
            setPageText(editorIndex, f.getName());
            setPageImage(editorIndex, WorkbenchPlugin.getDefault().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE));

            //            docs.put(lineProps, editor.getDocumentProvider().getDocument(editor.getEditorInput()));
            IDocument doc=editor.getDocumentProvider().getDocument(editor.getEditorInput());
            infos.put(locale, new Information(f, lineProps, editor, doc));
        } catch (PartInitException e) {
            Activator.log(ERROR, "Failed to create page for " + f, e); //$NON-NLS-1$
        } catch (Exception e) {
            Activator.log(ERROR, "Error loadingg properties for " + f, e); //$NON-NLS-1$
        }
    }

    private Set<Line> getKeys() {
        return (Set<Line>) ei18nComposite.getViewer().getInput();
    }

    @Override
    protected void pageChange(int newPageIndex) {
        String selectedKey=getKeySelected();
        if (oldPageIndex > 1) {
            // TODO CME
            //          LineProperties lineProperties=getLineProperties();
            //            try {
            //                for (String removedKey : lineProperties.reload(docs.get(lineProperties).get())) {
            //                    for (Iterator<Line> iterator=getKeys().iterator(); iterator.hasNext();) {
            //                        Line buf=iterator.next();
            //                        if (buf.getString().equals(removedKey)) {
            //                            iterator.remove();
            //                            ei18nComposite.getViewer().remove(buf);
            //                        }
            //                    }
            //                }
            //                updateJavaContent();
            //                Set<String> newKeys=Sets.newHashSet(lineProperties);
            //                newKeys.removeAll(Collections2.transform(getKeys(), BUF2STR));
            //                for (String newKey : newKeys) {
            //                    this.newKeys.add(newKey);
            //                    StringBuffer buf=getKeys().get(getKeys().size() - 1);
            //                    buf.setLength(0);
            //                    buf.append(newKey);
            //                    ei18nComposite.getViewer().refresh(buf);
            //                    StringBuffer newItem=new StringBuffer();
            //                    getKeys().add(newItem);
            //                    ei18nComposite.getViewer().add(newItem);
            //                }
            //                refreshColumns((String) ei18nComposite.getViewer().getColumnProperties()[oldPageIndex - 1]);
            //            } catch (BadLocationException e) {
            //                Activator.log(ERROR, "Failed reloading file", e); //$NON-NLS-1$
            //            } catch (IOException e) {
            //                Activator.log(ERROR, "Failed reloading file", e); //$NON-NLS-1$
            //            }
        } else if (oldPageIndex == 1) {

        } else {
            newKeys.clear();
            //            refreshColumns((String) ei18nComposite.getViewer().getColumnProperties()[0]);
            // select((AbstractDecoratedTextEditor)getEditor(newPageIndex), //
            // (StringBuffer)((IStructuredSelection)ei18nComposite.getViewer().getSelection()).getFirstElement());
        }

        super.pageChange(newPageIndex);
        oldPageIndex=newPageIndex;

        setKeySelected(selectedKey);
    }

    private void setKeySelected(String selectedKey) {
        if (oldPageIndex == 0) {
            select(selectedKey);
            // TODO
            //        } else if (oldPageIndex == 1 && cu != null) {
            //            try {
            //                for (IType type : cu.getAllTypes()) {
            //                    for (IField field : type.getFields()) {
            //                        if (CompilationUnitUtil.isValid(field) && field.getElementName().equals(selectedKey)) {
            //                            // ISourceRange nameRange=field.getSourceRange();
            //                            ((JavaEditor) getActiveEditor()).setSelection(field);// getSelectionProvider().setSelection(new TextSelection(nameRange.getOffset(),
            //                            // nameRange.getOffset()));
            //                        }
            //                    }
            //                }
            //            } catch (JavaModelException e) {
            //                // NO-OP
            //            }
        } else if (oldPageIndex != -1) {
            // TODO CME
            //            LineProperties lineProperties=getLineProperties();
            //            for (String key : lineProperties) {
            //                if (key.equals(selectedKey)) {
            //                    IRegion region=lineProperties.getRegion(key);
            //                    ((ITextEditor) getActiveEditor()).selectAndReveal(region.getOffset(), key.length());
            //                }
            //            }
        }
    }

    private String getKeySelected() {
        if (oldPageIndex == 0) {
            Line buffer=(Line) ((IStructuredSelection) ei18nComposite.getViewer().getSelection()).getFirstElement();
            //            if (buffer != null)
            //                return buffer.toString();
            // TODO CME
            //        } else if (oldPageIndex == 1 && cu != null) {
            //            int offset=((ITextSelection) ((ITextEditor) getEditor(oldPageIndex)).getSelectionProvider().getSelection()).getOffset();
            //            try {
            //                IJavaElement elementAt=cu.getElementAt(offset);
            //                if (elementAt instanceof IField) {
            //                    IField field=(IField) elementAt;
            //                    if (CompilationUnitUtil.isValid(field))
            //                        return field.getElementName();
            //                }
            //            } catch (JavaModelException e) {
            //                // NO-OP
            //            }
        } else if (oldPageIndex != -1) {
            // TODO CME
            //            int startLine=((ITextSelection) ((ITextEditor) getEditor(oldPageIndex)).getSelectionProvider().getSelection()).getStartLine();
            //            LineProperties lineProperties=getLineProperties();
            //            for (String key : lineProperties) {
            //                if (lineProperties.getLineNumber(key) == startLine)
            //                    return key;
            //            }
        }
        return null;
    }

    //    protected void refreshColumns(String... columns) {
    //    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        //        try {
        propertyFile=(IFile) input.getAdapter(IFile.class);
        mappingPreference=new MappingPreference(propertyFile);

        //
        // try {
        // IJavaElement elt=JavaCore.create(file.getParent());
        //
        // List<IPackageFragment> pkgs;
        // if (elt instanceof IPackageFragment)
        // pkgs=Collections.singletonList((IPackageFragment) elt);
        // else if (elt instanceof IPackageFragmentRoot)
        // pkgs=Lists.newArrayList(Iterables.filter(Arrays.asList(((IPackageFragmentRoot) elt).getChildren()), IPackageFragment.class));
        // else
        // pkgs=Collections.emptyList();
        //
        // for (IPackageFragment pkg : pkgs) {
        // for (ICompilationUnit cu : pkg.getCompilationUnits()) {
        // if (CompilationUnitUtil.isValid(cu)) {
        // this.cu=cu;
        // }
        // }
        // }
        // } catch (JavaModelException e2) {
        //                Activator.log(ERROR, "Failed to read compilation unit", e2); //$NON-NLS-1$
        // }

        setSite(site);
        setPartName(propertyFile.getName());

        //            IType type=FindBrokenNLSKeysAction.getAccessorType(file);
        //            if (type != null) {
        //                cu=type.getCompilationUnit();
        //                javaFile=(IFile) type.getResource();
        //            }
        setInput(new FileEditorInput(propertyFile));
        site.setSelectionProvider(new MultiPageSelectionProvider(this));
        //        } catch (CoreException e) {
        //            throw new PartInitException("Failed to init", e); //$NON-NLS-1$
        //        }
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        //        Builder<IEditorPart> builder=new ImmutableList.Builder<IEditorPart>().addAll(editors);
        List<IEditorPart> list=Lists.newArrayListWithCapacity(infos.size() + 1);
        for (Information info : infos.values())
            list.add(info.editor);
        if (javaFile != null)
            list.add(javaEditor);
        for (IEditorPart part : list) {
            if (part.isDirty())
                part.doSave(monitor);
        }
    }

    @Override
    public void doSaveAs() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    protected boolean containsKey(String str) {
        for (Line line : getKeys()) {
            if (line.getString() != null && line.getString().equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }

    public void select(String key) {
        if (StringUtils.isNotEmpty(key)) {
            for (Line buf : getKeys()) {
                if (buf.getString().equals(key)) {
                    ei18nComposite.getViewer().setSelection(new StructuredSelection(buf), true);
                    //                    ei18nComposite.getViewer().reveal(buf);
                }
            }
        }
    }

    protected void deleteItem(Line line) {
        String key=line.getString();
        // Remove from .properties files
        for (Information info : infos.values()) {
            LineProperties property=info.properties;
            if (property.contains(key)) {
                DocumentChange change=new DocumentChange(EI18N, info.doc);
                try {
                    IRegion region=property.getRegion(key);
                    change.setEdit(new DeleteEdit(region.getOffset(), region.getLength() + property.getLineDelimiter(key).length()));
                    change.perform(new NullProgressMonitor());
                    IDocument document=change.getCurrentDocument(new NullProgressMonitor());
                    //                    LineProperties lineProperties=docs.inverse().get(document);
                    property.reload(document.get());
                } catch (Exception e1) {
                    Activator.log(ERROR, "Failed to delete source code in " + property.getFile(), e1); //$NON-NLS-1$
                }
            }
        }
        // Remove from .java files
        JavaMappingExtension ext=getSelectedExtension();
        if (ext != null) {
            ext.getJavaMapping().syncFields(Collections.<String> emptyList(), Collections.singleton(key));
        }
        //        ext.getJavaMapping().removeField(buf.toString());
        //        try {
        //            IDocument document=javaEditor.getDocumentProvider().getDocument(javaEditor.getEditorInput());
        //            DocumentChange change=new DocumentChange(EI18N, document);
        //            change.setEdit(CompilationUnitUtil.removeField(document, cu, buf.toString()));
        //            change.perform(new NullProgressMonitor());
        //        } catch (Exception e1) {
        //            Activator.log(ERROR, "Failed to delete source code in " + javaFile, e1); //$NON-NLS-1$
        //        }
        // Remove from viewer
        ei18nComposite.getViewer().remove(line);
        firePropertyChange(PROP_DIRTY);
    }

    private JavaMappingExtension getSelectedExtension() {
        return EI18nPropertyPage.getExtension(getProject());//(JavaMappingExtension) ((IStructuredSelection) comboViewer.getSelection()).getFirstElement();
    }

    private Shell getShell() {
        return getSite().getShell();
    }
}
