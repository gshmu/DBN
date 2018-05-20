package com.dci.intellij.dbn.editor.data.model;

import com.dci.intellij.dbn.connection.transaction.ConnectionSavepointCall;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.data.value.ValueAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EditableResultSetAdapter extends ResultSetAdapter {
    private ResultSet resultSet;

    public EditableResultSetAdapter(DatasetEditorModel model, ResultSet resultSet) {
        super(model);
        this.resultSet = resultSet;
    }

    @Override
    public void scroll(final int rowIndex) throws SQLException {
        if (!isInsertMode()) {
            if (isUseSavePoints()) {
                new ConnectionSavepointCall(resultSet) {
                    @Override
                    public Object execute() throws SQLException {
                        absolute(resultSet, rowIndex);
                        return null;
                    }
                }.start();
            } else {
                absolute(resultSet, rowIndex);
            }
        }
    }

    @Override
    public void updateRow() throws SQLException {
        if (!isInsertMode())  {
            if (isUseSavePoints()) {
                new ConnectionSavepointCall(resultSet) {
                    @Override
                    public Object execute() throws SQLException {
                        updateRow(resultSet);
                        return null;
                    }
                }.start();
            } else {
                updateRow(resultSet);
            }
        }
    }

    @Override
    public void refreshRow() throws SQLException {
        if (!isInsertMode())  {
            if (isUseSavePoints()) {
                new ConnectionSavepointCall(resultSet) {
                    @Override
                    public Object execute() throws SQLException {
                        refreshRow(resultSet);
                        return null;
                    }
                }.start();
            } else {
                refreshRow(resultSet);
            }
        }
    }

    @Override
    public void startInsertRow() throws SQLException {
        if (!isInsertMode())  {
            if (isUseSavePoints()) {
                new ConnectionSavepointCall(resultSet) {
                    @Override
                    public Object execute() throws SQLException {
                        moveToInsertRow(resultSet);
                        setInsertMode(true);
                        return null;
                    }
                }.start();
            } else {
                moveToInsertRow(resultSet);
                setInsertMode(true);
            }
        }
    }

    @Override
    public void cancelInsertRow() throws SQLException {
        if (isInsertMode())  {
            if (isUseSavePoints()) {
                new ConnectionSavepointCall(resultSet) {
                    @Override
                    public Object execute() throws SQLException {
                        moveToCurrentRow(resultSet);
                        setInsertMode(false);
                        return null;
                    }
                }.start();
            } else {
                moveToCurrentRow(resultSet);
                setInsertMode(false);
            }
        }
    }

    @Override
    public void insertRow() throws SQLException {
        if (isInsertMode())  {
            if (isUseSavePoints()) {
                new ConnectionSavepointCall(resultSet) {
                    @Override
                    public Object execute() throws SQLException {
                        insertRow(resultSet);
                        moveToCurrentRow(resultSet);
                        setInsertMode(false);
                        return null;
                    }
                }.start();
            } else {
                insertRow(resultSet);
                moveToCurrentRow(resultSet);
                setInsertMode(false);
            }
        }
    }

    @Override
    public void deleteRow() throws SQLException {
        if (!isInsertMode())  {
            if (isUseSavePoints()) {
                new ConnectionSavepointCall(resultSet) {
                    @Override
                    public Object execute() throws SQLException {
                        deleteRow(resultSet);
                        return null;
                    }
                }.start();
            } else {
                deleteRow(resultSet);
            }
        }
    }

    @Override
    public void setValue(final int columnIndex, @NotNull final ValueAdapter valueAdapter, @Nullable final Object value) throws SQLException {
        final Connection connection = resultSet.getStatement().getConnection();
        if (isUseSavePoints()) {
            new ConnectionSavepointCall(resultSet) {
                @Override
                public Object execute() throws SQLException {
                    valueAdapter.write(connection, resultSet, columnIndex, value);
                    return null;
                }
            }.start();
        } else {
            valueAdapter.write(connection, resultSet, columnIndex, value);
        }
    }

    @Override
    public void setValue(final int columnIndex, @NotNull final DBDataType dataType, @Nullable final Object value) throws SQLException {
        if (isUseSavePoints()) {
            new ConnectionSavepointCall(resultSet) {
                @Override
                public Object execute() throws SQLException {
                    dataType.setValueToResultSet(resultSet, columnIndex, value);
                    return null;
                }
            }.start();
        } else {
            dataType.setValueToResultSet(resultSet, columnIndex, value);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        resultSet = null;
    }
}
