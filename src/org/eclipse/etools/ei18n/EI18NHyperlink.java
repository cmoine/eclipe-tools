package org.eclipse.etools.ei18n;

import org.eclipse.core.resources.IFile;
import org.eclipse.etools.Activator;
import org.eclipse.etools.ei18n.editors.EI18NEditorPart;
import org.eclipse.etools.ei18n.util.EI18NUtil;
import org.eclipse.etools.ei18n.util.MappingPreference;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.actions.SelectionDispatchAction;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

public class EI18NHyperlink implements IHyperlink {
    private final IRegion wordRegion;
    private final String key;
    private IFile file;

    public EI18NHyperlink(IRegion wordRegion, SelectionDispatchAction openAction, IJavaElement element, boolean qualify) {
        this.wordRegion=wordRegion;
        key=element.getElementName();
        IFile res=EI18NUtil.getFile(element);
        for (MappingPreference mPref : MappingPreference.list(res.getProject())) {
            if (res.equals(mPref.getJavaFile())) {
                file=mPref.getPropertyFile();
            }
        }
    }

    public boolean isValid() {
        return file != null;
    }

    public IRegion getHyperlinkRegion() {
        return wordRegion;
    }

    public String getTypeLabel() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getHyperlinkText() {
        return "Open With EI18N";
    }

    public void open() {
        try {
            EI18NEditorPart editorPart=(EI18NEditorPart) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getActivePage().openEditor(new FileEditorInput(file), EI18NEditorPart.ID, true, IWorkbenchPage.MATCH_ID | IWorkbenchPage.MATCH_INPUT);
            editorPart.select(key);
        } catch (PartInitException e) {
            Activator.logError("Failed opening hyperlink", e); //$NON-NLS-1$
        }
    }
}
