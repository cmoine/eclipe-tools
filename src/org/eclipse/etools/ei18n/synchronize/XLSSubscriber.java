package org.eclipse.etools.ei18n.synchronize;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.eclipse.core.resources.IResource;
import org.eclipse.etools.Activator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.variants.ResourceVariantTree;
import org.eclipse.team.core.variants.SessionResourceVariantByteStore;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class XLSSubscriber extends AbstractSubscriber {
    private static final int MAX_DISPLAYED_CONFLICTS=20;

    private List<Row> rows;
    private int keyCol=-1;
    private int defaultCol=-1;

    private final Map<String, Integer> translations=Maps.newHashMap();

    public XLSSubscriber(IResource[] roots, File file) throws IOException {
        super(roots);
        InputStream is=null;
        try {
            is=new FileInputStream(file);
            HSSFWorkbook wb=new HSSFWorkbook(is);
            HSSFSheet sheet=wb.getSheetAt(0);
            rows=Lists.newArrayListWithCapacity(sheet.getLastRowNum() - 1);
            for (int i=1; i < sheet.getLastRowNum(); i++) {
                rows.add(sheet.getRow(i));
            }
            HSSFRow firstRow=sheet.getRow(0);
            for (int col=0;; col++) {
                if (firstRow.getCell(col) == null) {
                    if (translations.isEmpty()) {
                        Activator.logError("Impossible de trouver la colonne => " + col, null); //$NON-NLS-1$
                        continue;
                    } else {
                        break;
                    }
            	}
                String cellValue=firstRow.getCell(col).getStringCellValue();
                if (StringUtils.equalsIgnoreCase(cellValue, "key")) { //$NON-NLS-1$
                    keyCol=col;
                } else if (StringUtils.contains(cellValue.toLowerCase(), "default")) { //$NON-NLS-1$
                    defaultCol=col;
                } else if (StringUtils.isNotEmpty(cellValue)) {
                    translations.put(cellValue.trim(), col);
                }
            }

            final List<String> conflicts=new ArrayList<String>();
            HSSFSheet checkSheet=wb.getSheetAt(1);
            for (int line=0; line < rows.size(); line++) {
                String cell1=sheet.getRow(line + 1).getCell(keyCol).getStringCellValue();
                String cell2=checkSheet.getRow(line + 1).getCell(keyCol).getStringCellValue();
                if (!cell1.equals(cell2)) {
                    if (conflicts.size() > MAX_DISPLAYED_CONFLICTS) {
                        conflicts.add("..."); //$NON-NLS-1$
                        break;
                    }

                    conflicts.add(cell1 + " instead of " + cell2); //$NON-NLS-1$
                }
            }
            if (!conflicts.isEmpty()) {
                Display.getDefault().asyncExec(new Runnable() {
                    public void run() {
                        MessageDialog.openWarning(Display.getCurrent().getActiveShell(), "Warning", "Keys were modified:\n" + Joiner.on(',').join(conflicts)); //$NON-NLS-1$//$NON-NLS-2$
                    }
                });
            }
        } finally {
            IOUtils.closeQuietly(is);
        }
	}

    @Override
    protected ResourceVariantTree createResourceVariantTree(SessionResourceVariantByteStore sessionResourceVariantByteStore) {
        return new XLSResourceVariantTree(this, sessionResourceVariantByteStore);
    }

    public List<Row> getRows() {
        return rows;
    }

    public int getKeyCol() {
        return keyCol;
    }

    public int getDefaultCol() {
        return defaultCol;
    }

    public Map<String, Integer> getTranslations() {
        return translations;
    }
}
