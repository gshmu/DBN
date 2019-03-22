package com.dci.intellij.dbn.editor.data.model;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ResultSetUtil;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.data.value.ValueAdapter;
import com.dci.intellij.dbn.database.DatabaseFeature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

public abstract class ResultSetAdapter extends ResultSetUtil implements Disposable {
    private boolean useSavePoints;
    private boolean insertMode;
    private DatasetEditorModel model;
    public ResultSetAdapter(DatasetEditorModel model) {
        this.model = model;
        ConnectionHandler connectionHandler = model.getConnectionHandler();
        useSavePoints = !DatabaseFeature.CONNECTION_ERROR_RECOVERY.isSupported(connectionHandler);
    }

    public boolean isUseSavePoints() {
        return useSavePoints;
    }

    public boolean isInsertMode() {
        return insertMode;
    }

    public void setInsertMode(boolean insertMode) {
        this.insertMode = insertMode;
    }

    public DatasetEditorModel getModel() {
        return model;
    }

    public abstract void scroll(int rowIndex) throws SQLException;

    public abstract void updateRow() throws SQLException;

    public abstract void refreshRow() throws SQLException;

    public abstract void startInsertRow() throws SQLException;

    public abstract void cancelInsertRow() throws SQLException;

    public abstract void insertRow() throws SQLException;

    public abstract void deleteRow() throws SQLException;

    public abstract void setValue(int columnIndex, @NotNull ValueAdapter valueAdapter, @Nullable Object value) throws SQLException;

    public abstract void setValue(int columnIndex, @NotNull DBDataType dataType, @Nullable Object value) throws SQLException;

    @Override
    public void disposeInner() {
        Disposer.nullify(this);
        super.disposeInner();
    }
}
