package org.eclipse.etools.ei18n.synchronize;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IEncodedStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import com.google.common.base.Charsets;

public class MyStorage implements IEncodedStorage {
    private final String content;
    private final String path;
    private final String name;

    public MyStorage(String path, String name, String content) {
        this.path=path;
        this.name=name;
        this.content=content;
    }


    public InputStream getContents() throws CoreException {
        return new ByteArrayInputStream(content.getBytes());
    }


    public IPath getFullPath() {
        return new Path(path);
    }


    public String getName() {
        return name;
    }


    public boolean isReadOnly() {
        return true;
    }


    public Object getAdapter(Class adapter) {
        return null;
    }


    public String getCharset() throws CoreException {
        return Charsets.UTF_8.name();
    }

}
