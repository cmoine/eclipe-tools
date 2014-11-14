package org.eclipse.etools.ei18n.search;

import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
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
    //    private IFileSearchContentProvider fContentProvider;

    private static class ArrayTreeContentProvider extends ArrayContentProvider implements ITreeContentProvider {
        public static final ArrayTreeContentProvider INSTANCE=new ArrayTreeContentProvider();
        private EI18NTextSearchResult input;

        @Override
        public Object[] getElements(Object inputElement) {
            //            EI18NTextSearchResult result=(EI18NTextSearchResult) inputElement;
            return input.getElements();
        }

        public Object getParent(Object element) {
            return null;
        }

        public boolean hasChildren(Object element) {
            return getChildren(element).length > 0;
        }

        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof IFile) {

                return input.getMultimap().get((IFile) parentElement).toArray();
            }

            return ArrayUtils.EMPTY_OBJECT_ARRAY;
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            this.input=(EI18NTextSearchResult) newInput;
        }
    }

    public EI18NSearchResultPage() {
        setElementLimit(-1);
    }

    @Override
    protected void elementsChanged(Object[] objects) {
        //        getViewer().refresh();
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
            //            if (element instanceof Infos)
            //                return EI18NImage.DELETE_16.getImage();

            return super.getImage(element);
        }

        @Override
        public String getText(Object object) {
            //            if (object instanceof Infos)
            //                return ((Infos) object).getMessage();
            return super.getText(object);
        }

        @Override
        public StyledString getStyledText(Object element) {
            //            if (element instanceof Infos)
            //                return new StyledString(getText(element));

            return super.getStyledText(element);
        }
    };
    private IFileSearchContentProvider fContentProvider;

    @Override
    protected void configureTableViewer(TableViewer viewer) {
        viewer.setUseHashlookup(true);
        //        FileLabelProvider innerLabelProvider=new FileLabelProvider(this, FileLabelProvider.SHOW_LABEL_PATH);
        viewer.setLabelProvider(new DecoratingFileSearchLabelProvider(innerLabelProvider));
        viewer.setContentProvider(new FileTableContentProvider(this));
        //        viewer.setContentProvider(ArrayTreeContentProvider.INSTANCE);
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
        //        viewer.setContentProvider(ArrayTreeContentProvider.INSTANCE);
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
        //        if (showLineMatches()) {
        if (element instanceof LineElement) {
            LineElement lineEntry=(LineElement) element;
            return lineEntry.getNumberOfMatches(getInput());
        }
        return 0;
        //        }
        //        return super.getDisplayedMatchCount(element);
    }

    @Override
    public Match[] getDisplayedMatches(Object element) {
        //        if (showLineMatches()) {
        if (element instanceof LineElement) {
            LineElement lineEntry=(LineElement) element;
            return lineEntry.getMatches(getInput());
        }
        return new Match[0];
        //        }
        //        return super.getDisplayedMatches(element);
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
        //        if (showLineMatches()) {
        for (int i=0; i < matches.length; i++) {
            changedElements.add(((FileMatch) matches[i]).getLineElement());
        }
        //        } else {
        //            super.evaluateChangedElements(matches, changedElements);
        //        }
    }
}
