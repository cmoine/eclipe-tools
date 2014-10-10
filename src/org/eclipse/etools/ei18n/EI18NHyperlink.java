package org.eclipse.etools.ei18n;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.etools.Activator;
import org.eclipse.etools.ei18n.editors.EI18NEditorPart;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.actions.SelectionDispatchAction;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

public class EI18NHyperlink implements IHyperlink {
    private final IRegion wordRegion;
    private IJavaElement element;

    public EI18NHyperlink(IRegion wordRegion, SelectionDispatchAction openAction, IJavaElement element, boolean qualify) {
        this.wordRegion=wordRegion;
        this.element=element;
    }

    public IRegion getHyperlinkRegion() {
        return wordRegion;
    }

    public String getTypeLabel() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getHyperlinkText() {
        return "Open in EI18N";
    }

    public void open() {
        try {
            IResource res=null;
            String key=element.getElementName();
            while ((res=element.getCorrespondingResource()) == null)
                element=element.getParent();

            EI18NEditorPart editorPart=(EI18NEditorPart) PlatformUI.getWorkbench().getActiveWorkbenchWindow() //
                    .getActivePage()
                    .openEditor(new FileEditorInput((IFile) res), EI18NEditorPart.ID, true, IWorkbenchPage.MATCH_ID | IWorkbenchPage.MATCH_INPUT);
            editorPart.select(key);
        } catch (PartInitException e) {
            Activator.log(IStatus.ERROR, "Failed opening hyperlink", e); //$NON-NLS-1$
        } catch (JavaModelException e) {
            Activator.log(IStatus.ERROR, "Failed opening hyperlink", e); //$NON-NLS-1$
        }
    }
}
