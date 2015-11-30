package com.dci.intellij.dbn.editor.data.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.connection.transaction.ConnectionSavepointCall;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.data.value.ValueAdapter;

public class EditableResultSetHandler {
    private ResultSet resultSet;
    private boolean useSavepoints;
    private boolean insertMode;

    public EditableResultSetHandler(ResultSet resultSet, boolean useSavepoints) {
        this.resultSet = resultSet;
        this.useSavepoints = useSavepoints;
    }

    public void scroll(final int rowIndex) throws SQLException {
        if (!insertMode) {
            if (useSavepoints) {
                new ConnectionSavepointCall(resultSet) {
                    @Override
                    public Object execute() throws SQLException {
                        resultSet.absolute(rowIndex);
                        return null;
                    }
                }.start();
            } else {
                resultSet.absolute(rowIndex);
            }
        }
    }

    public void updateRow() throws SQLException {
        if (!insertMode)  {
            if (useSavepoints) {
                new ConnectionSavepointCall(resultSet) {
                    @Override
                    public Object execute() throws SQLException {
                        resultSet.updateRow();
                        return null;
                    }
                }.start();
            } else {
                resultSet.updateRow();
            }
        }
    }

    public void refreshRow() throws SQLException {
        if (!insertMode)  {
            if (useSavepoints) {
                new ConnectionSavepointCall(resultSet) {
                    @Override
                    public Object execute() throws SQLException {
                        resultSet.refreshRow();
                        return null;
                    }
                }.start();
            } else {
                resultSet.refreshRow();
            }
        }
    }

    public void startInsertRow() throws SQLException {
        if (!insertMode)  {
            if (useSavepoints) {
                new ConnectionSavepointCall(resultSet) {
                    @Override
                    public Object execute() throws SQLException {
                        resultSet.moveToInsertRow();
                        insertMode = true;
                        return null;
                    }
                }.start();
            } else {
                resultSet.moveToInsertRow();
                insertMode = true;
            }
        }
    }

    public void cancelInsertRow() throws SQLException {
        if (!insertMode)  {
            if (useSavepoints) {
                new ConnectionSavepointCall(resultSet) {
                    @Override
                    public Object execute() throws SQLException {
                        resultSet.moveToCurrentRow();
                        insertMode = false;
                        return null;
                    }
                }.start();
            } else {
                resultSet.moveToCurrentRow();
                insertMode = false;
            }
        }
    }

    public void insertRow() throws SQLException {
        if (insertMode)  {
            if (useSavepoints) {
                new ConnectionSavepointCall(resultSet) {
                    @Override
                    public Object execute() throws SQLException {
                        resultSet.insertRow();
                        resultSet.moveToCurrentRow();
                        insertMode = false;
                        return null;
                    }
                }.start();
            } else {
                resultSet.insertRow();
                resultSet.moveToCurrentRow();
                insertMode = false;
            }
        }
    }

    public void deleteRow() throws SQLException {
        if (!insertMode)  {
            if (useSavepoints) {
                new ConnectionSavepointCall(resultSet) {
                    @Override
                    public Object execute() throws SQLException {
                        resultSet.deleteRow();
                        return null;
                    }
                }.start();
            } else {
                resultSet.deleteRow();
            }
        }
    }

    public void setValue(final int columnIndex, @NotNull final ValueAdapter valueAdapter, @Nullable final Object value) throws SQLException {
        final Connection connection = resultSet.getStatement().getConnection();
        if (useSavepoints) {
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

    public void setValue(final int columnIndex, @NotNull final DBDataType dataType, @Nullable final Object value) throws SQLException {
        if (useSavepoints) {
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
}
