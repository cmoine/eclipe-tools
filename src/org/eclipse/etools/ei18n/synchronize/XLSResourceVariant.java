package org.eclipse.etools.ei18n.synchronize;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.eclipse.core.runtime.Platform;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.variants.IResourceVariant;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class XLSResourceVariant extends AbstractResourceVariant {
    private final XLSSubscriber subscriber;

    public XLSResourceVariant(XLSSubscriber subscriber, String path) {
        super(subscriber, path);
        this.subscriber=subscriber;
    }

    @Override
    protected InputStream createInputStream() throws IOException {
        // Collect
        String prefix=getPath() + '#';
        Properties props=new Properties();
        String locale=getLocale();
        int localeIndex=subscriber.getDefaultCol();
        if (locale != null) {
            localeIndex=subscriber.getTranslations().get(locale);
            prefix=StringUtils.replace(prefix, '_' + locale + ".properties", ".properties"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        for (Row row : subscriber.getRows()) {
            Cell cell=row.getCell(subscriber.getKeyCol());
            if (cell != null && cell.getStringCellValue().startsWith(prefix)) {
                String key=StringUtils.substringAfter(cell.getStringCellValue(), "#"); //$NON-NLS-1$
                Cell cell2=row.getCell(localeIndex);
                String value=cell2 == null ? StringUtils.EMPTY : StringUtils.defaultString(getValue(cell2));
                if (StringUtils.isNotBlank(key))
                    props.setProperty(key, value);
            }
        }
        // Write
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        props.store(baos, StringUtils.EMPTY);
        StringBuffer buf=new StringBuffer();
        for (String line : IOUtils.readLines(new ByteArrayInputStream(baos.toByteArray()))) {
            line=StringUtils.trimToEmpty(line);
            if (!line.isEmpty() && !line.startsWith("#")) { //$NON-NLS-1$
                buf.append(line).append(System.getProperty(Platform.PREF_LINE_SEPARATOR));
            }
        }
        return new ByteArrayInputStream(buf.toString().getBytes());
    }

    private String getValue(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC:
                String string=Double.toString(cell.getNumericCellValue());
                //                Assert.isTrue(string.endsWith(".0"), "Must not be decimal"); //$NON-NLS-1$ //$NON-NLS-2$
                string=StringUtils.removeEnd(string, ".0"); //$NON-NLS-1$
                return string;
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            case Cell.CELL_TYPE_BLANK:
            default:
                return null;
                // return cell.getRawValue();
        }
    }

    @Override
    public IResourceVariant[] list() throws TeamException {
        List<XLSResourceVariant> result=Lists.newArrayList();
        if (!getPath().endsWith(".properties")) { //$NON-NLS-1$
            Set<String> subdirs=Sets.newHashSet();
            for (Row row : subscriber.getRows()) {
                Cell cell=row.getCell(subscriber.getKeyCol());
                if (cell != null) {
                    String key=StringUtils.substringBefore(cell.getStringCellValue(), "#"); //$NON-NLS-1$
                    String prefix=/* '/' + getProject() + '/' + */getSafePath();
                    if (key.startsWith(prefix) && !key.equals(getPath())) {
                        String subdir=StringUtils.removeStart(key, prefix);
                        subdir=StringUtils.substringBefore(subdir, "/");
                        if (StringUtils.isNotEmpty(subdir))
                            subdirs.add(subdir);
                    }
                }
            }
            for (String subdir : subdirs) {
                String debug=getSafePath() + subdir;
                result.add(new XLSResourceVariant(subscriber, debug));
                if (subdir.endsWith(".properties")) { //$NON-NLS-1$
                    String filename=StringUtils.removeEnd(subdir, ".properties"); //$NON-NLS-1$
                    for (String lang : subscriber.getTranslations().keySet()) {
                        debug=getSafePath() + filename + '_' + lang + ".properties"; //$NON-NLS-1$
                        result.add(new XLSResourceVariant(subscriber, debug));
                    }
                }
            }
        }
        return result.toArray(new IResourceVariant[] {});
    }

    private String getSafePath() {
        String path=getPath();
        if (StringUtils.isNotEmpty(path))
            path+='/';
        return path;
    }
}
