package org.eclipse.etools.ei18n.editors;


public class Line implements Comparable<Line> {
    private String string;

    //    private final Map<String, String> map=Maps.newHashMap();

    public Line() {
    }

    public Line(String key) {
        string=key;
    }

    public String getString() {
        return string;
    }

    //    public String getString(String locale) {
    //        return map.get(locale);
    //    }

    public boolean isNew() {
        return string == null;
    }

    public void setString(String string) {
        this.string=string;
    }

    //    public void putString(String locale, String string) {
    //        map.put(locale, string);
    //    }
    //
    //    public Set<String> getLocales() {
    //        return map.keySet();
    //    }

    @Override
    public boolean equals(Object arg0) {
        if (arg0 instanceof Line && arg0 != null && string != null)
            return string.equals(((Line) arg0).string);

        return false;
    }

    @Override
    public int hashCode() {
        return string.hashCode();
    }

    public int compareTo(Line o) {
        if (string == null)
            return 1;
        if (o.string == null)
            return -1;

        return string.compareToIgnoreCase(o.string);
    }
}
