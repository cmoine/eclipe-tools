package org.eclipse.etools.ei18n.util;

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.etools.Activator;
import org.eclipse.etools.ei18n.services.BingTranslatorService;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.ListDialog;

import com.google.common.collect.Maps;

public abstract class TranslationCellEditor extends TextAndDialogCellEditor {
    private final String toLocale;

    public TranslationCellEditor(Composite parent, String toLocale) {
        super(parent);
        this.toLocale=toLocale;
    }

    @Override
    protected Object openDialogBox(Control cellEditorWindow) {
        final Map<String, String> toTranslate=getStringsToTranslate();
        try {
            if (toTranslate != null && !toTranslate.isEmpty()) {
                final Map<String, String> translations=Maps.newHashMapWithExpectedSize(toTranslate.size());
                ProgressMonitorDialog dialogMonitor=new ProgressMonitorDialog(getControl().getShell());
                //                final IFile selectedFile=getSelectedFile();
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
                                    String key=translate.toLowerCase().trim();
                                    if (!translations.containsKey(key)) {
                                        translations.put(key, translate);
                                    }
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
                if (translations.size() == 1) {
                    return translations.values().iterator().next();
                } else if (!translations.isEmpty()) {
                    ListDialog dialog=new ListDialog(getControl().getShell());
                    dialog.setContentProvider(new ArrayContentProvider());
                    dialog.setLabelProvider(new LabelProvider());
                    dialog.setInput(translations.values());
                    if (dialog.open() == Window.OK)
                        return dialog.getResult()[0];
                }
            }
        } catch (Exception e) {
            Activator.log(IStatus.ERROR, "Failed to find translation(s) for " + StringUtils.join(toTranslate, ", "), e); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return StringUtils.EMPTY;
    }

    protected abstract Map<String, String> getStringsToTranslate();

    //    protected abstract IFile getSelectedFile();
}
