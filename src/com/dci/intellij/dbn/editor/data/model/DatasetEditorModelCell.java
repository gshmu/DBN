package com.dci.intellij.dbn.editor.data.model;


import static com.dci.intellij.dbn.editor.data.model.RecordStatus.*;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellEditor;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.locale.Formatter;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetDataModelCell;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.data.type.GenericDataType;
import com.dci.intellij.dbn.data.value.ValueAdapter;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.editor.data.DatasetEditorError;
import com.dci.intellij.dbn.editor.data.ui.DatasetEditorErrorForm;
import com.dci.intellij.dbn.editor.data.ui.table.DatasetEditorTable;
import com.dci.intellij.dbn.editor.data.ui.table.cell.DatasetTableCellEditor;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.openapi.diagnostic.Logger;

public class DatasetEditorModelCell extends ResultSetDataModelCell implements ChangeListener {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    private Object originalUserValue;
    private DatasetEditorError error;
    private boolean isModified;

    public DatasetEditorModelCell(DatasetEditorModelRow row, ResultSet resultSet, DatasetEditorColumnInfo columnInfo) throws SQLException {
        super(row, resultSet, columnInfo);
        originalUserValue = getUserValue();
    }

    @Override
    public DatasetEditorColumnInfo getColumnInfo() {
        return (DatasetEditorColumnInfo) super.getColumnInfo();
    }

    public void updateUserValue(Object newUserValue, boolean bulk) {
        boolean valueChanged = userValueChanged(newUserValue);
        if (hasError() || valueChanged) {
            DatasetEditorModelRow row = getRow();
            ResultSetAdapter resultSetAdapter = getModel().getResultSetAdapter();
            try {
                resultSetAdapter.scroll(row.getResultSetRowIndex());
            } catch (Exception e) {
                MessageUtil.showErrorDialog(getProject(), "Could not update cell value for " + getColumnInfo().getName() + ".", e);
                return;
            }
            GenericDataType genericDataType = getColumnInfo().getDataType().getGenericDataType();
            final boolean isValueAdapter = ValueAdapter.supports(genericDataType);

            final ConnectionHandler connectionHandler = getConnectionHandler();
            try {
                clearError();
                final int columnIndex = getColumnInfo().getResultSetColumnIndex();
                if (isValueAdapter && userValue == null) {
                    userValue = ValueAdapter.create(genericDataType);
                }

                if (isValueAdapter) {
                    ValueAdapter valueAdapter = (ValueAdapter) userValue;
                    if (newUserValue instanceof ValueAdapter) {
                        ValueAdapter newValueAdapter = (ValueAdapter) newUserValue;
                        newUserValue = newValueAdapter.read();
                    }
                    resultSetAdapter.setValue(columnIndex, valueAdapter, newUserValue);
                } else {
                    DBDataType dataType = getColumnInfo().getDataType();
                    resultSetAdapter.setValue(columnIndex, dataType, newUserValue);
                }


                resultSetAdapter.updateRow();
            } catch (Exception e) {
                DatasetEditorError error = new DatasetEditorError(connectionHandler, e);

                // error may affect other cells in the row (e.g. foreign key constraint for multiple primary key)
                if (e instanceof SQLException) getRow().notifyError(error, false, !bulk);

                // if error was not notified yet on row level, notify it on cell isolation level
                if (!error.isNotified()) notifyError(error, !bulk);
            } finally {
                if (valueChanged) {
                    if (!isValueAdapter) {
                        setUserValue(newUserValue);
                    }
                    DBNConnection connection = getModel().getConnection();
                    connection.notifyDataChanges(getDataset().getVirtualFile());
                    EventUtil.notify(getProject(), DatasetEditorModelCellValueListener.TOPIC).valueChanged(this);
                }
                try {
                    resultSetAdapter.refreshRow();
                } catch (SQLException e) {
                    DatasetEditorError error = new DatasetEditorError(connectionHandler, e);
                    getRow().notifyError(error, false, !bulk);
                }
            }

            if (row.isNot(INSERTING) && !connectionHandler.isAutoCommit()) {
                isModified = true;

                row.reset();
                row.set(RecordStatus.MODIFIED, true);
                row.getModel().setModified(true);
            }
        }
    }

    protected DBDataset getDataset() {
        return getEditorModel().getDataset();
    }

    private boolean userValueChanged(Object newUserValue) {
        if (userValue instanceof ValueAdapter) {
            ValueAdapter valueAdapter = (ValueAdapter) userValue;
            try {
                return !CommonUtil.safeEqual(valueAdapter.read(), newUserValue);
            } catch (SQLException e) {
                return true;
            }
        }

        if (userValue != null && newUserValue != null) {
            if (userValue.equals(newUserValue)) {
                return false;
            }
            // user input may not contain the entire precision (e.g. date time format)
            Formatter formatter = getFormatter();
            String formattedValue1 = formatter.formatObject(userValue);
            String formattedValue2 = formatter.formatObject(newUserValue);
            return !formattedValue1.equals(formattedValue2);
        }
        
        return !CommonUtil.safeEqual(userValue, newUserValue);
    }

    public void updateUserValue(Object userValue, String errorMessage) {
        if (!CommonUtil.safeEqual(userValue, getUserValue()) || hasError()) {
            DatasetEditorModelRow row = getRow();
            DatasetEditorError error = new DatasetEditorError(errorMessage, getColumnInfo().getColumn());
            getRow().notifyError(error, true, true);
            setUserValue(userValue);
            ConnectionHandler connectionHandler = getConnectionHandler();
            if (row.isNot(INSERTING) && !connectionHandler.isAutoCommit()) {
                isModified = true;

                row.reset();
                row.set(MODIFIED, true);
                row.getModel().setModified(true);
                getConnection().updateLastAccess();
            }
        }
    }

