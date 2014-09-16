package org.eclipse.etools.ei18n.impex;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
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
import org.eclipse.core.runtime.Path;
import org.eclipse.etools.ei18n.extensions.IImpex;
import org.eclipse.etools.ei18n.util.EI18NConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class ExcelImpex implements IImpex {
    protected static final int STARTING_COLUMN=1;

    public IRunnableWithProgress getExportOperation(final Iterable<IFile> iterable, final String destinationValue) {
        return new IRunnableWithProgress() {

            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                FileOutputStream os=null;
                InputStream is=null;
                try {
                    HSSFWorkbook wb=new HSSFWorkbook();
                    HSSFSheet sheet=wb.createSheet("main"); //$NON-NLS-1$
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
                    Multimap<IFile, IFile> files=HashMultimap.create();
                    Collection<String> locales=Sets.newHashSet();
                    for (IFile file : iterable) {
                        Matcher matcher=EI18NConstants.LOCALE_PATTERN.matcher(file.getName());
                        if (matcher.matches()) {
                            IFile defaultFile=file.getParent().getFile(new Path(StringUtils.substringBefore(file.getName(), "_") + ".properties")); //$NON-NLS-1$
                            if (defaultFile.exists()) {
                                files.put(defaultFile, file);
                                locales.add(matcher.group(1));
                            }
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
                    for (IFile file : files.keySet()) {
                        Properties props=new Properties();
                        is=file.getContents();
                        props.load(is);
                        is.close();
                        List<Properties> propsList=Lists.newArrayList();
                        for (String locale : locales) {
                            Properties localeProps=new Properties();
                            propsList.add(localeProps);
                            for (IFile localeFile : files.get(file)) {
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
                            HSSFRow row=sheet.createRow(rownum++);
                            int col=STARTING_COLUMN;
                            row.createCell(col++).setCellValue(file.getFullPath().toString() + "#" + key); //$NON-NLS-1$
                            row.createCell(col++).setCellValue((String) props.get(key));
                            for (Properties localeProps : propsList) {
                                HSSFCell cell2=row.createCell(col++);
                                cell2.setCellValue((String) localeProps.get(key));
                            }
                            if (col > maxCols)
                                maxCols=col;
                        }
                    }
                    createEmptyFormattingRule(
                            sheet,
                            CellRangeAddress.valueOf(CellReference.convertNumToColString(STARTING_COLUMN + 1)
                                    + "2:" + CellReference.convertNumToColString(maxCols - 1) + rownum)); //$NON-NLS-1$
                    for (int col=STARTING_COLUMN; col < maxCols; col++)
                        sheet.autoSizeColumn(col);
                    os=new FileOutputStream(destinationValue);
                    wb.write(os);
                } catch (FileNotFoundException e) {
                    throw new InvocationTargetException(e);
                } catch (IOException e) {
                    throw new InvocationTargetException(e);
                } catch (CoreException e) {
                    throw new InvocationTargetException(e);
                } finally {
                    IOUtils.closeQuietly(os);
                    IOUtils.closeQuietly(is);
                }
            }
        };
    }


    public String getFileExtension() {
        return "*.xls"; //$NON-NLS-1$
    }

    protected void createEmptyFormattingRule(HSSFSheet sheet, CellRangeAddress... regions) {
        HSSFConditionalFormattingRule conditionalFormattingRule=sheet.getSheetConditionalFormatting().createConditionalFormattingRule(ComparisonOperator.EQUAL,
                "\"\""); //$NON-NLS-1$
        PatternFormatting fill1=conditionalFormattingRule.createPatternFormatting();
        fill1.setFillBackgroundColor(IndexedColors.RED.index);
        sheet.getSheetConditionalFormatting().addConditionalFormatting(regions, conditionalFormattingRule);
    }
}
