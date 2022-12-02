package com.dci.intellij.dbn.data.export.processor;

import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.locale.Formatter;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.export.DataExportException;
import com.dci.intellij.dbn.data.export.DataExportFormat;
import com.dci.intellij.dbn.data.export.DataExportInstructions;
import com.dci.intellij.dbn.data.export.DataExportModel;
import com.dci.intellij.dbn.data.type.GenericDataType;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Objects;


public class JIRAMarkupDataExportProcessor extends DataExportProcessor{
    @Override
    public DataExportFormat getFormat() {
        return DataExportFormat.JIRA;
    }

    @Override
    public String getFileExtension() {
        return "txt";
    }

    @Override
    public boolean supports(DataExportFeature feature) {
        return feature.isOneOf(
                DataExportFeature.HEADER_CREATION,
                DataExportFeature.FRIENDLY_HEADER,
                DataExportFeature.EXPORT_TO_FILE,
                DataExportFeature.EXPORT_TO_CLIPBOARD,
                DataExportFeature.FILE_ENCODING);
    }

    @Override
    public String adjustFileName(String fileName) {
        if (!fileName.endsWith(".txt")) {
            fileName = fileName + ".txt";
        }
        return fileName;
    }

    @Override
    public Transferable createClipboardContent(String content) {
        return new JiraContent(content);
    }

    public static class JiraContent implements Transferable {
        private DataFlavor[] dataFlavors;
        private final String content;

        public JiraContent(String markupText) {
            content = markupText;
            try {
                dataFlavors = new DataFlavor[3];
                dataFlavors[2] = new DataFlavor("text/plain;class=java.lang.String");

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return dataFlavors;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return Objects.equals(flavor.getMimeType(), "text/plain");
        }

        @NotNull
        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException{
            return content;
        }
    }


    @Override
    public void performExport(DataExportModel model, DataExportInstructions instructions, ConnectionHandler connection) throws DataExportException {
        StringBuilder buffer = new StringBuilder();

        if (instructions.isCreateHeader()) {
            buffer.append("||");
            for (int columnIndex = 0; columnIndex < model.getColumnCount(); columnIndex++){
                String columnName = getColumnName(model, instructions, columnIndex);
                buffer.append(columnName).append("||");
            }
            buffer.append("\n");
        }

        Formatter formatter = getFormatter(connection.getProject());

        for (int rowIndex=0; rowIndex < model.getRowCount(); rowIndex++) {
            buffer.append("|");

            for (int columnIndex=0; columnIndex < model.getColumnCount(); columnIndex++){
                ProgressMonitor.checkCancelled();
                GenericDataType genericDataType = model.getGenericDataType(columnIndex);
                Object object = model.getValue(rowIndex, columnIndex);
                String value = formatValue(formatter, object);
                value = value.replaceAll("\\|", "\\|");
                value = value.replaceAll("\\*", "\\*");
                // TODO add more markup escapes

                if (Strings.isEmptyOrSpaces(value)) value = " ";

/*                boolean isNoWrap =
                        genericDataType == GenericDataType.NUMERIC ||
                        genericDataType == GenericDataType.DATE_TIME ||
                        value.length() < 100;

                boolean isAlignRight = genericDataType == GenericDataType.NUMERIC;

                if (isNoWrap) buffer.append(" nowrap");
                if (isAlignRight) buffer.append(" align=\"right\"");*/
                buffer.append(value);
                buffer.append("|");
            }
            buffer.append("\n");
        }

        writeContent(instructions, buffer.toString());
    }
}
