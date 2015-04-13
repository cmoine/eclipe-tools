package org.eclipse.etools.efavorites.correction;

import static java.text.MessageFormat.format;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.etools.efavorites.EFavoritesImage;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import com.google.common.base.Joiner;

public class AddStaticImportProposal implements IJavaCompletionProposal {
    private String fullName;

    public AddStaticImportProposal(IInvocationContext context) {
        ASTNode coveringNode=context.getCoveringNode();
        ITypeBinding resolveBinding=null;
        SimpleName name=null;
        if (coveringNode.getParent() instanceof MethodInvocation) {
            MethodInvocation mi=(MethodInvocation) coveringNode.getParent();
            resolveBinding=mi.resolveMethodBinding().getDeclaringClass();
            name=mi.getName();
        } else if (coveringNode.getParent() instanceof QualifiedName) {
            QualifiedName qName=(QualifiedName) coveringNode.getParent();
            resolveBinding=(ITypeBinding) qName.getQualifier().resolveBinding();
            name=qName.getName();
        }
        if (resolveBinding != null && name != null)
            fullName=new StringBuffer(resolveBinding.getQualifiedName()).append('.').append(name.getIdentifier()).toString();
    }

    public boolean isValid() {
        return fullName != null;
    }

    public void apply(IDocument document) {
        IPreferenceStore store=JavaPlugin.getDefault().getPreferenceStore();
        String[] values=ArrayUtils.add(store.getString(PreferenceConstants.CODEASSIST_FAVORITE_STATIC_MEMBERS).split(";"), fullName);
        store.setValue(PreferenceConstants.CODEASSIST_FAVORITE_STATIC_MEMBERS, Joiner.on(';').join(values));
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
        return format("Add ''{0}'' to static imports", fullName);
    }

    public Image getImage() {
        return EFavoritesImage.STATIC_IMPORT_16.getImage();
    }

    public IContextInformation getContextInformation() {
        // TODO Auto-generated method stub
        return null;
    }

    public int getRelevance() {
        return 0;
    }
}
