package com.dci.intellij.dbn.data.export.processor;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Workbook;
import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;
import org.odftoolkit.odfdom.doc.table.OdfTable;
import org.odftoolkit.odfdom.doc.table.OdfTableCell;
import org.odftoolkit.odfdom.dom.OdfContentDom;
import org.odftoolkit.odfdom.dom.style.OdfStyleFamily;
import org.odftoolkit.odfdom.incubator.doc.office.OdfOfficeAutomaticStyles;
import org.odftoolkit.odfdom.incubator.doc.style.OdfStyle;

import com.dci.intellij.dbn.common.locale.Formatter;
import com.dci.intellij.dbn.common.locale.options.RegionalSettings;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.export.DataExportException;
import com.dci.intellij.dbn.data.export.DataExportFormat;
import com.dci.intellij.dbn.data.export.DataExportInstructions;
import com.dci.intellij.dbn.data.export.DataExportModel;
import com.intellij.openapi.project.Project;

public class OpenDocumentSpreadsheetProcessor extends DataExportProcessor{

    protected DataExportFormat getFormat() {
        return DataExportFormat.OPENDOC;
    }

    @Override
    public String getFileExtension() {
        return "ods";
    }

    public boolean canCreateHeader() {
        return true;
    }

    public boolean canExportToClipboard() {
        return false;
    }

    public boolean canQuoteValues() {
        return false;
    }

    @Override
    public String adjustFileName(String fileName) {
        if (!fileName.contains(".ods")) {
            fileName = fileName + ".ods";
        }
        return fileName;
    }

    public void performExport(DataExportModel model, DataExportInstructions instructions, ConnectionHandler connectionHandler) throws DataExportException, InterruptedException {
        OdfSpreadsheetDocument document = null;
        OdfContentDom contentDom = null;
        OdfTable table = null;
        try {
            document = OdfSpreadsheetDocument.newSpreadsheetDocument();
            contentDom = document.getContentDom();
            table = document.getTableList().get(0);
            table.setTableName("Test");
        } catch (Exception e) {
            throw new DataExportException("Error creating export model. Reason: " + e.getMessage());
        }


/*
        OdfStylesDom stylesDom = document.getStylesDom();

        OdfOfficeStyles stylesOfficeStyles = document.getOrCreateDocumentStyles();
        OfficeSpreadsheetElement officeSpreadsheet = document.getContentRoot();*/

        OdfOfficeAutomaticStyles contentAutoStyles = contentDom.getOrCreateAutomaticStyles();
        OdfStyle headerStyle = contentAutoStyles.newStyle(OdfStyleFamily.TableCell);


        if (instructions.createHeader()) {
            for (int columnIndex = 0; columnIndex < model.getColumnCount(); columnIndex++){
                OdfTableCell cell = table.getCellByPosition(columnIndex, 0);
                String columnName = model.getColumnName(columnIndex);
                cell.setStringValue(columnName);
                //cell.setCellBackgroundColor("#d3d3d3");

/*
                CellStyle cellStyle = workbook.createCellStyle();
                Font tableHeadingFont = workbook.createFont();
                tableHeadingFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
                cellStyle.setFont(tableHeadingFont);
                cell.setCellStyle(cellStyle);
*/
            }
        }


        for (short rowIndex = 0; rowIndex < model.getRowCount(); rowIndex++) {
            for (int columnIndex = 0; columnIndex < model.getColumnCount(); columnIndex++){
                checkCancelled();
                OdfTableCell cell = table.getCellByPosition(columnIndex, instructions.createHeader() ? rowIndex + 1 : rowIndex);
                Object value = model.getValue(rowIndex, columnIndex);
                if (value != null) {
                    if (value instanceof Number) {
                        Number number = (Number) value;
                        double doubleValue = number.doubleValue();
                        cell.setDoubleValue(doubleValue);
/*
                        cell.setCellStyle(
                                doubleValue % 1 == 0 ?
                                        cellStyleCache.getIntegerStyle() :
                                        cellStyleCache.getNumberStyle());

*/
                    } else if (value instanceof Date) {
                        Date date = (Date) value;
                        boolean hasTime = hasTimeComponent(date);
                        Calendar calendar = new GregorianCalendar();
                        calendar.setTime(date);

                        cell.setDateValue(calendar);
/*
                        cell.setCellStyle(hasTime ?
                                cellStyleCache.getDatetimeStyle() :
                                cellStyleCache.getDateStyle());
*/
                    } else {
                        cell.setStringValue(value.toString());
                    }
                }
            }
        }

/*
        for (int columnIndex=0; columnIndex < model.getColumnCount(); columnIndex++){
            sheet.autoSizeColumn(columnIndex);
        }
*/
        File file = instructions.getFile();
        try {
            document.save(file);
        } catch (Exception e) {
            throw new DataExportException(
                    "Could not write file " + file.getPath() +".\n" +
                            "Reason: " + e.getMessage());
        }
    }

    protected Workbook createWorkbook() {
        return new HSSFWorkbook();
    }

    private class CellStyleCache {
        private Workbook workbook;
        private RegionalSettings regionalSettings;

        private CellStyle dateStyle;
        private CellStyle datetimeStyle;
        private CellStyle numberStyle;
        private CellStyle integerStyle;

        private CellStyleCache(Workbook workbook, Project project) {
            this.workbook = workbook;
            regionalSettings = RegionalSettings.getInstance(project);
        }


        private Formatter getFormatter() {
            return regionalSettings.getFormatter();
        }

        public CellStyle getDateStyle() {
            if (dateStyle == null) {
                dateStyle = workbook.createCellStyle();
                String dateFormatPattern = getFormatter().getDateFormatPattern();
                short dateFormat = getFormat(dateFormatPattern);
                dateStyle.setDataFormat(dateFormat);
            }
            return dateStyle;
        }

        public CellStyle getDatetimeStyle() {
            if (datetimeStyle == null) {
                datetimeStyle = workbook.createCellStyle();
                String datetimeFormatPattern = getFormatter().getDatetimeFormatPattern();
                short dateFormat = getFormat(datetimeFormatPattern);
                datetimeStyle.setDataFormat(dateFormat);
            }

            return datetimeStyle;
        }

        public CellStyle getNumberStyle() {
            if (numberStyle == null) {
                numberStyle = workbook.createCellStyle();
                String numberFormatPattern = getFormatter().getNumberFormatPattern();
                short numberFormat = getFormat(numberFormatPattern);
                numberStyle.setDataFormat(numberFormat);
            }

            return numberStyle;
        }

        public CellStyle getIntegerStyle() {
            if (integerStyle == null) {
                integerStyle = workbook.createCellStyle();
                String integerFormatPattern = getFormatter().getIntegerFormatPattern();
                short integerFormat = getFormat(integerFormatPattern);
                integerStyle.setDataFormat(integerFormat);
            }

            return integerStyle;
        }

        private short getFormat(String datetimeFormatPattern) {
            CreationHelper creationHelper = workbook.getCreationHelper();
            DataFormat dataFormat = creationHelper.createDataFormat();
            return dataFormat.getFormat(datetimeFormatPattern);
        }

    }
}
