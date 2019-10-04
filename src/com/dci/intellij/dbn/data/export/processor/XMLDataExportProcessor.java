package com.dci.intellij.dbn.data.export.processor;

import com.dci.intellij.dbn.common.locale.Formatter;
import com.dci.intellij.dbn.common.util.ClipboardUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.export.DataExportException;
import com.dci.intellij.dbn.data.export.DataExportFormat;
import com.dci.intellij.dbn.data.export.DataExportInstructions;
import com.dci.intellij.dbn.data.export.DataExportModel;
import com.dci.intellij.dbn.data.type.GenericDataType;

import java.awt.datatransfer.Transferable;


public class XMLDataExportProcessor extends DataExportProcessor{
    @Override
    public DataExportFormat getFormat() {
        return DataExportFormat.XML;
    }

    @Override
    public String getFileExtension() {
        return "xml";
    }

    @Override
    public String adjustFileName(String fileName) {
        if (!fileName.contains(".xml")) {
            fileName = fileName + ".xml";
        }
        return fileName;
    }

    @Override
    public boolean canCreateHeader() {
        return false;
    }

    @Override
    public boolean canExportToClipboard() {
        return true;
    }

    @Override
    public boolean canQuoteValues() {
        return false;
    }

    @Override
    public boolean supportsFileEncoding() {
        return true;
    }

    @Override
    public Transferable createClipboardContent(String content) {
        return ClipboardUtil.createXmlContent(content);
    }

    @Override
    public void performExport(DataExportModel model, DataExportInstructions instructions, ConnectionHandler connectionHandler) throws DataExportException {
        StringBuilder buffer = new StringBuilder();
        buffer.append("<table name=\"");
        buffer.append(model.getTableName());
        buffer.append("\">\n");
        Formatter formatter = getFormatter(connectionHandler.getProject());

        for (int rowIndex=0; rowIndex < model.getRowCount(); rowIndex++) {
            buffer.append("    <row index=\"");
            buffer.append(rowIndex);
            buffer.append("\">\n");
            for (int columnIndex=0; columnIndex < model.getColumnCount(); columnIndex++){
                checkCancelled();
                String columnName = model.getColumnName(columnIndex);
                GenericDataType genericDataType = model.getGenericDataType(columnIndex);

                String value = null;
                if (genericDataType.isOneOf(
                        GenericDataType.BOOLEAN,
                        GenericDataType.LITERAL,
                        GenericDataType.NUMERIC,
                        GenericDataType.ROWID,
                        GenericDataType.DATE_TIME,
                        GenericDataType.XMLTYPE,
                        GenericDataType.CLOB,
                        GenericDataType.BLOB)) {

                    Object object = model.getValue(rowIndex, columnIndex);
                    value = formatValue(formatter, object);
                }

                if (value == null) value = "";
                if (value.contains("<") || value.contains(">")) {
                    value = "<![CDATA[\n" + value + "\n]]>";
                }

                boolean isCDATA = StringUtil.containsOneOf(value, "\n", "<", ">");
                boolean isWrap = value.length() > 100 || isCDATA;

                buffer.append("        <column name=\"");
                buffer.append(columnName);
                buffer.append("\">");
                if (isWrap) {
                    value = ("\n" + value);//.replace("\n", "\n            ");
                }
                
                if (isCDATA) {
                    buffer.append("\n            <![CDATA[");
                    buffer.append(value);
                    buffer.append("\n            ]]>");
                } else {
                    buffer.append(value);
                }
                buffer.append(isWrap ? "\n        </column>\n" : "</column>\n");
            }

            buffer.append("    </row>\n");
        }
        buffer.append("</table>\n");


        writeContent(instructions, buffer.toString());
    }
}
