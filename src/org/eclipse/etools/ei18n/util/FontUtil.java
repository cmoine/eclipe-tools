package org.eclipse.etools.ei18n.util;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

public final class FontUtil {
    private FontUtil() {
    }

    public static Font derivate(Font font, int style) {
        FontData[] fontData=font.getFontData();
        fontData[0].setStyle(fontData[0].getStyle() ^ style);
        return new Font(font.getDevice(), fontData);
    }

    public static void safeDispose(Font... fonts) {
        for (Font font : fonts) {
            if (font != null)
                font.dispose();
        }
    }
}
