package org.eclipse.etools.ei18n.synchronize;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.etools.Activator;
import org.eclipse.etools.ei18n.util.EI18NConstants;
import org.eclipse.etools.ei18n.util.PreferencesUtil;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.IRegion;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.variants.IResourceVariant;

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
            DefaultLineTracker lineTracker=new DefaultLineTracker();
            String content=EMPTY;
            if (iFile != null) {
                InputStream stream=null;
                try {
                    stream=iFile.getContents();
                    content=IOUtils.toString(stream);
                } finally {
                    IOUtils.closeQuietly(stream);
                }
            }
            lineTracker.set(content);
            is=createInputStream();
            if (is != null) {
                List<String> lines=IOUtils.readLines(is);
                IOUtils.closeQuietly(is);

                StringBuffer buf=new StringBuffer();
                for (int i=0; i < lineTracker.getNumberOfLines(); i++) {
                    IRegion region=lineTracker.getLineInformation(i);
                    String lineDelimiter=lineTracker.getLineDelimiter(i);
                    String actualLine=content.substring(region.getOffset(), region.getOffset() + region.getLength());
                    boolean found=false;
                    for (String line : lines) {
                        if (equals(actualLine, line)) {
                            lines.remove(line);
                            buf.append(line).append(lineDelimiter);
                            found=true;
                            break;
                        }
                    }
                    if (!found) {
                        buf.append(actualLine);
                        if (lineDelimiter != null)
                            buf.append(lineDelimiter);
                    }
                }
                for (String line : lines) {
                    if (!line.trim().startsWith("#")) //$NON-NLS-1$
                        buf.append(line).append(PreferencesUtil.getLineDelimiter(iFile == null ? null : iFile.getProject()));
                }
                return createStorage(buf.toString());
            }
            // }
        } catch (Exception e) {
            Activator.logError("Failed fetching member", e); //$NON-NLS-1$
            ByteArrayOutputStream baos=new ByteArrayOutputStream();
            e.printStackTrace(new PrintWriter(baos));
            return createStorage("ERROR : " + e.getMessage() + IOUtils.LINE_SEPARATOR + baos); //$NON-NLS-1$
        } finally {
            IOUtils.closeQuietly(is);
        }
        return createStorage("ERROR"); //$NON-NLS-1$
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
            return matcher.group(EI18NConstants.LOCALE_GROUP);
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
