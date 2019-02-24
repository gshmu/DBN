package com.dci.intellij.dbn.data.model.resultSet;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.thread.Background;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.jdbc.DBNResultSet;
import com.dci.intellij.dbn.connection.jdbc.ResourceStatus;
import com.dci.intellij.dbn.data.model.sortable.SortableDataModel;
import com.dci.intellij.dbn.data.model.sortable.SortableDataModelState;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.TableModelEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResultSetDataModel<T extends ResultSetDataModelRow> extends SortableDataModel<T> {
    private DBNResultSet resultSet;
    private ConnectionHandler connectionHandler;
    private boolean resultSetExhausted = false;

    public ResultSetDataModel(ConnectionHandler connectionHandler) {
        super(connectionHandler.getProject());
        this.connectionHandler = connectionHandler;
    }

    public ResultSetDataModel(DBNResultSet resultSet, ConnectionHandler connectionHandler, int maxRecords) throws SQLException {
        super(connectionHandler.getProject());
        this.connectionHandler = connectionHandler;
        this.resultSet = resultSet;
        setHeader(new ResultSetDataModelHeader(connectionHandler, resultSet));
        fetchNextRecords(maxRecords, false);
        Disposer.register(connectionHandler, this);
    }

    protected T createRow(int resultSetRowIndex) throws SQLException {
        return (T) new ResultSetDataModelRow(this, getResultSet(), resultSetRowIndex);
    }

    @NotNull
    protected DBNResultSet getResultSet() {
        return Failsafe.get(resultSet);
    }

    @NotNull
    public DBNConnection getConnection() {
        return getResultSet().getConnection();
    }

    public void setResultSet(DBNResultSet resultSet) throws SQLException {
        this.resultSet = resultSet;
    }

    @Nullable
    public T getRowAtResultSetIndex(int index) {
        // model may be reloading when this is called, hence
        // IndexOutOfBoundsException is thrown if the range is not checked
        List<T> rows = getRows();
        for (T row : rows) {
            if (row.getResultSetRowIndex() == index) {
                return row;
            }
        }
        return null;
    }

    public int fetchNextRecords(int records, boolean reset) throws SQLException {
        checkDisposed();
        int originalRowCount = getRowCount();
        if (resultSetExhausted) return originalRowCount;

        int initialIndex = reset ? 0 : originalRowCount;
        int count = 0;

        final List<T> oldRows = getRows();
        List<T> newRows = reset ? new ArrayList<>(oldRows.size()) : new ArrayList<>(oldRows);

        if (resultSet == null || ConnectionUtil.isClosed(resultSet)) {
            resultSetExhausted = true;
        } else {
            DBNConnection connection = resultSet.getConnection();
            try {
                connection.set(ResourceStatus.ACTIVE, true);
                connection.updateLastAccess();
                while (count < records) {
                    checkDisposed();
                    if (resultSet != null && resultSet.next()) {
                        count++;
                        T row = createRow(initialIndex + count);
                        newRows.add(row);
                    } else {
                        resultSetExhausted = true;
                        break;
                    }
                }
            } finally {
                connection.set(ResourceStatus.ACTIVE, false);
                connection.updateLastAccess();
            }
        }

        checkDisposed();

        sort(newRows);
        setRows(newRows);

        if (reset) {
            disposeRows(oldRows);
        }

        int newRowCount = getRowCount();
        if (reset) notifyListeners(null, new TableModelEvent(ResultSetDataModel.this, TableModelEvent.HEADER_ROW));
        if (newRowCount > originalRowCount) notifyRowsInserted(originalRowCount, newRowCount);
        if (newRowCount < originalRowCount) notifyRowsDeleted(newRowCount, originalRowCount);
        int updateIndex = Math.min(originalRowCount, newRowCount);
        if (updateIndex > 0) notifyRowsUpdated(0, updateIndex);


        return newRowCount;
    }

    private void disposeRows(final List<T> oldRows) {
        Background.run(() -> {
            // dispose old content
            for (T row : oldRows) {
                disposeRow(row);
            }
        });
    }

    protected void disposeRow(T row) {
        Disposer.dispose(row);
    }

    public boolean isResultSetExhausted() {
        return resultSetExhausted;
    }

    public void setResultSetExhausted(boolean resultSetExhausted) {
        this.resultSetExhausted = resultSetExhausted;
    }

    public void closeResultSet() {
        ConnectionUtil.close(resultSet);
    }

    @NotNull
    public ConnectionHandler getConnectionHandler() {
        return Failsafe.get(connectionHandler);
    }

    @Override
    protected SortableDataModelState createState() {
        return new SortableDataModelState();
    }

    @Override
    public boolean isReadonly() {
        return true;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return getColumnInfo(columnIndex).getDataType().getTypeClass();
    }

    @Override
    public void dispose() {
        super.dispose();
        closeResultSet();
        resultSet = null;
        connectionHandler = null;
    }
}
