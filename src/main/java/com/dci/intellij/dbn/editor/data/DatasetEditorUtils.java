package com.dci.intellij.dbn.editor.data;

import com.dci.intellij.dbn.connection.Resources;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterfaceInvoker;
import com.dci.intellij.dbn.database.interfaces.DatabaseMetadataInterface;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBDataset;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.dci.intellij.dbn.common.Priority.HIGH;

public class DatasetEditorUtils {
    public static List<String> loadDistinctColumnValues(@NotNull DBColumn column) {
        try {
            return DatabaseInterfaceInvoker.load(HIGH,
                    "Loading data",
                    "Loading possible values for " + column.getQualifiedNameWithType(),
                    column.getProject(),
                    column.getConnectionId(),
                    conn -> loadDistinctColumnValues(column, conn));
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @NotNull
    private static List<String> loadDistinctColumnValues(DBColumn column, DBNConnection conn) throws SQLException {
        List<String> list = new ArrayList<>();
        ResultSet resultSet = null;
        try {

            DBDataset dataset = column.getDataset();
            String schemaName = dataset.getSchemaName();
            String datasetName = dataset.getName();
            String columnName = column.getName();

            DatabaseMetadataInterface metadata = column.getMetadataInterface();
            resultSet = metadata.getDistinctValues(
                    schemaName,
                    datasetName,
                    columnName,
                    conn);

            while (resultSet.next()) {
                String value = resultSet.getString(1);
                list.add(value);
            }
        } finally {
            Resources.close(resultSet);
        }
        return list;
    }
}
