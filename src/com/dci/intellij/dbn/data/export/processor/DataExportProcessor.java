package com.dci.intellij.dbn.data.export.processor;

import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.locale.Formatter;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.export.DataExportException;
import com.dci.intellij.dbn.data.export.DataExportFormat;
import com.dci.intellij.dbn.data.export.DataExportInstructions;
import com.dci.intellij.dbn.data.export.DataExportModel;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public abstract class DataExportProcessor {
    public abstract boolean canCreateHeader();
    public abstract boolean canExportToClipboard();
    public abstract boolean canQuoteValues();
    public abstract boolean supportsFileEncoding();
    public abstract void performExport(DataExportModel model, DataExportInstructions instructions, ConnectionHandler connectionHandler) throws DataExportException, InterruptedException;

    Formatter getFormatter(Project project) {
        return Formatter.getInstance(project).clone();
    }

    public abstract String getFileExtension();

    public void export(DataExportModel model, DataExportInstructions instructions, ConnectionHandler connectionHandler)
            throws DataExportException, InterruptedException {
        try {
            if ((model.getColumnCount() == 0 || model.getRowCount() == 0) &&
                    instructions.getScope() == DataExportInstructions.Scope.SELECTION) {
                throw new DataExportException("No content selected for export. Uncheck the Scope \"Selection\" if you want to export the entire content.");
            }
            String fileName = adjustFileName(instructions.getFileName());
            instructions.setFileName(fileName);
            performExport(model, instructions, connectionHandler);
        } catch (InterruptedException e) {
            throw e;
        } catch (DataExportException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataExportException(e.getMessage());
        }
    }

    public abstract DataExportFormat getFormat();

    void writeContent(DataExportInstructions instructions, String content) throws DataExportException {
        if (instructions.getDestination() == DataExportInstructions.Destination.CLIPBOARD) {
            writeToClipboard(content);
        } else {
            writeToFile(instructions.getFile(), content, instructions.getCharset());
        }
    }

    private void writeToFile(File file, String content, Charset charset) throws DataExportException {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset));
            writer.write(content);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new DataExportException("Could not write file " + file.getPath() + ".\n Reason: " + e.getMessage());
        }
    }

    private void writeToClipboard(String content) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(createClipboardContent(content), null);
    }

    public Transferable createClipboardContent(String content) {
        return new StringSelection(content);
    }

    public String adjustFileName(String fileName) {
        return fileName;
    }

    public static boolean hasTimeComponent(Date date) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);

        return
            calendar.get(Calendar.HOUR) != 0 ||
            calendar.get(Calendar.MINUTE) != 0 ||
            calendar.get(Calendar.SECOND) != 0 ||
            calendar.get(Calendar.MILLISECOND) != 0;
    }

    void checkCancelled() throws InterruptedException {
        ProgressIndicator progressIndicator = ProgressMonitor.getProgressIndicator();
        if (progressIndicator != null) {
            if (progressIndicator.isCanceled()) {
                throw new InterruptedException();
            }
        }
    }

}
