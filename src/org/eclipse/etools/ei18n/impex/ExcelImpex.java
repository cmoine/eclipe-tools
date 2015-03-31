package org.eclipse.etools.ei18n.impex;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.record.CFRuleRecord.ComparisonOperator;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFConditionalFormattingRule;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PatternFormatting;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.etools.Activator;
import org.eclipse.etools.ei18n.extensions.IImpex;
import org.eclipse.etools.ei18n.util.EI18NConstants;
import org.eclipse.etools.ei18n.util.EI18NUtil;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ExcelImpex implements IImpex {
    protected static final int STARTING_COLUMN=1;

    private static class Line {
        private final IFile mainFile;
        private final Map<String, IFile> files=new HashMap<String, IFile>();

        public Line(IFile file) {
            mainFile=file;
        }

        @Override
        public int hashCode() {
            final int prime=31;
            int result=1;
            result=prime * result + (mainFile == null ? 0 : mainFile.getFullPath().toString().hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Line other=(Line) obj;
            if (mainFile == null) {
                if (other.mainFile != null)
                    return false;
            } else if (!mainFile.getFullPath().toString().equals(other.mainFile.getFullPath().toString()))
                return false;
            return true;
        }
    }

    public void export(final Iterable<IFile> iterable, File dst, IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        FileOutputStream os=null;
        try {
            HSSFWorkbook wb=new HSSFWorkbook();
            createSheet(iterable, wb, "main"); //$NON-NLS-1$
            createSheet(iterable, wb, "do not edit"); //$NON-NLS-1$
            os=new FileOutputStream(dst);
            wb.write(os);
        } catch (Exception e) {
            Activator.logError("Failed to export", e); //$NON-NLS-1$
            throw new InvocationTargetException(e);
        } finally {
            IOUtils.closeQuietly(os);
        }
    }

    private void createSheet(final Iterable<IFile> iterable, HSSFWorkbook wb, String sheetName) throws CoreException, IOException {
        InputStream is=null;
        try {
            HSSFSheet sheet=wb.createSheet(sheetName);
            sheet.createFreezePane(0, 1);

            int rownum=0;
            HSSFRow headerRow=sheet.createRow(rownum++);
            CellStyle headerStyle=wb.createCellStyle();
            Font font=wb.createFont();
            font.setBoldweight(Font.BOLDWEIGHT_BOLD);
            headerStyle.setFont(font);
            HSSFCell cell=headerRow.createCell(1);
            cell.setCellValue("Key"); //$NON-NLS-1$
            cell.setCellStyle(headerStyle);
            cell=headerRow.createCell(2);
            cell.setCellValue("Default Translation"); //$NON-NLS-1$
            cell.setCellStyle(headerStyle);

            // Organize file into multimap
            //                    Multimap<IFile, IFile> files=HashMultimap.create();
            Map<Line, Line> lines=new HashMap<Line, Line>();
            Set<String> locales=Sets.newTreeSet();
            for (IFile file : iterable) {
                Matcher matcher=null;
                if ((matcher=EI18NConstants.PATTERN.matcher(file.getName())).matches()) {
                    Line line=new Line(file);
                    if (!lines.containsKey(line)) {
                        lines.put(line, line);
                    } else {
                        line=lines.get(line);
                    }
                } else if ((matcher=EI18NConstants.LOCALE_PATTERN.matcher(file.getName())).matches()) {
                    IFile defaultFile=EI18NUtil.getDefaultFile(file);

                    Line line=new Line(defaultFile);
                    if (!lines.containsKey(line)) {
                        lines.put(line, line);
                    } else {
                        line=lines.get(line);
                    }

                    line.files.put(matcher.group(EI18NConstants.LOCALE_GROUP), file);
                    locales.add(matcher.group(EI18NConstants.LOCALE_GROUP));
                }
            }
            {
                int column=3;
                for (String locale : locales) {
                    cell=headerRow.createCell(column++);
                    cell.setCellValue(locale);
                    cell.setCellStyle(headerStyle);
                }
            }

            int maxCols=0;
            for (Line line : lines.keySet()) {
                Properties props=new Properties();
                is=line.mainFile.getContents();
                props.load(is);
                is.close();
                List<Properties> propsList=Lists.newArrayList();
                for (String locale : locales) {
                    Properties localeProps=new Properties();
                    propsList.add(localeProps);
                    //                            for (IFile localeFile : line.files.get(locale)) {
                    IFile localeFile=line.files.get(locale);
                    if (localeFile != null) {
                        Matcher matcher=EI18NConstants.LOCALE_PATTERN.matcher(localeFile.getName());
                        Assert.isTrue(matcher.matches());
                        if (locale.equals(matcher.group(1))) {
                            Assert.isTrue(localeProps.isEmpty());
                            is=localeFile.getContents();
                            localeProps.load(is);
                            is.close();
                        }
                    }
                }
                for (String key : Iterables.filter(Collections.list(props.keys()), String.class)) {
                    if (StringUtils.isNotBlank(key)) {
                        HSSFRow row=sheet.createRow(rownum++);
                        int col=STARTING_COLUMN;
                        row.createCell(col++).setCellValue(line.mainFile.getFullPath().toString() + "#" + key); //$NON-NLS-1$
                        row.createCell(col++).setCellValue((String) props.get(key));
                        for (Properties localeProps : propsList) {
                            HSSFCell cell2=row.createCell(col++);
                            cell2.setCellValue((String) localeProps.get(key));
                        }
                        if (col > maxCols)
                            maxCols=col;
                    }
                }
            }
            createEmptyFormattingRule(
                    sheet,
                    CellRangeAddress.valueOf(CellReference.convertNumToColString(STARTING_COLUMN + 1)
                            + "2:" + CellReference.convertNumToColString(maxCols - 1) + rownum)); //$NON-NLS-1$
            for (int col=STARTING_COLUMN; col < maxCols; col++)
                sheet.autoSizeColumn(col);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    protected void createEmptyFormattingRule(HSSFSheet sheet, CellRangeAddress... regions) {
        HSSFConditionalFormattingRule conditionalFormattingRule=sheet.getSheetConditionalFormatting().createConditionalFormattingRule(ComparisonOperator.EQUAL,
                "\"\""); //$NON-NLS-1$
        PatternFormatting fill1=conditionalFormattingRule.createPatternFormatting();
        fill1.setFillBackgroundColor(IndexedColors.RED.index);
        sheet.getSheetConditionalFormatting().addConditionalFormatting(regions, conditionalFormattingRule);
    }
}
