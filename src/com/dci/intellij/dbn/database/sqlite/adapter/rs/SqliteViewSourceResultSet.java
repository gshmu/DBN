package com.dci.intellij.dbn.database.sqlite.adapter.rs;

import com.dci.intellij.dbn.database.common.util.ResultSetAdapter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqliteViewSourceResultSet extends ResultSetAdapter {
    private static final Pattern DDL_STUB_REGEX = Pattern.compile("CREATE\\s+(TEMP(ORARY)?\\s+)?VIEW[^.]+\\s+AS\\s+(?=SELECT)", Pattern.CASE_INSENSITIVE);
    private ResultSet resultSet;

    public SqliteViewSourceResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        if (columnLabel.equals("SOURCE_CODE")) {
            String sourceCode = resultSet.getString("SOURCE_CODE");

            Matcher m = DDL_STUB_REGEX.matcher(sourceCode);
            if (m.find()) {
                int end = m.end();
                return sourceCode.substring(end).trim();
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
