package org.eclipse.etools.ei18n.editors;

import static java.text.MessageFormat.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;

import javax.lang.model.SourceVersion;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.etools.Activator;
import org.eclipse.etools.ei18n.EI18NImage;
import org.eclipse.etools.ei18n.dialogs.JavaFileSelectionDialog;
import org.eclipse.etools.ei18n.extensions.IJavaMapping;
import org.eclipse.etools.ei18n.extensions.JavaMappingExtensionManager.JavaMappingExtension;
import org.eclipse.etools.ei18n.properties.EI18nPropertyPage;
import org.eclipse.etools.ei18n.util.EI18NConstants;
import org.eclipse.etools.ei18n.util.Escapers;
import org.eclipse.etools.ei18n.util.FontUtil;
import org.eclipse.etools.ei18n.util.LineProperties;
import org.eclipse.etools.ei18n.util.MappingPreference;
import org.eclipse.etools.ei18n.util.PreferencesUtil;
import org.eclipse.etools.ei18n.util.StorageUtil;
import org.eclipse.etools.ei18n.util.TranslationCellEditor;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JarEntryEditorInput;
import org.eclipse.jdt.internal.ui.propertiesfileeditor.PropertiesFileEditor;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.part.MultiPageSelectionProvider;
import org.osgi.service.prefs.BackingStoreException;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class EI18NEditorPart extends MultiPageEditorPart
{
    public static final String ID=EI18NEditorPart.class.getName();

    public static final String KEY_COLUMN_PROPERTY="key"; //$NON-NLS-1$
    public static final String ADD_COLUMN_PROPERTY="add"; //$NON-NLS-1$
    private static final String EI18N="EI18N"; //$NON-NLS-1$
    //    private EI18NComposite ei18nComposite;
    private int oldPageIndex=-1;

    private FilteredTree viewer;

    private static class MyContentProvider extends ArrayContentProvider implements ITreeContentProvider {
        public static final MyContentProvider INSTANCE=new MyContentProvider();

        public Object[] getChildren(Object parentElement) {
            // TODO Auto-generated method stub
            return null;
        }

        public Object getParent(Object element) {
            // TODO Auto-generated method stub
            return null;
        }

        public boolean hasChildren(Object element) {
            return false;
        }
    }

    private static class Information {
        private final IStorage file;
        private final LineProperties properties;
        private final IDocument doc;
        private final TextEditor editor;

        public Information(IStorage file, LineProperties lineProps, TextEditor editor, IDocument doc) {
            this.file=file;
            properties=lineProps;
            this.editor=editor;
            this.doc=doc;
        }
    }

    private class ViewLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider, ITableFontProvider {
        private Font boldFont;
        private Font italicFont;

        @Override
        public String getText(Object element) {
            StringBuffer buf=new StringBuffer();
            for (int i=0; i < getNbLocales(); i++) {
                if (buf.length() > 0)
                    buf.append('|');

                buf.append(getColumnText(element, i));
            }
            return buf.toString();
        }

        public String getColumnText(Object obj, int index) {
            Line str=(Line) obj;
            if (str.isNew())
                return index > 0 ? StringUtils.EMPTY : "New"; //$NON-NLS-1$
            if (index == 0)
                return str.getString();
            else {
                String locale=getLocale(index);
                if (locale != null) {
                    Information info=infos.get(locale);
                    if (info != null)
                        return info.properties.getProperty(str.getString()); // TODO
                }
            }

            return null;
        }

        public Image getColumnImage(Object element, int index) {
            String locale=getLocale(index);
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
                italicFont=FontUtil.derivate(viewer.getViewer().getControl().getFont(), SWT.ITALIC);

            return italicFont;
        }

        private Font boldFont() {
            if (boldFont == null)
                boldFont=FontUtil.derivate(viewer.getViewer().getControl().getFont(), SWT.BOLD);

            return boldFont;
        }

        @Override
        public void dispose() {
            super.dispose();
            FontUtil.safeDispose(boldFont, italicFont);
        }
    }

    private final Map<String, Information> infos=Maps.newHashMap();
    private final Set<String> newKeys=Sets.newHashSet();
    private CompilationUnitEditor javaEditor;
    private IFile javaFile;
    private Link link;
    //    private IFile propertyFile;
    private MappingPreference mappingPreference;

    private Combo combo;

    private IStorage storage;

    @Override
    protected void createPages() {
        // Get all concerned files

        // ADD the main page
        Composite composite=new Composite(getContainer(), SWT.NONE);
        GridLayout gLayout=new GridLayout(3, false);
        composite.setLayout(gLayout);

        link=new Link(composite, SWT.NONE);
        link.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if ("change".equals(e.text)) { //$NON-NLS-1$
                    changeAssociation();
                } else if ("manage".equals(e.text)) { //$NON-NLS-1$
                    try {
                        mappingPreference.manage();
                        updateJavaFile();
                    } catch (BackingStoreException e1) {
                        Activator.logError("Failed to store mapping pref (manage)", e1); //$NON-NLS-1$
                    }
                } else if ("unmanage".equals(e.text)) { //$NON-NLS-1$
                    try {
                        mappingPreference.unmanage();
                        updateJavaFile();
                    } catch (BackingStoreException e1) {
                        Activator.logError("Failed to store mapping pref (unmanage)", e1); //$NON-NLS-1$
                    }
                } else if ("goto".equals(e.text)) { //$NON-NLS-1$
                    setActivePage(1);
                }
            }

            private void changeAssociation() {
                JavaFileSelectionDialog dialog=new JavaFileSelectionDialog(getShell(), "Please select the compilation unit it is linked with", getProject(),
                        javaFile == null ? (IFile) getPropertyFile().getParent().findMember("Messages.java") : javaFile);

                if (dialog.open() == Window.OK) {
                    try {
                        IFile file=(IFile) ((org.eclipse.jdt.internal.core.CompilationUnit) dialog.getFirstResult()).getCorrespondingResource();
                        mappingPreference.set(file);
                        updateJavaFile();
                    } catch (CoreException e1) {
                        Activator.logError("Failed to set association", e1); //$NON-NLS-1$
                    } catch (BackingStoreException e) {
                        Activator.logError("Failed to store mapping pref (bind)", e); //$NON-NLS-1$
                    }

                }
            }
        });
        link.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        new Label(composite, SWT.NONE).setText("Characters escaping: ");
        combo=new Combo(composite, SWT.READ_ONLY);
        for (Escapers enc : Escapers.values()) {
            combo.add(enc.name());
        }
        combo.select(mappingPreference.getEncoding().ordinal());
        combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    mappingPreference.setEncoding(Escapers.values()[combo.getSelectionIndex()]);
                } catch (BackingStoreException e1) {
                    Activator.logError("Failed to store mapping pref (encoding)", e1); //$NON-NLS-1$
                }
            }
        });

        viewer=new FilteredTree(composite, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL, new PatternFilter(), true);
        viewer.getViewer().setContentProvider(MyContentProvider.INSTANCE);
        Tree table=viewer.getViewer().getTree();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        createColumn(500, "Key"); //$NON-NLS-1$
        createColumn(200, "Default Traduction").setImage(EI18NImage.LOGO_16.getImage()); //$NON-NLS-1$

        viewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, gLayout.numColumns, 1));
        int mainPageIndex=addPage(composite);
        setPageText(mainPageIndex, EI18N);
        setPageImage(mainPageIndex, EI18NImage.LOGO_16.getImage());

        //        props=Lists.newArrayListWithCapacity(localFiles.size() + 1);
        //        editors=Lists.newArrayListWithCapacity(localFiles.size() + 1);

        //        if (javaFile != null)
        //            loadJavaEditorTab();
        updateJavaFile();

        try {
            Matcher matcher=EI18NConstants.PATTERN.matcher(storage.getName());
            if (matcher.matches()) {
                String baseName=matcher.group(1);
                //                IJarEntryResource
                Map<String, IStorage> map=new TreeMap<String, IStorage>();
                map.put(EMPTY, storage);
                //                IContainer folder=getPropertyFile().getParent();
                for (IStorage res : StorageUtil.members(storage)/*folder.members()*/) {
                    String name=res.getName();

                    //                    if ((matcher=EI18NConstants.PATTERN.matcher(name)).matches() && baseName.equals(matcher.group(EI18NConstants.BASE_NAME_GROUP))) {
                    //                        loadPropertyTab(EMPTY, res);
                    //                    } else
                    if ((matcher=EI18NConstants.LOCALE_PATTERN.matcher(name)).matches()
                            && baseName.equals(matcher.group(EI18NConstants.BASE_NAME_GROUP))) {
                        map.put(matcher.group(EI18NConstants.LOCALE_GROUP), res);
                    }
                }
                for (Entry<String, IStorage> entry : map.entrySet()) {
                    loadPropertyTab(entry.getKey(), entry.getValue());
                }
                setLocales(infos.keySet());
            }
        } catch (CoreException e1) {
            Activator.logError("Failed loading locales", e1); //$NON-NLS-1$
        }

        viewer.getViewer().setLabelProvider(new ViewLabelProvider());
        viewer.getViewer().setCellModifier(new ICellModifier() {
            public void modify(Object element, String property, Object value) {
                try {
                    Line line=(Line) ((TreeItem) element).getData();
                    String key=line.getString();
                    List<DocumentChange> changes=Lists.newArrayList();
                    if (property.equals(KEY_COLUMN_PROPERTY)) {
                        // Key edition
                        String newKey=(String) value;
                        line.setString(newKey);//Length(0);
                        //                        keyBuffer.append(newKey);
                        if (!hasNew()) {
                            // Re add new item
                            //                            StringBuffer newItem=new StringBuffer();
                            Line newItem=new Line();
                            getKeys().add(newItem);
                            viewer.getViewer().add(viewer.getViewer().getInput(), newItem);
                        }

                        // Change in .properties file
                        for (Information info : infos.values()) {
                            LineProperties lineProperties=info.properties;
                            String val=lineProperties.getProperty(key);
                            if (val != null) {
                                DocumentChange change=new DocumentChange(EI18N, info.doc);
                                IRegion region=lineProperties.getRegion(key);
                                change.setEdit(new ReplaceEdit(region.getOffset(), region.getLength() + lineProperties.getLineDelimiter(key).length(), getLine(
                                        info,
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
                            change.setEdit(new InsertEdit(0, getLine(info, key, val)));
                        } else {
                            IRegion region=lineProperties.getRegion(key);
                            change.setEdit(new ReplaceEdit(region.getOffset(), region.getLength() + lineProperties.getLineDelimiter(key).length(), getLine(
                                    info, key,
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
                    viewer.getViewer().update(line, new String[] { property });
                    firePropertyChange(PROP_DIRTY);
                } catch (Exception e) {
                    Activator.logError("Failed to modify source code", e); //$NON-NLS-1$
                }
            }

            protected String getLine(Information info, String key, String value) {
                IFile file=(IFile) info.file;
                String str=mappingPreference.getEncoding().encode(value, file);
                return key + "=" + str + PreferencesUtil.getLineDelimiter(file); //$NON-NLS-1$
            }

            public Object getValue(Object element, String property) {
                if (property == KEY_COLUMN_PROPERTY)
                    return Strings.nullToEmpty(((Line) element).getString());

                Line key=(Line) element;
                if (key.isNew())
                    return StringUtils.EMPTY;

                Map<String, String> map=Maps.newHashMap();
                for (int i=0; i < viewer.getViewer().getColumnProperties().length; i++) {
                    String locale=getLocale(i);
                    if (locale != null) {
                        map.put(locale, StringUtils.defaultString(infos.get(locale).properties.getProperty(key.getString())));
                    }
                }

                return new EditionLine(property, StringUtils.defaultString(infos.get(property).properties.getProperty(key.getString())), map);
            }

            public boolean canModify(Object element, String property) {
                return mappingPreference.isEditable();
            }
        });
        updateInput();

        // ADD delete entry
        Menu menu=new Menu(viewer.getViewer().getControl());
        final MenuItem deleteItem=new MenuItem(menu, SWT.NONE);
        deleteItem.setText("Delete");
        deleteItem.setImage(EI18NImage.DELETE_16.getImage());
        deleteItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Line buf=(Line) ((IStructuredSelection) viewer.getViewer().getSelection()).getFirstElement();
                deleteItem(buf);
            }
        });
        viewer.getViewer().getControl().addMenuDetectListener(new MenuDetectListener() {

            public void menuDetected(MenuDetectEvent e) {
                deleteItem.setEnabled(false);
                if (e.y >= viewer.getViewer().getTree().getHeaderHeight()) {
                    Line selection=(Line) ((IStructuredSelection) viewer.getViewer().getSelection()).getFirstElement();
                    deleteItem.setEnabled(selection != null && !selection.isNew());
                }
            }
        });
        viewer.getViewer().getControl().setMenu(menu);
    }

    public int getNbLocales() {
        return viewer.getViewer().getColumnProperties().length;
    }

    private void updateInput() {
        Set<Line> input=Sets.newTreeSet();
        for (Information info : infos.values()) {
            LineProperties prop=info.properties;
            for (String key : prop) {
                input.add(new Line(key));
            }
        }
        if (mappingPreference.isEditable())
            input.add(new Line());
        setInput(input);
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
        if (!mappingPreference.isEditable()) {
            link.setText(EMPTY);
        } else if (!mappingPreference.isManaged()) {
            link.setText("<a href=\"change\">Bind with a java file</a>, or simply <a href=\"manage\">manage it</a>."/*, or create a <a href=\"new\">new</a> one"*/);
        } else if (javaFile == null) {
            link.setText("Currently managed. <a href=\"unmanage\">Unmanage it</a>."/*, or create a <a href=\"new\">new</a> one"*/);
        } else {
            link.setText(format(
                    "Currently binded with Java file ''<a href=\"goto\">{0}</a>''. <a href=\"change\">Change it</a>, or <a href=\"unmanage\">unbind it</a>."/*, or create a <a href=\"new\">new</a> one"*/,
                    javaFile.getFullPath().toPortableString()));
            try {
                FileEditorInput editorInput=new FileEditorInput(javaFile);
                javaEditor=new CompilationUnitEditor();

                addPage(1, javaEditor, editorInput);
                setPageText(1, javaFile.getName());
                setPageImage(1, javaEditor.getTitleImage());
            } catch (PartInitException e) {
                Activator.logError("Failed to create page for " + javaFile, e); //$NON-NLS-1$
            } catch (Exception e) {
                Activator.logError("Error loadingg properties for " + javaFile, e); //$NON-NLS-1$
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
        IFile propertyFile=getPropertyFile();
        return propertyFile == null ? null : propertyFile.getProject();
    }

    protected void loadPropertyTab(String locale, IStorage f) {
        try {
            LineProperties lineProps=new LineProperties(f);

            IEditorInput editorInput;
            if (f instanceof IFile)
                editorInput=new FileEditorInput((IFile) f);
            else
                editorInput=new JarEntryEditorInput(f);

            TextEditor editor=new PropertiesFileEditor();

            int editorIndex=addPage(editor, editorInput);
            setPageText(editorIndex, f.getName());
            setPageImage(editorIndex, WorkbenchPlugin.getDefault().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE));

            //            docs.put(lineProps, editor.getDocumentProvider().getDocument(editor.getEditorInput()));
            IDocument doc=editor.getDocumentProvider().getDocument(editor.getEditorInput());
            infos.put(locale, new Information(f, lineProps, editor, doc));
        } catch (PartInitException e) {
            Activator.logError("Failed to create page for " + f, e); //$NON-NLS-1$
        } catch (Exception e) {
            Activator.logError("Error loadingg properties for " + f, e); //$NON-NLS-1$
        }
    }

    private Set<Line> getKeys() {
        return (Set<Line>) viewer.getViewer().getInput();
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
            Line buffer=(Line) ((IStructuredSelection) viewer.getViewer().getSelection()).getFirstElement();
            if (buffer != null)
                return buffer.toString();

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
        try {
            storage=((IStorageEditorInput) input).getStorage();
            mappingPreference=new MappingPreference(storage);
            //        if(propertyFile!=null) {
            //        } else if(input instanceof IStorageEditorInput) {
            //            ((IStorageEditorInput)input).getStorage().getName()
            //        }

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
            setPartName(storage.getName());

            //            IType type=FindBrokenNLSKeysAction.getAccessorType(file);
            //            if (type != null) {
            //                cu=type.getCompilationUnit();
            //                javaFile=(IFile) type.getResource();
            //            }
            setInput(input /* new FileEditorInput(propertyFile) */);
            site.setSelectionProvider(new MultiPageSelectionProvider(this));
        } catch (CoreException e) {
            throw new PartInitException("Failed to init", e); //$NON-NLS-1$
        }
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

    protected boolean hasNew() {
        for (Line line : getKeys()) {
            if (line.isNew()) {
                return true;
            }
        }
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
                if (Strings.nullToEmpty(buf.getString()).equals(key)) {
                    viewer.getViewer().setSelection(new StructuredSelection(buf), true);
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
                    Activator.logError("Failed to delete source code in " + property.getFile(), e1); //$NON-NLS-1$
                }
            }
        }
        // Remove from .java files
        JavaMappingExtension ext=getSelectedExtension();
        if (ext != null) {
            ext.getJavaMapping().syncFields(Collections.<String> emptyList(), Collections.singleton(key));
        }
        //        ext.getJavaMapping().removeField(key);
        //        try {
        //            IDocument document=javaEditor.getDocumentProvider().getDocument(javaEditor.getEditorInput());
        //            DocumentChange change=new DocumentChange(EI18N, document);
        //            change.setEdit(CompilationUnitUtil.removeField(document, cu, buf.toString()));
        //            change.perform(new NullProgressMonitor());
        //        } catch (Exception e1) {
        //            Activator.log(ERROR, "Failed to delete source code in " + javaFile, e1); //$NON-NLS-1$
        //        }
        // Remove from viewer
        viewer.getViewer().remove(line);
        firePropertyChange(PROP_DIRTY);
    }

    private JavaMappingExtension getSelectedExtension() {
        IProject project=getProject();
        return project == null ? null : EI18nPropertyPage.getExtension(project);
    }

    private Shell getShell() {
        return getSite().getShell();
    }

    @Override
    public void setFocus() {
        viewer.getViewer().getControl().setFocus();
    }

    private static class MyViewerComparator extends ViewerComparator {
        private final int column;
        private final boolean invert;

        public MyViewerComparator(int column, boolean invert) {
            this.column=column;
            this.invert=invert;
        }

        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            if (((Line) e1).isNew())
                return 1;
            if (((Line) e2).isNew())
                return -1;

            String name1=getLabel(viewer, e1);
            String name2=getLabel(viewer, e2);

            if (invert)
                return StringUtils.defaultString(name2).compareToIgnoreCase(StringUtils.defaultString(name1));
            else
                return StringUtils.defaultString(name1).compareToIgnoreCase(StringUtils.defaultString(name2));
        }

        private String getLabel(Viewer viewer, Object e) {
            return ((ITableLabelProvider) ((TreeViewer) viewer).getLabelProvider()).getColumnText(e, column);
        }
    }

    protected TreeColumn createColumn(int width, String text) {
        final int columnCount=viewer.getViewer().getTree().getColumnCount();

        TreeColumn column=new TreeColumn(viewer.getViewer().getTree(), SWT.NONE);
        column.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                TreeColumn column=(TreeColumn) event.widget;
                Tree tree=viewer.getViewer().getTree();
                int direction=tree.getSortDirection();
                int newDirection;
                switch (direction) {
                    case SWT.NONE: {
                        viewer.getViewer().setComparator(new MyViewerComparator(columnCount, false));
                        newDirection=SWT.UP;
                        break;
                    }
                    case SWT.UP: {
                        viewer.getViewer().setComparator(new MyViewerComparator(columnCount, true));
                        newDirection=SWT.DOWN;
                        break;
                    }
                    default:
                        viewer.getViewer().setComparator(null);
                        newDirection=SWT.NONE;
                }
                tree.setSortDirection(newDirection);
                tree.setSortColumn(newDirection == SWT.NONE ? null : column);
            }
        });
        column.setWidth(width);
        column.setText(text);
        return column;
    }

    public void setInput(Set<Line> input) {
        viewer.getViewer().setInput(input);
    }

    public void setLocales(Collection<String> locales) {
        createLocaleColumn(locales);
        addAddColumn();
    }

    protected void addAddColumn() {
        if (!mappingPreference.isEditable())
            return;

        final TreeColumn addColumn=new TreeColumn(viewer.getViewer().getTree(), SWT.NONE);
        addColumn.setWidth(30);
        addColumn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                InputDialog dialog=new InputDialog(getShell(), StringUtils.EMPTY, "Please enter a new locale", null, new IInputValidator() { //$NON-NLS-1$
                            public String isValid(String newText) {
                                try {
                                    LocaleUtils.toLocale(newText);
                                    return null;
                                } catch (IllegalArgumentException e) {
                                    return "Please enter a valid locale (ie.: fr)"; //$NON-NLS-1$
                                }
                            }
                        });
                if (dialog.open() == Window.OK) {
                    String locale=dialog.getValue();
                    addColumn.setWidth(100);
                    addColumn.setImage(EI18NImage.getImage(LocaleUtils.toLocale(locale)));
                    createLocale(locale);
                    addColumn.setText(locale);
                    viewer.getViewer().setCellEditors(ArrayUtils.add(viewer.getViewer().getCellEditors(), createLocaleEditor(locale)));
                    String[] properties=(String[]) viewer.getViewer().getColumnProperties();
                    viewer.getViewer().setColumnProperties(ArrayUtils.add(properties, properties.length - 1, locale));
                    addAddColumn();
                    viewer.getViewer().refresh();
                }
            }
        });
        addColumn.setImage(EI18NImage.ADD_16.getImage());
    }

    //    protected void createLocale(String locale) {
    //    }

    public TreeViewer getViewer() {
        return viewer.getViewer();
    }

    public Control getControl() {
        return viewer;
    }

    private CellEditor createLocaleEditor(String locale) {
        return new TranslationCellEditor(viewer.getViewer().getTree(), locale) {
            private EditionLine line;

            @Override
            protected Map<String, String> getStringsToTranslate() {
                //                String[] columnProperties=(String[]) viewer.getColumnProperties();
                //                Map<String, String> result=Maps.newHashMapWithExpectedSize(columnProperties.length - 2);
                //                for (int i=1; i < columnProperties.length - 1; i++) {
                //                    result.put(columnProperties[i], get);
                //                }
                return line.getMap();
            }

            @Override
            protected void updateContents(Object value) {
                if (value instanceof EditionLine) {
                    line=(EditionLine) value;
                    super.updateContents(line.getValue());
                } else
                    super.updateContents(value);
            }
        };
    }

    protected void createLocaleColumn(Collection<String> locales) {
        List<String> columnProps=Lists.newArrayList();
        columnProps.add(KEY_COLUMN_PROPERTY);
        columnProps.add(EMPTY);
        List<CellEditor> editors=Lists.newArrayList();
        final CellEditor firstCellEditor=new TextCellEditor(viewer.getViewer().getTree());
        firstCellEditor.setValidator(new ICellEditorValidator() {
            public String isValid(Object value) {
                String line=(String) value;
                if (StringUtils.isEmpty(line))
                    return "Empty key";

                if (!SourceVersion.isName(line))
                    return "Not a valid Java identifier"; //$NON-NLS-1$

                return containsKey(line) ? "Key already exists" : null; //$NON-NLS-1$
            }
        });
        firstCellEditor.addListener(new ICellEditorListener() {

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
                setErrorMessage(firstCellEditor.getErrorMessage());
            }
        });

        editors.add(firstCellEditor);

        CellEditor cellEditor=createLocaleEditor(EMPTY);
        editors.add(cellEditor);
        for (String locale : locales) {
            if (!locale.isEmpty()) {
                TreeColumn column=createColumn(100, locale);
                column.setImage(EI18NImage.getImage(LocaleUtils.toLocale(locale)));
                //                column.setData(locale);
                columnProps.add(locale);
                editors.add(createLocaleEditor(locale));
            }
        }
        columnProps.add(ADD_COLUMN_PROPERTY);
        viewer.getViewer().setColumnProperties(columnProps.toArray(new String[] {}));
        viewer.getViewer().setCellEditors(editors.toArray(new CellEditor[] {}));
    }

    public String getLocale(int columnIndex) {
        Object[] columnProperties=viewer.getViewer().getColumnProperties();
        if (columnProperties != null) {
            String columnProperty=(String) columnProperties[columnIndex];
            if (ADD_COLUMN_PROPERTY.equals(columnProperty) || KEY_COLUMN_PROPERTY.equals(columnProperty))
                return null;
            return columnProperty;
        }
        return null;
    }

    private IFile getPropertyFile() {
        return (IFile) getEditorInput().getAdapter(IFile.class);
    }

    protected void createLocale(String locale) {
        // TODO CME
        IFile newFile=getPropertyFile().getParent().getFile(new Path("messages_" + locale + ".properties")); //$NON-NLS-1$//$NON-NLS-2$
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
            Activator.logError("Failed creating file " + newFile, e); //$NON-NLS-1$
        }
    }
}
