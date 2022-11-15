package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.routine.ThrowableConsumer;
import com.dci.intellij.dbn.connection.util.Jdbc.Runnable;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.dci.intellij.dbn.common.util.Exceptions.toSqlException;

public class ResultSets extends StatefulDisposable.Base {
    public static void insertRow(ResultSet resultSet) throws SQLException {
        try {
            resultSet.insertRow();
        } catch (Throwable e) {
            throw toSqlException(e, "Error inserting row");
        }
    }

    public static void moveToInsertRow(ResultSet resultSet) throws SQLException {
        try {
            resultSet.moveToInsertRow();
        } catch (Throwable e) {
            throw toSqlException(e, "Error selecting insert row");
        }
    }
    public static void moveToCurrentRow(ResultSet resultSet) throws SQLException {
        try {
            resultSet.moveToCurrentRow();
        } catch (Throwable e) {
            throw toSqlException(e, "Error selecting current row");
        }
    }

    public static void deleteRow(final ResultSet resultSet) throws SQLException {
        try {
            resultSet.deleteRow();
        } catch (Throwable e) {
            throw toSqlException(e, "Error deleting row");
        }
    }


    public static void refreshRow(final ResultSet resultSet) throws SQLException {
        try {
            resultSet.refreshRow();
        } catch (Throwable e) {
            throw toSqlException(e, "Error refreshing row");
        }
    }

    public static void updateRow(final ResultSet resultSet) throws SQLException {
        try {
            resultSet.updateRow();
        } catch (Throwable e) {
            throw toSqlException(e, "Error updating row");
        }
    }

    public static void absolute(ResultSet resultSet, int row) throws SQLException {
        try {
            resultSet.absolute(row);
        } catch (Throwable e) {
            throw toSqlException(e, "Error selecting row");
        }
    }

    public static List<String> getColumnNames(ResultSet resultSet) throws SQLException {
        List<String> columnNames = new ArrayList<>();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int i=0; i<columnCount; i++) {
            columnNames.add(metaData.getColumnName(i+1).intern());
        }
        return columnNames;
    }

    public static void forEachRow(ResultSet resultSet, Runnable consumer) throws SQLException {
        try {
            if (resultSet != null && !Resources.isClosed(resultSet)) {
                while (resultSet.next()) {
                    consumer.run();
                }
            }
        } finally {
            Resources.close(resultSet);
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
