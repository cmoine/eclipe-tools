package org.eclipse.etools.ei18n;

import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaElementHyperlinkDetector;
import org.eclipse.jdt.ui.actions.SelectionDispatchAction;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;

public class EI18NHyperlinkDetector extends JavaElementHyperlinkDetector {
    @Override
    protected void addHyperlinks(List<IHyperlink> hyperlinksCollector, IRegion wordRegion, SelectionDispatchAction openAction, IJavaElement element,
            boolean qualify, JavaEditor editor) {
        hyperlinksCollector.add(new EI18NHyperlink(wordRegion, openAction, element, qualify));
    }
}
