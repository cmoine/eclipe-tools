package org.eclipse.etools.ei18n.editors;

import java.util.Map;

public class EditionLine {
    private final String locale;
    private final Map<String, String> map;
    private final String value;

    public EditionLine(String locale, String value, Map<String, String> map) {
        this.locale=locale;
        this.value=value;
        this.map=map;
    }

    public String getLocale() {
        return locale;
    }

    public String getValue() {
        return value;
    }

    public Map<String, String> getMap() {
        return map;
    }
}
