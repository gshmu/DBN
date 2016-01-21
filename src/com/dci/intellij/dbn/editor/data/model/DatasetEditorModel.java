package com.dci.intellij.dbn.editor.data.model;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.environment.EnvironmentManager;
import com.dci.intellij.dbn.common.thread.CancellableDatabaseCall;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetDataModel;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.dci.intellij.dbn.editor.data.DatasetEditorError;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilter;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterInput;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterManager;
import com.dci.intellij.dbn.editor.data.options.DataEditorSettings;
import com.dci.intellij.dbn.editor.data.state.DatasetEditorState;
import com.dci.intellij.dbn.editor.data.ui.table.DatasetEditorTable;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBConstraint;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.project.Project;

public class DatasetEditorModel extends ResultSetDataModel<DatasetEditorModelRow> implements ListSelectionListener {
    private boolean isInserting;
    private boolean isModified;
    private DatasetEditor datasetEditor;
    private DataEditorSettings settings;
    private DBObjectRef<DBDataset> datasetRef;

    private CancellableDatabaseCall loaderCall;
    private boolean isDirty;
    private EditableResultSetHandler resultSetHandler;

    private List<DatasetEditorModelRow> changedRows = new ArrayList<DatasetEditorModelRow>();

    public DatasetEditorModel(DatasetEditor datasetEditor) throws SQLException {
        super(datasetEditor.getConnectionHandler());
        Project project = getProject();
        this.datasetEditor = datasetEditor;
        DBDataset dataset = datasetEditor.getDataset();
        this.datasetRef = DBObjectRef.from(dataset);
        this.settings =  DataEditorSettings.getInstance(project);
        setHeader(new DatasetEditorModelHeader(datasetEditor, null));

        EnvironmentManager environmentManager = EnvironmentManager.getInstance(project);
        boolean readonly = environmentManager.isReadonly(dataset, DBContentType.DATA);
        setEnvironmentReadonly(readonly);
    }

    public void load(final boolean useCurrentFilter, final boolean keepChanges) throws SQLException {
        isDirty = false;
        checkDisposed();
        closeResultSet();
        int timeout = settings.getGeneralSettings().getFetchTimeout().value();
        final AtomicReference<Statement> statementRef = new AtomicReference<Statement>();
        final ConnectionHandler connectionHandler = getConnectionHandler();
        Connection connection = connectionHandler.getStandaloneConnection();

        loaderCall = new CancellableDatabaseCall(connectionHandler, connection, timeout, TimeUnit.SECONDS) {
            @Override
            public Object execute() throws Exception {
                ResultSet newResultSet = loadResultSet(useCurrentFilter, statementRef);

                if (newResultSet != null && !newResultSet.isClosed()) {
                    checkDisposed();
                    setResultSet(newResultSet);
                    boolean useSavepoints = !DatabaseFeature.CONNECTION_ERROR_RECOVERING.isSupported(connectionHandler);
                    resultSetHandler = new EditableResultSetHandler(newResultSet, useSavepoints);
                    setResultSetExhausted(false);
                    if (keepChanges) snapshotChanges(); else clearChanges();

                    int rowCount = computeRowCount();
                    fetchNextRecords(rowCount, true);
                    restoreChanges();
                }
                loaderCall = null;
                return null;
            }

            @Override
            public void cancel() throws Exception {
                Statement statement = statementRef.get();
                ConnectionUtil.cancelStatement(statement);
                loaderCall = null;
                isDirty = true;
            }
        };
        loaderCall.start();
    }

    EditableResultSetHandler getResultSetHandler() {
        return resultSetHandler;
    }

    private int computeRowCount() {
        int originalRowCount = getRowCount();
        int stateRowCount = getState().getRowCount();
        int fetchRowCount = Math.max(stateRowCount, originalRowCount);

        int fetchBlockSize = settings.getGeneralSettings().getFetchBlockSize().value();
        fetchRowCount = (fetchRowCount/fetchBlockSize + 1) * fetchBlockSize;

        return Math.max(fetchRowCount, fetchBlockSize);
    }

