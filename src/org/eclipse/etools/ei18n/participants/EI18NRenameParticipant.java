package org.eclipse.etools.ei18n.participants;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.etools.Activator;
import org.eclipse.etools.ei18n.util.EI18NConstants;
import org.eclipse.etools.ei18n.util.LineProperties;
import org.eclipse.etools.ei18n.util.PreferencesUtil;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.eclipse.text.edits.ReplaceEdit;

import com.google.common.collect.Lists;

public class EI18NRenameParticipant extends RenameParticipant {
    private EI18NParticipant participant;

    @Override
    protected boolean initialize(Object element) {
        return (participant=new EI18NParticipant(element)).isInitialized();
    }

    @Override
    public String getName() {
        return EI18NParticipant.NAME + " Rename"; //$NON-NLS-1$
    }

    @Override
    public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context) throws OperationCanceledException {
        return new RefactoringStatus();
    }

    @Override
    public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        List<Change> changes=Lists.newArrayList();
        for (IResource res : ((IFolder) participant.getDeclaringType().getResource().getParent()).members()) {
            if (res instanceof IFile && //
                    (EI18NConstants.LOCALE_PATTERN.matcher(res.getName()).matches() //
                    || EI18NConstants.PATTERN.matcher(res.getName()).matches())) {
                IFile srcFile=(IFile) res;
                changes.addAll(createChange(srcFile));
            }
        }
        return new CompositeChange("Messages*.property", changes.toArray(new Change[changes.size()])); //$NON-NLS-1$
    }

    private List<Change> createChange(IFile srcFile) {
        List<Change> changes=Lists.newArrayList();
        try {
            String oldName=participant.getField().getElementName();
            LineProperties properties=new LineProperties(srcFile);
            if (properties.contains(oldName)) {
                IRegion region=properties.getRegion(oldName);
                TextFileChange change=new TextFileChange("Remove key " + oldName, srcFile); //$NON-NLS-1$
                change.setEdit(new ReplaceEdit(region.getOffset(), region.getLength() + properties.getLineDelimiter(oldName).length(), getArguments()
                        .getNewName() + "=" + StringEscapeUtils.escapeJava(properties.getProperty(oldName)) + PreferencesUtil.getLineDelimiter(srcFile))); //$NON-NLS-1$
                changes.add(change);
            }
        } catch (IOException e) {
            Activator.logError("Failed to read " + srcFile, e); //$NON-NLS-1$
        } catch (CoreException e) {
            Activator.logError("Failed to read " + srcFile, e); //$NON-NLS-1$
        } catch (BadLocationException e) {
            Activator.logError("Failed to read " + srcFile, e); //$NON-NLS-1$
        }
        return changes;
    }

}
