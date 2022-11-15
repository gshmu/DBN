package com.dci.intellij.dbn.editor.data;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.Resources;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterface;
import com.dci.intellij.dbn.database.interfaces.DatabaseMetadataInterface;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBDataset;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatasetEditorUtils {
    public static List<String> loadDistinctColumnValues(@NotNull DBColumn column) {
        try {
            ConnectionHandler connection = column.getConnection();
            return DatabaseInterface.call(true, connection, conn -> {
                        List<String> list = new ArrayList<>();
                        ResultSet resultSet = null;
                        try {
                            DBDataset dataset = column.getDataset();
                            String schemaName = dataset.getSchema().getName();
                            String datasetName = dataset.getName();
                            String columnName = column.getName();

                            DatabaseMetadataInterface metadata = connection.getMetadataInterface();
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
                    });

        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