    public boolean matches(DatasetEditorModelCell remoteCell, boolean lenient) {
        if (CommonUtil.safeEqual(getUserValue(), remoteCell.getUserValue())){
            return true;
        }
        if (lenient && (getRow().is(INSERTED) || getRow().is(MODIFIED)) && getUserValue() == null && remoteCell.getUserValue() != null) {
            return true;
        }
        return false;
    }

    public ConnectionHandler getConnectionHandler() {
        return getEditorModel().getConnectionHandler();
    }

    private DatasetEditorTable getEditorTable() {
        return getEditorModel().getEditorTable();
    }

    @NotNull
    private DatasetEditorModel getEditorModel() {
        return getRow().getModel();
    }

    public void edit() {
        int index = getEditorTable().convertColumnIndexToView(getIndex());
        if (index > 0) {
            DatasetEditorTable table = getEditorTable();
            table.editCellAt(getRow().getIndex(), index);
        }
    }

    public void editPrevious() {
        int index = getEditorTable().convertColumnIndexToView(getIndex());
        if (index > 0) {
            DatasetEditorTable table = getEditorTable();
            DatasetEditorModelRow row = getRow();
            table.editCellAt(row.getIndex(), index -1);
        }
    }

    public void editNext(){
        int index = getEditorTable().convertColumnIndexToView(getIndex());
        DatasetEditorModelRow row = getRow();
        if (index < row.getCells().size()-1) {
            DatasetEditorTable table = getEditorTable();
            table.editCellAt(row.getIndex(), index + 1);
        }
    }

    @NotNull
    public DatasetEditorModelRow getRow() {
        return (DatasetEditorModelRow) super.getRow();
    }

    @NotNull
    @Override
    public DatasetEditorModel getModel() {
        return (DatasetEditorModel) super.getModel();
    }

    void setOriginalUserValue(Object value) {
        if (originalUserValue == null) {
            isModified = value != null;
        } else {
            isModified = !originalUserValue.equals(value);
        }
        this.originalUserValue = value;
    }

    public Object getOriginalUserValue() {
        return originalUserValue;
    }

    public boolean isModified() {
        return isModified;
    }

    public boolean isEditing() {
        DatasetEditorTable table = getEditorTable();
        return table.isEditing() &&
               table.isCellSelected(getRow().getIndex(), getIndex());
    }

    public boolean isNavigable() {
        DBColumn column = getColumnInfo().getColumn();
        return column.isForeignKey() && getUserValue() != null;
    }

    private void notifyCellUpdated() {
        getEditorModel().notifyCellUpdated(getRow().getIndex(), getIndex());
    }

    private void scrollToVisible() {
        DatasetEditorTable table = getEditorTable();
        table.scrollRectToVisible(table.getCellRect(getRow().getIndex(), getIndex(), true));
    }

    public boolean isResultSetUpdatable() {
        return getRow().isResultSetUpdatable();
    }

    /*********************************************************
     *                    ChangeListener                     *
     *********************************************************/
    public void stateChanged(ChangeEvent e) {
        notifyCellUpdated();
    }


    /*********************************************************
     *                        ERROR                          *
     *********************************************************/
    public boolean hasError() {
        if (error != null && error.isDirty()) {
            error = null;
        }
        return error != null;
    }

    boolean notifyError(DatasetEditorError error, final boolean showPopup) {
        error.setNotified(true);
        if(!CommonUtil.safeEqual(this.error, error)) {
            clearError();
            this.error = error;
            notifyCellUpdated();
            if (showPopup) scrollToVisible();
            if (isEditing()) {
                DatasetEditorTable table = getEditorTable();
                TableCellEditor tableCellEditor = table.getCellEditor();
                if (tableCellEditor instanceof DatasetTableCellEditor) {
                    DatasetTableCellEditor cellEditor = (DatasetTableCellEditor) tableCellEditor;
                    cellEditor.highlight(DatasetTableCellEditor.HIGHLIGHT_TYPE_ERROR);
                }
            }
            error.addChangeListener(this);
            if (showPopup) showErrorPopup();
            return true;
        }
        return false;
    }

    void showErrorPopup() {
        SimpleLaterInvocator.invoke(() -> {
            if (!isDisposed()) {
                DatasetEditorModelRow row = getRow();
                DatasetEditorModel model = row.getModel();
                DatasetEditorTable editorTable = model.getEditorTable();
                if (!editorTable.isShowing()) {
                    DBDataset dataset = getDataset();
                    DatabaseFileSystem.getInstance().openEditor(dataset, EditorProviderId.DATA, true);
                }
                if (error != null) {
                    DatasetEditorErrorForm errorForm = new DatasetEditorErrorForm(DatasetEditorModelCell.this);
                    errorForm.show();
                }
            }
        });
    }

    private void clearError() {
        if (error != null ) {
            error.markDirty();
            error = null;
        }
    }

    public DatasetEditorError getError() {
        return error;
    }

    @Override
    public void dispose() {
        super.dispose();
        originalUserValue = null;
        error = null;
    }

    public void revertChanges() {
        if (isModified) {
            updateUserValue(originalUserValue, false);
            this.isModified = false;
        }
    }
}
