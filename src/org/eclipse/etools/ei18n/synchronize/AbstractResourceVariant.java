package org.eclipse.etools.ei18n.synchronize;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.etools.Activator;
import org.eclipse.etools.ei18n.util.EI18NConstants;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.variants.IResourceVariant;

import com.google.common.collect.Lists;

abstract class AbstractResourceVariant implements IResourceVariant {
    private final String path;
    private final AbstractSubscriber subscriber;

    public AbstractResourceVariant(AbstractSubscriber subscriber, String path) {
        this.subscriber=subscriber;
        this.path=path;
    }

    public boolean isContainer() {
        return !path.endsWith(".properties"); //$NON-NLS-1$
    }


    public String getName() {
        return getFilename();
    }


    public String getContentIdentifier() {
        return getPath();
    }


    public byte[] asBytes() {
        return getContentIdentifier().getBytes();
    }

    public AbstractSubscriber getSubscriber() {
        return subscriber;
    }

    public String getPath() {
        return path;
    }


    public IStorage getStorage(IProgressMonitor monitor) throws TeamException {
        InputStream is=null;
        try {
            String path=getPath();
            IFile iFile=null;
            for (IResource res : getSubscriber().roots()) {
                path=StringUtils.removeStart(path, res.getFullPath().toString() + "/"); //$NON-NLS-1$
                if (res instanceof IContainer && iFile == null) {
                    IContainer container=(IContainer) res;
                    IResource member=container.findMember(path);
                    if (member != null && member instanceof IFile) {
                        iFile=(IFile) member;
                    }
                }
            }
            List<String> actualLines=Collections.emptyList();
            if (iFile != null) {
                InputStream stream=null;
                try {
                    stream=iFile.getContents();
                    actualLines=Arrays.asList(StringUtils.splitPreserveAllTokens(IOUtils.toString(stream), '\n'));
                } catch (CoreException e) {
                    Activator.logError("Failed to read " + iFile, e); //$NON-NLS-1$
                } finally {
                    IOUtils.closeQuietly(stream);
                }
            }
            is=createInputStream();
            if (is != null) {
                List<String> lines=IOUtils.readLines(is);
                IOUtils.closeQuietly(is);
                List<String> modifiedLines=Lists.newArrayList();
                for (String actualLine : actualLines) {
                    boolean found=false;
                    for (String line : lines) {
                        if (equals(actualLine, line)) {
                            lines.remove(line);
                            modifiedLines.add(line);
                            found=true;
                            break;
                        }
                    }
                    if (!found) {
                        modifiedLines.add(actualLine);
                    }
                }
                for (String line : lines) {
                    if (!line.trim().startsWith("#")) //$NON-NLS-1$
                        modifiedLines.add(line);
                }
                return createStorage(StringUtils.join(modifiedLines.toArray(), "\n")); //$NON-NLS-1$
            } else {
                return createStorage("ERROR"); //$NON-NLS-1$
            }
        } catch (IOException e) {
            Activator.logError("Failed fetching member", e); //$NON-NLS-1$
            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            e.printStackTrace(new PrintWriter(baos));
            return createStorage("ERROR : " + e.getMessage() + IOUtils.LINE_SEPARATOR + baos); //$NON-NLS-1$
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private IStorage createStorage(String content) {
        if (isContainer())
            return null;

        return new MyStorage(path, getFilename(), content);
    }

    protected String getLocale() {
        String filename=getFilename();
        if (EI18NConstants.PATTERN.matcher(filename).matches())
            return null;

        Matcher matcher=EI18NConstants.LOCALE_PATTERN.matcher(filename);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }

    protected String getFilename() {
        return path.contains("/") ? StringUtils.substringAfterLast(path, "/") : path; //$NON-NLS-1$ //$NON-NLS-2$
    }

    protected abstract InputStream createInputStream() throws IOException;

    private boolean equals(String actualLine, String line) {
        return getKey(actualLine).equals(getKey(line));
    }

    private String getKey(String line) {
        return StringUtils.substringBefore(StringUtils.substringBefore(line, ":"), "=").trim(); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public abstract IResourceVariant[] list() throws TeamException;
}
