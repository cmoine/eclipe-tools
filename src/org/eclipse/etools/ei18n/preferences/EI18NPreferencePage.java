package org.eclipse.etools.ei18n.preferences;

import org.eclipse.etools.Activator;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class EI18NPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, EI18NPreferenceConstants {
    //    private IDocument document;

    public EI18NPreferencePage() {
        //        setDescription("Template de code JAVA:");
    }

	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

    @Override
    protected Control createContents(Composite parent) {
        //        EditTemplateDialog dialog; // TODO
        //
        //        //        Composite composite=new Composite(parent, SWT.NONE);
        //        document=new Document();
        //        //        document.set(getPreferenceStore().getString(TEMPLATE_STR));
        //        JavaTextTools tools=JavaPlugin.getDefault().getJavaTextTools();
        //        tools.setupJavaDocumentPartitioner(document, IJavaPartitions.JAVA_PARTITIONING);
        //        IPreferenceStore store=JavaPlugin.getDefault().getCombinedPreferenceStore();
        //        SourceViewer viewer=new JavaSourceViewer(parent, null, null, false, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL, store);
        //        CodeTemplateSourceViewerConfiguration configuration=new CodeTemplateSourceViewerConfiguration(tools.getColorManager(), store, null,
        //                new TemplateVariableProcessor());
        //        viewer.configure(configuration);
        //
        //        viewer.setEditable(false);
        //        Cursor arrowCursor=viewer.getTextWidget().getDisplay().getSystemCursor(SWT.CURSOR_ARROW);
        //        viewer.getTextWidget().setCursor(arrowCursor);
        //
        //        // Don't set caret to 'null' as this causes https://bugs.eclipse.org/293263
        //        //      viewer.getTextWidget().setCaret(null);
        //
        //        viewer.setDocument(document);
        //
        //        Font font=JFaceResources.getFont(PreferenceConstants.EDITOR_TEXT_FONT);
        //        viewer.getTextWidget().setFont(font);
        //        new JavaSourcePreviewerUpdater(viewer, configuration, store);
        //
        //        return viewer.getControl();
        return null;
    }

    //    @Override
    //    protected void performDefaults() {
    //        //        getPreferenceStore().setToDefault(TEMPLATE_STR);
    //        //        document.set(getPreferenceStore().getString(TEMPLATE_STR));
    //        super.performDefaults();
    //    }
    //
    //    @Override
    //    public boolean performOk() {
    //        //        getPreferenceStore().setValue(TEMPLATE_STR, document.get());
    //        return true;
    //    }
}
