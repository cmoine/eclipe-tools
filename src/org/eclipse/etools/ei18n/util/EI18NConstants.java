package org.eclipse.etools.ei18n.util;

import java.util.regex.Pattern;

public interface EI18NConstants {
    public static final Pattern SUFFIX_LOCALE_PATTERN=Pattern.compile("_([a-z]{2}?)\\.properties"); //$NON-NLS-1$
    public static final Pattern LOCALE_PATTERN=Pattern.compile("([a-z]*)" + SUFFIX_LOCALE_PATTERN.pattern()); //$NON-NLS-1$
    public static final Pattern PATTERN=Pattern.compile("([^_]*)\\.properties"); //$NON-NLS-1$

    public static final int BASE_NAME_GROUP=1;
    public static final int LOCALE_GROUP=2;
}
