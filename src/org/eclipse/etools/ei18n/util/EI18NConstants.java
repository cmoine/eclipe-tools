package org.eclipse.etools.ei18n.util;

import java.util.regex.Pattern;

public interface EI18NConstants {
    public final Pattern SUFFIX_LOCALE_PATTERN=Pattern.compile("_([a-z]{2}?)\\.properties"); //$NON-NLS-1$
    public final Pattern LOCALE_PATTERN=Pattern.compile("[a-z]*" + SUFFIX_LOCALE_PATTERN.pattern()); //$NON-NLS-1$
    public final Pattern PATTERN=Pattern.compile("[^_]*\\.properties"); //$NON-NLS-1$
    public final String NLS_CLASS_NAME="NLS"; //$NON-NLS-1$
}
