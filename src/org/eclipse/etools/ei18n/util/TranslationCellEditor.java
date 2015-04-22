package org.eclipse.etools.ei18n.util;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.etools.Activator;
import org.eclipse.etools.ei18n.services.BingTranslatorService;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.ListDialog;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public abstract class TranslationCellEditor extends TextAndDialogCellEditor {
    private final String toLocale;

    public TranslationCellEditor(Composite parent, String toLocale) {
        super(parent);
        this.toLocale=toLocale;
    }

    private static class Translation {
        private final String src;
        private final String dst;
        private final String dstLowerCase;

        public Translation(String src, String dst) {
            this.src=src;
            this.dst=dst;
            dstLowerCase=dst.toLowerCase().trim();
        }

        @Override
        public int hashCode() {
            return dstLowerCase.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return dstLowerCase.equals(((Translation) obj).dstLowerCase);
        }

    }

    @Override
    protected Object openDialogBox(Control cellEditorWindow) {
        final Map<String, String> toTranslate=getStringsToTranslate();
        try {
            if (toTranslate != null && !toTranslate.isEmpty()) {
                final List<Translation> translations=Lists.newArrayListWithCapacity(toTranslate.size());

                ProgressMonitorDialog dialogMonitor=new ProgressMonitorDialog(getControl().getShell());

                dialogMonitor.run(true, true, new IRunnableWithProgress() {
                    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                        monitor.beginTask("Checking translation...", toTranslate.size()); //$NON-NLS-1$
                        try {
                            BingTranslatorService bingTranslatorService=new BingTranslatorService();
                            for (Entry<String, String> entry : toTranslate.entrySet()) {
                                String fromLocale=defaultLocale(entry.getKey());
                                String destLocale=defaultLocale(toLocale);
                                if (!fromLocale.equals(destLocale) && StringUtils.isNotBlank(entry.getValue())) {
                                    String translate=bingTranslatorService.translate(entry.getValue(), fromLocale, destLocale);
                                    translations.add(new Translation(entry.getValue(), translate));
                                }
                                if (monitor.isCanceled())
                                    break;
                                monitor.worked(1);
                            }
                        } catch (Exception e) {
                            throw new InvocationTargetException(e);
                        } finally {
                            monitor.done();
                        }
                    }

                    private String defaultLocale(String key) {
                        if (key == null || key.isEmpty()) {
                            return Locale.getDefault().getLanguage();
                        }

                        return key;
                    }
                });
                Set<Translation> translationSet=Sets.newHashSet(translations);
                if (translationSet.size() == 1) {
                    return translationSet.iterator().next().dst;
                } else if (!translationSet.isEmpty()) {
                    ListDialog dialog=new ListDialog(getControl().getShell()) {
                        {
                            setDialogBoundsSettings(Activator.getDefault().getOrCreateDialogSettings(getClass()), Dialog.DIALOG_DEFAULT_BOUNDS);
                        }

                        @Override
                        protected Control createDialogArea(Composite container) {
                            Control result=super.createDialogArea(container);
                            getTableViewer().setLabelProvider(new StyledCellLabelProvider() {
                                @Override
                                public void update(ViewerCell cell) {
                                    Translation t=(Translation) cell.getItem().getData();
                                    String suffix=" - " + t.src; //$NON-NLS-1$
                                    cell.setText(t.dst + suffix);
                                    cell.setStyleRanges(new StyleRange[] {
                                    new StyleRange(t.dst.length(), suffix.length(), cell.getControl().getDisplay().getSystemColor(SWT.COLOR_GRAY), null,
                                            SWT.ITALIC) });
                                }
                            });
                            return result;
                        }
                    };
                    dialog.setContentProvider(new ArrayContentProvider());
                    dialog.setLabelProvider(new LabelProvider());
                    dialog.setInput(translations);
                    if (dialog.open() == Window.OK)
                        return ((Translation) dialog.getResult()[0]).dst;
                }
            }
        } catch (Exception e) {
            Activator.logError("Failed to find translation(s) for " + StringUtils.join(toTranslate, ", "), e); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return StringUtils.EMPTY;
    }

    protected abstract Map<String, String> getStringsToTranslate();

    //    protected abstract IFile getSelectedFile();
}
