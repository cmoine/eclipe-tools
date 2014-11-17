package org.eclipse.etools.search;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.etools.ei18n.EI18NImage;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.search.internal.ui.text.DecoratingFileSearchLabelProvider;
import org.eclipse.search.internal.ui.text.FileLabelProvider;
import org.eclipse.search.internal.ui.text.FileMatch;
import org.eclipse.search.internal.ui.text.FileSearchPage.DecoratorIgnoringViewerSorter;
import org.eclipse.search.internal.ui.text.IFileSearchContentProvider;
import org.eclipse.search.internal.ui.text.LineElement;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.Match;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.views.navigator.NavigatorDragAdapter;

public class EI18NSearchResultPage extends AbstractTextSearchViewPage {
    public EI18NSearchResultPage() {
        setElementLimit(-1);
    }

    @Override
    protected void elementsChanged(Object[] objects) {
        if (fContentProvider != null)
            fContentProvider.elementsChanged(objects);
    }

    @Override
    protected void clear() {
        //        getViewer().refresh();
        if (fContentProvider != null)
            fContentProvider.clear();
    }

    private final FileLabelProvider innerLabelProvider=new FileLabelProvider(this, FileLabelProvider.SHOW_LABEL) {
        @Override
        public Image getImage(Object element) {
            if (element instanceof LineElement)
                return ((LineElement) element).getLine() == -1 ? EI18NImage.ERROR_16.getImage() : EI18NImage.WARNING_16.getImage();

            return super.getImage(element);
        }

        @Override
        public String getText(Object element) {
            if (element instanceof LineElement && ((LineElement) element).getLine() == -1)
                return ((LineElement) element).getContents();

            return super.getText(element);
        }

        @Override
        public StyledString getStyledText(Object element) {
            if (element instanceof LineElement && ((LineElement)element).getLine()==-1)
                return new StyledString(((LineElement) element).getContents());

            return super.getStyledText(element);
        }
    };
    private IFileSearchContentProvider fContentProvider;

    @Override
    protected void configureTableViewer(TableViewer viewer) {
        viewer.setUseHashlookup(true);
        viewer.setLabelProvider(new DecoratingFileSearchLabelProvider(innerLabelProvider));
        viewer.setContentProvider(new FileTableContentProvider(this));
        viewer.setComparator(new DecoratorIgnoringViewerSorter(innerLabelProvider));
        fContentProvider=(IFileSearchContentProvider) viewer.getContentProvider();
        addDragAdapters(viewer);
    }

    @Override
    protected void configureTreeViewer(TreeViewer viewer) {
        viewer.setUseHashlookup(true);
        viewer.setLabelProvider(new DecoratingFileSearchLabelProvider(innerLabelProvider));
        viewer.setContentProvider(new FileTreeContentProvider(this, viewer));

        viewer.setComparator(new DecoratorIgnoringViewerSorter(innerLabelProvider));
        fContentProvider=(IFileSearchContentProvider) viewer.getContentProvider();
        addDragAdapters(viewer);
    }

    private void addDragAdapters(StructuredViewer viewer) {
        Transfer[] transfers=new Transfer[] { ResourceTransfer.getInstance() };
        int ops=DND.DROP_COPY | DND.DROP_LINK;
        viewer.addDragSupport(ops, transfers, new NavigatorDragAdapter(viewer));
    }

    @Override
    public StructuredViewer getViewer() {
        return super.getViewer();
    }

    @Override
    public int getDisplayedMatchCount(Object element) {
        if (element instanceof LineElement) {
            LineElement lineEntry=(LineElement) element;
            return lineEntry.getNumberOfMatches(getInput());
        }
        return 0;
    }

    @Override
    public Match[] getDisplayedMatches(Object element) {
        if (element instanceof LineElement) {
            LineElement lineEntry=(LineElement) element;
            return lineEntry.getMatches(getInput());
        }
        return new Match[0];
    }

    @Override
    protected void showMatch(Match match, int offset, int length, boolean activate) throws PartInitException {
        IFile file=(IFile) match.getElement();
        IWorkbenchPage page=getSite().getPage();
        if (offset >= 0 && length != 0) {
            openAndSelect(page, file, offset, length, activate);
        } else {
            open(page, file, activate);
        }
    }

    @Override
    protected void evaluateChangedElements(Match[] matches, Set changedElements) {
        for (int i=0; i < matches.length; i++) {
            changedElements.add(((FileMatch) matches[i]).getLineElement());
        }
    }
}
