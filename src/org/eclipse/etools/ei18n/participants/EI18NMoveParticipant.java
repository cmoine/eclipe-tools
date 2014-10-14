package org.eclipse.etools.ei18n.participants;

import static org.eclipse.core.runtime.IStatus.ERROR;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.etools.Activator;
import org.eclipse.etools.ei18n.util.EI18NConstants;
import org.eclipse.etools.ei18n.util.LineProperties;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.MoveParticipant;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;

import com.google.common.collect.Lists;

public class EI18NMoveParticipant extends MoveParticipant {
    private EI18NParticipant participant;

    @Override
    protected boolean initialize(Object element) {
        return (participant=new EI18NParticipant(element)).isInitialized();
    }

    @Override
    public String getName() {
        return EI18NParticipant.NAME + " Move"; //$NON-NLS-1$
    }

    @Override
    public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context) throws OperationCanceledException {
        return new RefactoringStatus();
    }

    @Override
    public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        List<Change> changes=Lists.newArrayList();
        IFolder dstDir=(IFolder) ((IType) getArguments().getDestination()).getResource().getParent();
        for (IResource res : ((IFolder) participant.getDeclaringType().getResource().getParent()).members()) {
            if (res instanceof IFile //
                    && (EI18NConstants.LOCALE_PATTERN.matcher(res.getName()).matches() //
                    || EI18NConstants.PATTERN.matcher(res.getName()).matches())) {
                IFile srcFile=(IFile) res;
                IFile dstFile=dstDir.getFile(srcFile.getName());
                changes.addAll(createChange(srcFile, dstFile));
            }
        }
        return new CompositeChange("Messages*.property", changes.toArray(new Change[changes.size()])); //$NON-NLS-1$
    }

    private List<Change> createChange(IFile srcFile, IFile dstFile) {
        List<Change> changes=Lists.newArrayList();
        try {
            String key=participant.getField().getElementName();
            LineProperties properties=new LineProperties(srcFile);
            if (properties.contains(key)) {
                IRegion region=properties.getRegion(key);
                {
                    TextFileChange change=new TextFileChange("Remove key " + key, srcFile); //$NON-NLS-1$
                    change.setEdit(new DeleteEdit(region.getOffset(), region.getLength() + properties.getLineDelimiter(key).length()));
                    changes.add(change);
                }
                {
                    String line=properties.getLine(key);
                    TextFileChange change=new TextFileChange("Add key " + key, dstFile); //$NON-NLS-1$
                    change.setEdit(new InsertEdit(0, line.replaceAll("\\r|\\n", StringUtils.EMPTY) + IOUtils.LINE_SEPARATOR)); //$NON-NLS-1$
                    changes.add(change);
                }
            }
        } catch (IOException e) {
            Activator.log(ERROR, "Failed to read " + srcFile, e); //$NON-NLS-1$
        } catch (CoreException e) {
            Activator.log(ERROR, "Failed to read " + srcFile, e); //$NON-NLS-1$
        } catch (BadLocationException e) {
            Activator.log(ERROR, "Failed to read " + srcFile, e); //$NON-NLS-1$
        }
        return changes;
    }

}
