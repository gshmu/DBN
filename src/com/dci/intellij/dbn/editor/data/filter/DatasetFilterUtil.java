package com.dci.intellij.dbn.editor.data.filter;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.data.grid.options.DataGridSettings;
import com.dci.intellij.dbn.data.sorting.SortDirection;
import com.dci.intellij.dbn.data.sorting.SortingInstruction;
import com.dci.intellij.dbn.data.sorting.SortingState;
import com.dci.intellij.dbn.database.DatabaseCompatibility;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.JdbcProperty;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBDataset;

import java.util.List;

public class DatasetFilterUtil {

    public static void addOrderByClause(DBDataset dataset, StringBuilder buffer, SortingState sortingState) {
        DataGridSettings dataGridSettings = DataGridSettings.getInstance(dataset.getProject());
        boolean nullsFirst = dataGridSettings.getSortingSettings().isNullsFirst();
        List<SortingInstruction> sortingInstructions = sortingState.getInstructions();
        if (sortingInstructions.size() > 0) {
            buffer.append(" order by ");
            boolean instructionAdded = false;
            for (SortingInstruction sortingInstruction : sortingInstructions) {
                SortDirection sortDirection = sortingInstruction.getDirection();
                DBColumn column = dataset.getColumn(sortingInstruction.getColumnName());
                if (Failsafe.check(column) && !sortDirection.isIndefinite()) {
                    DatabaseCompatibilityInterface compatibilityInterface = DatabaseCompatibilityInterface.getInstance(column);
                    String orderByClause = compatibilityInterface.getOrderByClause(column.getQuotedName(false), sortDirection, nullsFirst);
                    buffer.append(instructionAdded ? ", " : "");
                    buffer.append(orderByClause);
                    instructionAdded = true;
                }
            }
        }
    }

    public static void createSelectStatement(DBDataset dataset, StringBuilder buffer) {
        buffer.append("select ");
        int index = 0;
        for (DBColumn column : dataset.getColumns()) {
            if (index > 0) {
                buffer.append(", ");
            }
            buffer.append(column.getQuotedName(false));
            index++;
        }
        buffer.append(" from ");
        buffer.append(dataset.getSchema().getQuotedName(true));
        buffer.append(".");
        buffer.append(dataset.getQuotedName(true));

    }

    public static void createSimpleSelectStatement(DBDataset dataset, StringBuilder buffer) {
        DatabaseCompatibility compatibility = dataset.getConnectionHandler().getCompatibility();
        // TODO not implemented yet - returning always true at the moment
        boolean aliased = compatibility.isSupported(JdbcProperty.SQL_DATASET_ALIASING);

        String schemaName = dataset.getSchema().getQuotedName(true);
        String datasetName = dataset.getQuotedName(true);

        if (aliased) {
            // IMPORTANT oracle jdbc seems to create readonly result-set if dataset is not aliased
            buffer.append("select a.* from ");
            buffer.append(schemaName);
            buffer.append(".");
            buffer.append(datasetName);
            buffer.append(" a");
        } else {
            buffer.append("select * from ");
            buffer.append(schemaName);
            buffer.append(".");
            buffer.append(datasetName);
        }
    }
}
