package com.dci.intellij.dbn.database.sqlite.rs;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.database.common.util.ResultSetAdapter;

public class SqliteTriggersResultSet extends ResultSetAdapter {
    private ResultSet resultSet;

    public SqliteTriggersResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        boolean isType = columnLabel.equals("TRIGGER_TYPE");
        boolean isEvent = columnLabel.equals("TRIGGERING_EVENT");
        if (isType || isEvent) {
            String sourceCode = resultSet.getString("SOURCE_CODE");

            Pattern p = Pattern.compile("(?i)(before|after|instead\\s+of)\\s+(delete|insert|update)");
            Matcher m = p.matcher(sourceCode);
            if (m.find()) {
                int start = m.start();
                int end = m.end();
                String definition = sourceCode.substring(start, end);
                if (isType) {
                    return
                        StringUtil.containsIgnoreCase(definition, "before") ? "BEFORE" :
                        StringUtil.containsIgnoreCase(definition, "after") ? "AFTER" :
                        StringUtil.containsIgnoreCase(definition, "instead") ? "INSTEAD OF" : "";
                }
                if (isEvent) {
                    return
                        StringUtil.containsIgnoreCase(definition, "delete") ? "DELETE" :
                        StringUtil.containsIgnoreCase(definition, "insert") ? "INSERT" :
                        StringUtil.containsIgnoreCase(definition, "update") ? "UPDATE" : "";

                }
            }
        }
        return resultSet.getString(columnLabel);
    }

    @Override
    public boolean next() throws SQLException {
        return resultSet.next();
    }

    @Override
    public void close() throws SQLException {
        resultSet.close();
    }
}
