package org.eclipse.etools.ei18n.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.IRegion;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class LineProperties implements Iterable<String> {
    private static class LineInformation {
        private final IRegion region;
        private final String lineDelimiter;
        private final String value;
        private final int lineNumber;
        private final String originalLine;

        public LineInformation(IRegion region, String lineDelimiter, /* String key, */String value, int lineNumber, String originalLine) {
            super();
            this.region=region;
            this.lineDelimiter=lineDelimiter;
            this.value=value;
            this.lineNumber=lineNumber;
            this.originalLine=originalLine;
        }
    }

    private final Map<String, LineInformation> lines=Maps.newHashMap();
    private final IStorage file;

    public LineProperties(IStorage file) throws IOException, BadLocationException, CoreException {
        this.file=file;
        InputStream is=null;
        try {
            String content=IOUtils.toString(is=file.getContents(), StorageUtil.getCharset(file).name());
            reload(content);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    public List<String> reload(String content) throws BadLocationException, IOException {
        DefaultLineTracker lineTracker=new DefaultLineTracker();
        lineTracker.set(content);
        List<String> oldKeys=Lists.newArrayList(lines.keySet());
        for (int lineNumber=0; lineNumber < lineTracker.getNumberOfLines(); lineNumber++) {
            IRegion region=lineTracker.getLineInformation(lineNumber);
            int start=region.getOffset();
            int end=start + region.getLength();
            String line=content.substring(start, end);

            Properties props=new Properties();
            props.load(new StringReader(line));

            for (String key : Iterables.filter(props.keySet(), String.class)) {
                lines.put(key, new LineInformation(region, StringUtils.defaultString(lineTracker.getLineDelimiter(lineNumber)), props.getProperty(key),
                        lineNumber, line));
                oldKeys.remove(key);
            }
        }
        for (String oldKey : oldKeys) {
            lines.remove(oldKey);
        }
        return Collections.unmodifiableList(oldKeys);
    }

    public boolean contains(String key) {
        return lines.containsKey(key);
    }

    public String getLineDelimiter(String key) {
        return lines.containsKey(key) ? lines.get(key).lineDelimiter : null;
    }

    public String getProperty(String key) {
        return lines.containsKey(key) ? lines.get(key).value : null;
    }

    public String getLine(String key) {
        return lines.containsKey(key) ? lines.get(key).originalLine : null;
    }

    public int getLineNumber(String key) {
        return lines.containsKey(key) ? lines.get(key).lineNumber : -1;
    }

    public IRegion getRegion(String key) {
        return lines.containsKey(key) ? lines.get(key).region : null;
    }

    public Iterator<String> iterator() {
        return Iterables.filter(lines.keySet(), String.class).iterator();
    }

    public IStorage getFile() {
        return file;
    }
}
