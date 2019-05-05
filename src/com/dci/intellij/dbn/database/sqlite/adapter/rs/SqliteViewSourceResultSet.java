package com.dci.intellij.dbn.database.sqlite.adapter.rs;

import com.dci.intellij.dbn.database.common.util.WrappedResultSet;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqliteViewSourceResultSet extends WrappedResultSet {
    private static final Pattern DDL_STUB_REGEX = Pattern.compile("CREATE\\s+(TEMP(ORARY)?\\s+)?VIEW[^.]+\\s+AS\\s+(?=SELECT)", Pattern.CASE_INSENSITIVE);

    public SqliteViewSourceResultSet(@Nullable ResultSet resultSet) {
        super(resultSet);
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        if (columnLabel.equals("SOURCE_CODE")) {
            String sourceCode = inner.getString("SOURCE_CODE");

            Matcher m = DDL_STUB_REGEX.matcher(sourceCode);
            if (m.find()) {
                int end = m.end();
                return sourceCode.substring(end).trim();
            }

        }
        return inner.getString(columnLabel);
    }
}
