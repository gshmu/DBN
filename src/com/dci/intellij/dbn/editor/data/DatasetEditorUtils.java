package com.dci.intellij.dbn.editor.data;

import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.object.DBColumn;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DatasetEditorUtils {
    public static List<String> loadDistinctColumnValues(@NotNull DBColumn column) {
        List<String> list = new ArrayList<String>();
        ConnectionHandler connectionHandler = FailsafeUtil.get(column.getConnectionHandler());
        Connection connection = null;
        ResultSet resultSet = null;
        try {
            connection = connectionHandler.getPoolConnection(true);
            resultSet = connectionHandler.getInterfaceProvider().getMetadataInterface().getDistinctValues(
                    column.getDataset().getSchema().getName(),
                    column.getDataset().getName(),
                    column.getName(), connection);

            while (resultSet.next()) {
                String value = resultSet.getString(1);
                list.add(value);
            }
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            ConnectionUtil.closeResultSet(resultSet);
            connectionHandler.freePoolConnection(connection);
        }

        return list;
    }
}
