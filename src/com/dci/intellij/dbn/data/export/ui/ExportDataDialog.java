package com.dci.intellij.dbn.data.export.ui;

import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.data.export.DataExportInstructions;
import com.dci.intellij.dbn.data.export.DataExportManager;
import com.dci.intellij.dbn.data.grid.ui.table.resultSet.ResultSetTable;
import com.dci.intellij.dbn.execution.ExecutionResult;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ExportDataDialog extends DBNDialog<ExportDataForm> {
    private ResultSetTable table;
    private ConnectionHandlerRef connectionHandlerRef;
    private DBObjectRef sourceObjectRef;

    public ExportDataDialog(ResultSetTable table, @NotNull DBObject sourceObject) {
        this(table, sourceObject, sourceObject.getConnectionHandler());
    }

    public ExportDataDialog(ResultSetTable table, @NotNull ExecutionResult executionResult) {
        this(table, null, executionResult.getConnectionHandler());
    }


    private ExportDataDialog(ResultSetTable table, @Nullable DBObject sourceObject, @NotNull ConnectionHandler connectionHandler) {
        super(connectionHandler.getProject(), "Export data", true);
        this.table = table;
        this.connectionHandlerRef = connectionHandler.getRef();
        this.sourceObjectRef = DBObjectRef.from(sourceObject);
        Disposer.register(this, table);
        init();
    }

    @NotNull
    @Override
    protected ExportDataForm createComponent() {
        DBObject sourceObject = DBObjectRef.get(sourceObjectRef);
        ConnectionHandler connectionHandler = connectionHandlerRef.ensure();
        DataExportManager exportManager = DataExportManager.getInstance(connectionHandler.getProject());
        DataExportInstructions instructions = exportManager.getExportInstructions();
        boolean hasSelection = table.getSelectedRowCount() > 1 || table.getSelectedColumnCount() > 1;
        instructions.setBaseName(table.getName());
        return new ExportDataForm(this, instructions, hasSelection, connectionHandler, sourceObject);
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandlerRef.ensure();
    }

    @NotNull
    @Override
    protected Action[] createActions() {
        return new Action[]{
                new DialogWrapperAction("Export") {
                    @Override
                    protected void doAction(ActionEvent actionEvent) {
                        doOKAction();
                    }
                },
                getCancelAction()};
    }

    @Override
    protected void doOKAction() {
        getComponent().validateEntries(
                () -> Progress.modal(
                        getProject(),
                        "Creating export file", true,
                        (progress) -> {
                            ConnectionHandler connectionHandler = getConnectionHandler();
                            DataExportManager exportManager = DataExportManager.getInstance(connectionHandler.getProject());
                            DataExportInstructions exportInstructions = getComponent().getExportInstructions();
                            exportManager.setExportInstructions(exportInstructions);
                            exportManager.exportSortableTableContent(
                                    table,
                                    exportInstructions,
                                    connectionHandler,
                                    () -> Dispatch.invoke(() -> ExportDataDialog.super.doOKAction()));
                        })
        );
    }
}
