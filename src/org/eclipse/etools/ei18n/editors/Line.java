package org.eclipse.etools.ei18n.editors;


public class Line implements Comparable<Line> {
    private String string;

    public Line() {
    }

    public Line(String key) {
        string=key;
    }

    public String getString() {
        return string;
    }

    public boolean isNew() {
        return string == null;
    }

    public void setString(String string) {
        this.string=string;
    }

    public int compareTo(Line o) {
        if (string == null)
            return 1;
        if (o.string == null)
            return -1;

        return string.compareToIgnoreCase(o.string);
    }
}
