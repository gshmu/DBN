package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.routine.ThrowableConsumer;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResultSetUtil extends StatefulDisposable.Base {
    public static void insertRow(ResultSet resultSet) throws SQLException {
        try {
            resultSet.insertRow();
        } catch (Throwable e) {
            throw e instanceof SQLException ?
                    (SQLException) e :
                    new SQLException("Error inserting row: [" + e.getClass().getSimpleName() + "] " + e.getMessage());
        }
    }

    public static void moveToInsertRow(ResultSet resultSet) throws SQLException {
        try {
            resultSet.moveToInsertRow();
        } catch (Throwable e) {
            throw e instanceof SQLException ?
                    (SQLException) e :
                    new SQLException("Error selecting insert row: [" + e.getClass().getSimpleName() + "] " + e.getMessage());
        }
    }
    public static void moveToCurrentRow(ResultSet resultSet) throws SQLException {
        try {
            resultSet.moveToCurrentRow();
        } catch (Throwable e) {
            throw e instanceof SQLException ?
                    (SQLException) e :
                    new SQLException("Error selecting current row: [" + e.getClass().getSimpleName() + "] " + e.getMessage());
        }
    }

    public static void deleteRow(final ResultSet resultSet) throws SQLException {
        try {
            resultSet.deleteRow();
        } catch (Throwable e) {
            throw e instanceof SQLException ?
                    (SQLException) e :
                    new SQLException("Error deleting row: [" + e.getClass().getSimpleName() + "] " + e.getMessage());
        }
    }


    public static void refreshRow(final ResultSet resultSet) throws SQLException {
        try {
            resultSet.refreshRow();
        } catch (Throwable e) {
            throw e instanceof SQLException ?
                    (SQLException) e :
                    new SQLException("Error refreshing row: [" + e.getClass().getSimpleName() + "] " + e.getMessage());
        }
    }

    public static void updateRow(final ResultSet resultSet) throws SQLException {
        try {
            resultSet.updateRow();
        } catch (Throwable e) {
            throw e instanceof SQLException ?
                    (SQLException) e :
                    new SQLException("Error updating row: [" + e.getClass().getSimpleName() + "] " + e.getMessage());
        }
    }

    public static void absolute(ResultSet resultSet, int row) throws SQLException {
        try {
            resultSet.absolute(row);
        } catch (Throwable e) {
            throw e instanceof SQLException ?
                    (SQLException) e :
                    new SQLException("Error selecting row: [" + e.getClass().getSimpleName() + "] " + e.getMessage());
        }
    }

    public static List<String> getColumnNames(ResultSet resultSet) throws SQLException {
        List<String> columnNames = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int i=0; i<columnCount; i++) {
            columnNames.add(metaData.getColumnName(i+1));
        }
        return columnNames;
    }

    public static void forEachRow(ResultSet resultSet, ThrowableRunnable<SQLException> consumer) throws SQLException {
        try {
            if (resultSet != null && !ResourceUtil.isClosed(resultSet)) {
                while (resultSet.next()) {
                    consumer.run();
                }
            }
        } finally {
            ResourceUtil.close(resultSet);
        }
    }

    public static <T> void forEachRow(ResultSet resultSet, String columnName, Class<T> columnType, ThrowableConsumer<T, SQLException> consumer) throws SQLException {
        forEachRow(resultSet, () -> {
            Object object = null;
            if (CharSequence.class.isAssignableFrom(columnType)) {
                object = resultSet.getString(columnName);

            } else if (Integer.class.isAssignableFrom(columnType)) {
                object = resultSet.getInt(columnName);

            } else {
                throw new UnsupportedOperationException("Lookup not implemented. Add more handlers here");
            }

            consumer.accept((T) object);
        });
    }

    @Override
    protected void disposeInner() {
        nullify();
    }
}
