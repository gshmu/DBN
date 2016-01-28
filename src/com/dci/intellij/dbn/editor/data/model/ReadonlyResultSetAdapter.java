package com.dci.intellij.dbn.editor.data.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.transaction.ConnectionSavepointCall;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.data.value.ValueAdapter;

public class ReadonlyResultSetAdapter extends ResultSetAdapter {
    private ResultSet resultSet;

    public ReadonlyResultSetAdapter(ConnectionHandler connectionHandler) {
        super(connectionHandler);
    }

    @Override
    public void scroll(final int rowIndex) throws SQLException {
        if (!isInsertMode()) {
            if (isUseSavePoints()) {
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

    @Override
    public void updateRow() throws SQLException {
        if (!isInsertMode())  {
            if (isUseSavePoints()) {
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

    @Override
    public void refreshRow() throws SQLException {
        if (!isInsertMode())  {
            if (isUseSavePoints()) {
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

    @Override
    public void startInsertRow() throws SQLException {
        if (!isInsertMode())  {
            if (isUseSavePoints()) {
                new ConnectionSavepointCall(resultSet) {
                    @Override
                    public Object execute() throws SQLException {
                        resultSet.moveToInsertRow();
                        setInsertMode(true);
                        return null;
                    }
                }.start();
            } else {
                resultSet.moveToInsertRow();
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
                        resultSet.moveToCurrentRow();
                        setInsertMode(false);
                        return null;
                    }
                }.start();
            } else {
                resultSet.moveToCurrentRow();
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
                        resultSet.insertRow();
                        resultSet.moveToCurrentRow();
                        setInsertMode(false);
                        return null;
                    }
                }.start();
            } else {
                resultSet.insertRow();
                resultSet.moveToCurrentRow();
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
                        resultSet.deleteRow();
                        return null;
                    }
                }.start();
            } else {
                resultSet.deleteRow();
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
}
