package com.dci.intellij.dbn.generator;

import java.util.Iterator;

import com.dci.intellij.dbn.common.message.MessageBundle;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBTable;

public class InsertStatementGenerator extends StatementGenerator {
    private DBTable table;

    public InsertStatementGenerator(DBTable table) {
        this.table = table;
    }

    @Override
    public StatementGeneratorResult generateStatement() {
        StatementGeneratorResult result = new StatementGeneratorResult();
        MessageBundle messages = result.getMessages();

        StringBuilder statement = new StringBuilder();
        statement.append("insert into ");
        statement.append(table.getName());
        statement.append(" (\n");

        Iterator<DBColumn> columnIterator = table.getColumns().iterator();
        while (columnIterator.hasNext()) {
            DBColumn column = columnIterator.next();
            statement.append("    ");
            statement.append(column.getName());
            if (columnIterator.hasNext()) {
                statement.append(",\n");
            } else {
                statement.append(")\n");
            }
        }
        statement.append("values (\n");

        columnIterator = table.getColumns().iterator();
        while (columnIterator.hasNext()) {
            DBColumn column = columnIterator.next();
            statement.append("    :");
            statement.append(column.getName().toLowerCase());
            if (columnIterator.hasNext()) {
                statement.append(",\n");
            } else {
                statement.append(")\n");
            }
        }
        statement.append(";");

        result.setStatement(statement.toString());
        return result;
    }
}
