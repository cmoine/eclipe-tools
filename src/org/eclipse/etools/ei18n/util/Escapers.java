package org.eclipse.etools.ei18n.util;

import java.nio.charset.Charset;

import org.apache.commons.lang3.StringEscapeUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.etools.Activator;

public enum Escapers {
    JAVA {
        @Override
        public byte[] encode(String str, Charset charset) {
            return StringEscapeUtils.escapeJava(str).getBytes(charset);
        }
    },
    NONE {
        @Override
        public byte[] encode(String str, Charset charset) {
            return str.getBytes(charset);
        }
    };

    public String encode(String str, IFile file) {
        Charset charset;
        try {
            charset=Charset.forName(file.getCharset());
        } catch (Throwable e) {
            charset=Charset.defaultCharset();
            Activator.logError("Unknow charset for file: " + file, e); //$NON-NLS-1$
        }
        return new String(encode(str, charset), charset);
    }

    public abstract byte[] encode(String str, Charset charset);
}