    public DataEditorSettings getSettings() {
        return settings;
    }

    private ResultSet loadResultSet(boolean useCurrentFilter, AtomicReference<Statement> statementRef) throws SQLException {
        int timeout = settings.getGeneralSettings().getFetchTimeout().value();
        ConnectionHandler connectionHandler = getConnectionHandler();
        Connection connection = connectionHandler.getStandaloneConnection();
        DBDataset dataset = getDataset();
        Project project = dataset.getProject();
        DatasetFilter filter = DatasetFilterManager.EMPTY_FILTER;
        if (useCurrentFilter) {
            DatasetFilterManager filterManager = DatasetFilterManager.getInstance(project);
            filter = filterManager.getActiveFilter(dataset);
            if (filter == null) filter = DatasetFilterManager.EMPTY_FILTER;
        }

        String selectStatement = filter.createSelectStatement(dataset, getState().getSortingState());
        Statement statement = isReadonly() ?
                connection.createStatement() :
                connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        statementRef.set(statement);
        checkDisposed();
        if (timeout != -1) {
            statement.setQueryTimeout(timeout);
        }

        return statement.executeQuery(selectStatement);
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void cancelDataLoad() {
        if (loaderCall != null) {
            loaderCall.requestCancellation();
        }
    }

    public boolean isLoadCancelled() {
        return loaderCall != null && loaderCall.isCancelRequested();
    }

    private void snapshotChanges() {
        for (DatasetEditorModelRow row : getRows()) {
            if (row.isDeleted() || row.isModified() || row.isNew()) {
                changedRows.add(row);
            }
        }
    }

    private void restoreChanges() throws SQLException {
        if (hasChanges()) {
            for (DatasetEditorModelRow row : getRows()) {
                checkDisposed();

                DatasetEditorModelRow changedRow = lookupChangedRow(row, true);
                if (changedRow != null) {
                    row.updateStatusFromRow(changedRow);
                }
            }
            isModified = true;
        }
    }

    private DatasetEditorModelRow lookupChangedRow(DatasetEditorModelRow row, boolean remove) {
        for (DatasetEditorModelRow changedRow : changedRows) {
            if (!changedRow.isDeleted() && changedRow.matches(row, false)) {
                if (remove) changedRows.remove(changedRow);
                return changedRow;
            }
        }
        return null;
    }

    @Override
    protected void disposeRow(DatasetEditorModelRow row) {
        if (!changedRows.contains(row)) {
            super.disposeRow(row);
        }
    }

    @NotNull
    @Override
    public DatasetEditorState getState() {
        return datasetEditor == null ? DatasetEditorState.VOID : datasetEditor.getEditorState();
    }

    private boolean hasChanges() {
        return changedRows.size() > 0;
    }

    private void clearChanges() {
        changedRows.clear();
    }

    public boolean isReadonly() {
        return !isEditable();
    }

    public boolean isEditable() {
        return getDataset().isEditable(DBContentType.DATA);
    }

    @Override
    public DatasetEditorModelHeader getHeader() {
        return (DatasetEditorModelHeader) super.getHeader();
    }

    @Override
    protected DatasetEditorModelRow createRow(int resultSetRowIndex) throws SQLException {
        return new DatasetEditorModelRow(this, getResultSet(), resultSetRowIndex);
    }

    @NotNull
    public DBDataset getDataset() {
        return FailsafeUtil.get(DBObjectRef.get(datasetRef));
    }

    @NotNull
    public DatasetEditor getDatasetEditor() {
        return FailsafeUtil.get(datasetEditor);
    }

    @NotNull
    public DatasetEditorTable getEditorTable() {
        return getDatasetEditor().getEditorTable();
    }

    public boolean isInserting() {
        return isInserting;
    }

    public void setModified(boolean isModified) {
        this.isModified = isModified;
    }

    public boolean isModified() {
        return isInserting || isModified;
    }

    @Nullable
    public DatasetFilterInput resolveForeignKeyRecord(DatasetEditorModelCell cell) {
        DBColumn column = cell.getColumnInfo().getColumn();
        if (column.isForeignKey()) {
            for (DBConstraint constraint : column.getConstraints()) {
                constraint = (DBConstraint) constraint.getUndisposedElement();
                if (constraint != null && constraint.isForeignKey()) {
                    DBConstraint foreignKeyConstraint = constraint.getForeignKeyConstraint();
                    if (foreignKeyConstraint != null) {
                        DBDataset foreignKeyDataset = foreignKeyConstraint.getDataset();
                        DatasetFilterInput filterInput = new DatasetFilterInput(foreignKeyDataset);

                        for (DBColumn constraintColumn : constraint.getColumns()) {
                            constraintColumn = (DBColumn) constraintColumn.getUndisposedElement();
                            if (constraintColumn != null) {
                                DBColumn foreignKeyColumn = constraintColumn.getForeignKeyColumn();
                                if (foreignKeyColumn != null) {
                                    Object value = cell.getRow().getCellForColumn(constraintColumn).getUserValue();
                                    filterInput.setColumnValue(foreignKeyColumn, value);
                                }
                            }
                        }
                        return filterInput;

                    }

                }
            }
        }
        return null;
    }

    /****************************************************************
     *                        Editor actions                        *
     ****************************************************************/
    public void deleteRecords(int[] rowIndexes) {
        DatasetEditorTable editorTable = getEditorTable();
        editorTable.fireEditingCancel();
        for (int index : rowIndexes) {
            DatasetEditorModelRow row = getRowAtIndex(index);
            if (!row.isDeleted()) {
                int rsRowIndex = row.getResultSetRowIndex();
                row.delete();
                if (row.isDeleted()) {
                    shiftResultSetRowIndex(rsRowIndex, -1);
                    notifyRowUpdated(index);
                }
            }
            isModified = true;
        }
        DBDataset dataset = getDataset();
        getConnectionHandler().notifyChanges(dataset.getVirtualFile());
    }

    public void insertRecord(int rowIndex) {
        DatasetEditorTable editorTable = getEditorTable();
        DBDataset dataset = getDataset();
        try {
            isInserting = true;
            editorTable.stopCellEditing();
            resultSetHandler.startInsertRow();
            DatasetEditorModelRow newRow = createRow(getRowCount()+1);
            newRow.setInsert(true);
            addRowAtIndex(rowIndex, newRow);
            notifyRowsInserted(rowIndex, rowIndex);

            editorTable.selectCell(rowIndex, editorTable.getSelectedColumn() == -1 ? 0 : editorTable.getSelectedColumn());

            getConnectionHandler().notifyChanges(dataset.getVirtualFile());
        } catch (SQLException e) {
            MessageUtil.showErrorDialog(getProject(), "Could not insert record for " + dataset.getQualifiedNameWithType() + ".", e);
        }
    }

    public void duplicateRecord(int rowIndex) {
        DatasetEditorTable editorTable = getEditorTable();
        DBDataset dataset = getDataset();
        try {
            isInserting = true;
            editorTable.stopCellEditing();
            int insertIndex = rowIndex + 1;
            resultSetHandler.startInsertRow();
            DatasetEditorModelRow oldRow = getRowAtIndex(rowIndex);
            DatasetEditorModelRow newRow = createRow(getRowCount() + 1);
            newRow.setInsert(true);
            newRow.updateDataFromRow(oldRow);
            addRowAtIndex(insertIndex, newRow);
            notifyRowsInserted(insertIndex, insertIndex);

            editorTable.selectCell(insertIndex, editorTable.getSelectedColumn());
            getConnectionHandler().notifyChanges(dataset.getVirtualFile());
        } catch (SQLException e) {
            MessageUtil.showErrorDialog(getProject(), "Could not duplicate record in " + dataset.getQualifiedNameWithType() + ".", e);
        }
    }

    public void postInsertRecord(boolean propagateError, boolean rebuild, boolean reset) throws SQLException {
        DatasetEditorTable editorTable = getEditorTable();
        DatasetEditorModelRow row = getInsertRow();
        if (row != null) {
            try {
                editorTable.stopCellEditing();
                resultSetHandler.insertRow();
                row.setInsert(false);
                row.setNew(true);
                isModified = true;
                isInserting = false;
                if (rebuild) load(true, true);
            } catch (SQLException e) {
                DatasetEditorError error = new DatasetEditorError(getConnectionHandler(), e);
                if (reset) {
                    isInserting = false;
                } else {
                    row.notifyError(error, true, true);
                }
                if (!error.isNotified() || propagateError) throw e;
            }
        }
    }

    public void cancelInsert(boolean notifyListeners) {
        DatasetEditorTable editorTable = getEditorTable();
        try {
            editorTable.fireEditingCancel();
            DatasetEditorModelRow insertRow = getInsertRow();
            if (insertRow != null) {
                int rowIndex = insertRow.getIndex();
                removeRowAtIndex(rowIndex);
                if (notifyListeners) notifyRowsDeleted(rowIndex, rowIndex);
            }
            resultSetHandler.cancelInsertRow();
            isInserting = false;

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * after delete or insert performed on a result set, the row indexes have to be shifted accordingly
     */
    private void shiftResultSetRowIndex(int fromIndex, int shifting) {
        for (DatasetEditorModelRow row : getRows()) {
            if (row.getResultSetRowIndex() > fromIndex) {
                row.shiftResultSetRowIndex(shifting);
            }
        }
    }

    @Nullable
    public DatasetEditorModelRow getInsertRow() {
        for (DatasetEditorModelRow row : getRows()) {
            if (row.isInsert()) {
                return row;
            }
        }
        return null;
    }

    public int getInsertRowIndex() {
        DatasetEditorModelRow insertRow = getInsertRow();
        return insertRow == null ? -1 : insertRow.getIndex();
    }

    public void revertChanges() {
        for (DatasetEditorModelRow row : getRows()) {
            row.revertChanges();
        }
    }

    /*********************************************************
     *                      DataModel                       *
     *********************************************************/
    public DatasetEditorModelCell getCellAt(int rowIndex, int columnIndex) {
        return (DatasetEditorModelCell) super.getCellAt(rowIndex, columnIndex);
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        DatasetEditorModelCell cell = getCellAt(rowIndex, columnIndex);
        cell.updateUserValue(value, false);
    }

    public void setValueAt(Object value, String errorMessage,  int rowIndex, int columnIndex) {
        DatasetEditorModelCell cell = getCellAt(rowIndex, columnIndex);
        cell.updateUserValue(value, errorMessage);
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        DatasetEditorTable editorTable = getEditorTable();
        DatasetEditorState editorState = getState();
        if (!isReadonly() && !isEnvironmentReadonly() && !editorState.isReadonly() && getConnectionHandler().isConnected()) {
            if (!editorTable.isLoading() && editorTable.getSelectedColumnCount() <= 1 && editorTable.getSelectedRowCount() <= 1) {
                DatasetEditorModelRow row = getRowAtIndex(rowIndex);
                return row != null && !(isInserting && !row.isInsert()) && !row.isDeleted();
            }
        }
        return false;
    }

    /*********************************************************
     *                ListSelectionListener                  *
     *********************************************************/
    public void valueChanged(ListSelectionEvent event) {
        if (isInserting && !event.getValueIsAdjusting()) {
            DatasetEditorModelRow insertRow = getInsertRow();
            if (insertRow != null) {
                int index = insertRow.getIndex();

                ListSelectionModel listSelectionModel = (ListSelectionModel) event.getSource();
                int selectionIndex = listSelectionModel.getLeadSelectionIndex();

                if (index != selectionIndex) {
                    //postInsertRecord();
                }
            }
        }
    }

    /*********************************************************
     *                       Disposable                      *
     *********************************************************/
    @Override
    public void dispose() {
        if (!isDisposed()) {
            super.dispose();
            datasetEditor = null;
            changedRows.clear();
            settings = null;
        }
    }
}
