package com.dci.intellij.dbn.data.export.processor;

import com.dci.intellij.dbn.common.locale.Formatter;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.export.DataExportException;
import com.dci.intellij.dbn.data.export.DataExportFormat;
import com.dci.intellij.dbn.data.export.DataExportInstructions;
import com.dci.intellij.dbn.data.export.DataExportModel;

public class CustomDataExportProcessor extends DataExportProcessor{
    @Override
    public DataExportFormat getFormat() {
        return DataExportFormat.CUSTOM;
    }

    @Override
    public boolean supports(DataExportFeature feature) {
        return feature.isOneOf(
                DataExportFeature.HEADER_CREATION,
                DataExportFeature.FRIENDLY_HEADER,
                DataExportFeature.EXPORT_TO_FILE,
                DataExportFeature.EXPORT_TO_CLIPBOARD,
                DataExportFeature.VALUE_QUOTING,
                DataExportFeature.FILE_ENCODING);
    }

    @Override
    public String getFileExtension() {
        return "csv";
    }

    @Override
    public void performExport(DataExportModel model, DataExportInstructions instructions, ConnectionHandler connectionHandler) throws DataExportException {
        StringBuilder buffer = new StringBuilder();
        if (instructions.isCreateHeader()) {
            for (int columnIndex=0; columnIndex < model.getColumnCount(); columnIndex++){
                String columnName = getColumnName(model, instructions, columnIndex);
                String separator = instructions.getValueSeparator();
                boolean containsSeparator = columnName.contains(separator);
                boolean quote =
                        instructions.isQuoteAllValues() || (
                        instructions.isQuoteValuesContainingSeparator() && containsSeparator);

                if (containsSeparator && !quote) {
                    throw new DataExportException(
                        "Can not create columns header with the given separator.\n" +
                        "Column " + columnName + " already contains the separator '" + separator + "'. \n" +
                        "Please consider quoting.");
                }

                if (columnIndex > 0) {
                    buffer.append(separator);
                }

                if (quote) {
                    if(columnName.indexOf('"') > -1) {
                        throw new DataExportException(
                            "Can not quote columns header.\n" +
                            "Column " + columnName + " contains quotes.");
                    }
                    buffer.append('"');
                    buffer.append(columnName);
                    buffer.append('"');
                } else {
                    buffer.append(columnName);
                }
            }
            buffer.append('\n');
        }

        Formatter formatter = getFormatter(connectionHandler.getProject());
        for (int rowIndex=0; rowIndex < model.getRowCount(); rowIndex++) {
            for (int columnIndex=0; columnIndex < model.getColumnCount(); columnIndex++){
                checkCancelled();
                String columnName = getColumnName(model, instructions, columnIndex);
                Object object = model.getValue(rowIndex, columnIndex);
                String value = formatValue(formatter, object);
                String separator = instructions.getValueSeparator();

                boolean containsSeparator = value.contains(separator);
                boolean quote =
                        instructions.isQuoteAllValues() || (
                        instructions.isQuoteValuesContainingSeparator() && containsSeparator);

                if (containsSeparator && !quote) {
                    throw new DataExportException(
                        "Can not create row " + rowIndex + " with the given separator.\n" +
                        "Value for column " + columnName + " already contains the separator '" + separator + "'. \n" +
                        "Please consider quoting.");
                }

                if (columnIndex > 0) {
                    buffer.append(separator);
                }

                if (quote) {
                    if(value.indexOf('"') > -1) {
                        throw new DataExportException(
                            "Can not quote value of " + columnName + " at row " + rowIndex + ".\n" +
                            "Value contains quotes itself.");
                    }
                    buffer.append('"');
                    buffer.append(value);
                    buffer.append('"');
                } else {
                    buffer.append(value);
                }
            }
            buffer.append('\n');
        }
        writeContent(instructions, buffer.toString());
    }

}
