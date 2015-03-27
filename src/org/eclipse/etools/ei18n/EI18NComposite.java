package org.eclipse.etools.ei18n;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.etools.ei18n.editors.EditionLine;
import org.eclipse.etools.ei18n.editors.Line;
import org.eclipse.etools.ei18n.util.TranslationCellEditor;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

import com.google.common.collect.Lists;

public class EI18NComposite {
    public static final String KEY_COLUMN_PROPERTY="key"; //$NON-NLS-1$
    public static final String ADD_COLUMN_PROPERTY="add"; //$NON-NLS-1$

    private final FilteredTree viewer;

    public EI18NComposite(Composite parent) {
        viewer=new FilteredTree(parent, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL, new PatternFilter(), true);
        viewer.getViewer().setContentProvider(ArrayContentProvider.getInstance());
        Tree table=viewer.getViewer().getTree();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        createColumn(500, "Key"); //$NON-NLS-1$
        createColumn(200, "Default Traduction").setImage(EI18NImage.LOGO_16.getImage()); //$NON-NLS-1$
    }

    public void setFocus() {
        viewer.getViewer().getControl().setFocus();
    }

    protected TreeColumn createColumn(int width, String text) {
        TreeColumn column=new TreeColumn(viewer.getViewer().getTree(), SWT.NONE);
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

    protected void createLocale(String value) {
    }

    public TreeViewer getViewer() {
        return viewer.getViewer();
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
        // while (viewer.getTable().getColumnCount() > 2)
        // viewer.getTable().getColumn(2).dispose();
        List<String> columnProps=Lists.newArrayList();
        columnProps.add(KEY_COLUMN_PROPERTY);
        columnProps.add(EMPTY);
        List<CellEditor> editors=Lists.newArrayList();
        CellEditor cellEditor=new TextCellEditor(viewer.getViewer().getTree());
        editors.add(cellEditor);
        cellEditor=createLocaleEditor(EMPTY);
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
        String columnProperty=(String) viewer.getViewer().getColumnProperties()[columnIndex];
        if (ADD_COLUMN_PROPERTY.equals(columnProperty) || KEY_COLUMN_PROPERTY.equals(columnProperty))
            return null;
        return columnProperty;
    }

    protected Shell getShell() {
        return viewer.getViewer().getControl().getShell();
    }
}
