package org.eclipse.etools.ei18n.util;

import java.nio.charset.Charset;

import org.apache.commons.lang3.StringEscapeUtils;

public enum Escapers {
    JAVA {
        @Override
        public byte[] encode(String str, Charset charset) {
            return StringEscapeUtils.escapeJava(str).getBytes(charset);
            //            try {
            //                Properties props=new Properties();
            //                props.put(StringUtils.EMPTY, str);
            //                ByteArrayOutputStream baos=new ByteArrayOutputStream();
            //                props.store(baos, null);
            //                return StringUtils.substringBefore(StringUtils.substringAfter(baos.toString(), "="), IOUtils.LINE_SEPARATOR).getBytes(charset); //$NON-NLS-1$
            //            } catch (IOException e) {
            //                Activator.logError("Failed encoding string " + str, e); //$NON-NLS-1$
            //                throw Throwables.propagate(e);
            //            }
        }
    },
    NONE {
        @Override
        public byte[] encode(String str, Charset charset) {
            return str.getBytes(charset);
        }
    };

    public abstract byte[] encode(String str, Charset charset);
}
