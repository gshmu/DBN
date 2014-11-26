package com.dci.intellij.dbn.data.export.processor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Workbook;
import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;
import org.odftoolkit.odfdom.doc.table.OdfTable;
import org.odftoolkit.odfdom.doc.table.OdfTableCell;

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

    public void performExport(DataExportModel model, DataExportInstructions instructions, ConnectionHandler connectionHandler) throws DataExportException {

        try {
            OdfSpreadsheetDocument document = OdfSpreadsheetDocument.newSpreadsheetDocument();
/*            OdfContentDom contentDom = document.getContentDom();
            OdfStylesDom stylesDom = document.getStylesDom();
            OdfOfficeAutomaticStyles contentAutoStyles = contentDom.getOrCreateAutomaticStyles();
            OdfOfficeStyles stylesOfficeStyles = document.getOrCreateDocumentStyles();
            OfficeSpreadsheetElement officeSpreadsheet = document.getContentRoot();*/


            OdfTable table = OdfTable.newTable(document);

            if (instructions.createHeader()) {
                table.appendRow();
                for (int columnIndex = 0; columnIndex < model.getColumnCount(); columnIndex++){
                    table.appendColumn();
                    String columnName = model.getColumnName(columnIndex);
                    OdfTableCell cell = table.getCellByPosition(columnIndex + 1, 1);
                    cell.setStringValue(columnName);


/*
                    CellStyle cellStyle = workbook.createCellStyle();
                    Font tableHeadingFont = workbook.createFont();
                    tableHeadingFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
                    cellStyle.setFont(tableHeadingFont);
                    cell.setCellStyle(cellStyle);
*/
                }
            }

/*            CellStyleCache cellStyleCache = new CellStyleCache(workbook, model.getProject());

            for (short rowIndex = 0; rowIndex < model.getRowCount(); rowIndex++) {
                Row row = sheet.createRow(rowIndex + 1);
                for (int columnIndex = 0; columnIndex < model.getColumnCount(); columnIndex++){
                    Cell cell = row.createCell(columnIndex);
                    Object value = model.getValue(rowIndex, columnIndex);
                    if (value != null) {
                        if (value instanceof Number) {
                            Number number = (Number) value;
                            double doubleValue = number.doubleValue();
                            cell.setCellValue(doubleValue);
                            cell.setCellStyle(
                                    doubleValue % 1 == 0 ?
                                            cellStyleCache.getIntegerStyle() :
                                            cellStyleCache.getNumberStyle());

                        } else if (value instanceof Date) {
                            Date date = (Date) value;
                            boolean hasTime = hasTimeComponent(date);
                            cell.setCellValue(date);
                            cell.setCellStyle(hasTime ?
                                    cellStyleCache.getDatetimeStyle() :
                                    cellStyleCache.getDateStyle());
                        } else {
                            cell.setCellValue(value.toString());
                        }
                    }
                }
            }

            for (int columnIndex=0; columnIndex < model.getColumnCount(); columnIndex++){
                sheet.autoSizeColumn(columnIndex);
            }*/

            File file = instructions.getFile();
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                document.save(fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                throw new DataExportException(
                        "Could not write file " + file.getPath() +".\n" +
                                "Reason: " + e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
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
