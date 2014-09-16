package org.eclipse.etools.ei18n.extensions;

import java.util.Collection;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;

public interface IJavaMapping {
    public void init(IDocument document, IFile javaFile);

    public Set<String> getKeys();

    public void syncFields(Collection<String> newFields, Collection<String> fieldsToRemove);

    //    public String getTemplate() throws IOException;
}
