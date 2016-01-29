package com.dci.intellij.dbn.database.sqlite.rs;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dci.intellij.dbn.database.common.util.ResultSetAdapter;
import com.dci.intellij.dbn.editor.code.content.GuardedBlockMarker;

public class SqliteTriggerSourceResultSet extends ResultSetAdapter {
    public static final Pattern DDL_STUB_REGEX = Pattern.compile("(?i)(CREATE\\s+(TEMP(ORARY)?\\s+)?)");
    public static final Pattern GUARDED_STUB_REGEX = Pattern.compile("(?i)TRIGGER\\s+[^.]+(?=\\s+(BEFORE|AFTER|INSTEAD))");
    private ResultSet resultSet;

    public SqliteTriggerSourceResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        if (columnLabel.equals("SOURCE_CODE")) {
            String sourceCode = resultSet.getString("SOURCE_CODE");

            Matcher m = DDL_STUB_REGEX.matcher(sourceCode);
            if (m.find()) {
                int end = m.end();
                sourceCode = sourceCode.substring(end).trim();
                m = GUARDED_STUB_REGEX.matcher(sourceCode);
                if (m.find()) {
                    sourceCode = sourceCode.substring(0, m.end()) + GuardedBlockMarker.END_OFFSET_IDENTIFIER + sourceCode.substring(m.end());
                }


                return sourceCode;
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
