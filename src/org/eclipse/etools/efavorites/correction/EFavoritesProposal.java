package org.eclipse.etools.efavorites.correction;

import static java.text.MessageFormat.format;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.etools.efavorites.EFavoritesImage;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import com.google.common.base.Joiner;

public class EFavoritesProposal implements IJavaCompletionProposal {
    private final String fullName;

    public EFavoritesProposal(String fullName) {
        this.fullName=fullName;
    }

    public void apply(IDocument document) {
        IPreferenceStore store=JavaPlugin.getDefault().getPreferenceStore();
        String[] values=ArrayUtils.add(store.getString(PreferenceConstants.TYPEFILTER_ENABLED).split(";"), fullName);
        store.setValue(PreferenceConstants.TYPEFILTER_ENABLED, Joiner.on(';').join(values));

    }

    public Point getSelection(IDocument document) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getAdditionalProposalInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getDisplayString() {
        return format("Add ''{0}'' to type filters", fullName);
    }

    public Image getImage() {
        return EFavoritesImage.TRASH_16.getImage();
    }

    public IContextInformation getContextInformation() {
        // TODO Auto-generated method stub
        return null;
    }

    public int getRelevance() {
        return 0;
    }
}
