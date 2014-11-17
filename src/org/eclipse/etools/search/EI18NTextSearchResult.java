package org.eclipse.etools.search;

import org.eclipse.core.resources.IFile;
import org.eclipse.etools.ei18n.EI18NImage;
import org.eclipse.etools.ei18n.actions.ValidateNLSHandler;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;

public final class EI18NTextSearchResult extends AbstractTextSearchResult implements IFileMatchAdapter, IEditorMatchAdapter {
    //    public static class Infos {
    //        private final Match match;
    //        private final String message;
    //
    //        public Infos(Match match, String message) {
    //            this.match=match;
    //            this.message=message;
    //        }
    //
    //        public Match getMatch() {
    //            return match;
    //        }
    //
    //        public String getMessage() {
    //            return message;
    //        }
    //    }

    private static final Match[] EMPTY_ARR=new Match[0];

    //    private final Multimap<IFile, Infos> multimap=HashMultimap.create();

    private final ISearchQuery query;

    public EI18NTextSearchResult(ISearchQuery query) {
        this.query=query;
    }

    public String getLabel() {
        return ValidateNLSHandler.LABEL;
    }

    public String getTooltip() {
        return ValidateNLSHandler.LABEL;
    }

    public ImageDescriptor getImageDescriptor() {
        return EI18NImage.LOGO_16.getImageDescriptor();
    }

    public ISearchQuery getQuery() {
        return query;
    }

    @Override
    public IEditorMatchAdapter getEditorMatchAdapter() {
        return this;
    }

    @Override
    public IFileMatchAdapter getFileMatchAdapter() {
        return this;
    }

    public Match[] computeContainedMatches(AbstractTextSearchResult result, IFile file) {
        return getMatches(file);
    }

    public IFile getFile(Object element) {
        return (IFile) element;
    }

    public boolean isShownInEditor(Match match, IEditorPart editor) {
        return true;
    }

    public Match[] computeContainedMatches(AbstractTextSearchResult result, IEditorPart editor) {
        IEditorInput ei=editor.getEditorInput();
        if (ei instanceof IFileEditorInput) {
            IFileEditorInput fi=(IFileEditorInput) ei;
            return getMatches(fi.getFile());
        }
        return EMPTY_ARR;
    }

    //    public void addMatch(Match match, IFile file, String message) {
    //        addMatch(match);
    //        multimap.put(file, new Infos(match, message));
    //    }
    //
    //    public Multimap<IFile, Infos> getMultimap() {
    //        return multimap;
    //    }
}