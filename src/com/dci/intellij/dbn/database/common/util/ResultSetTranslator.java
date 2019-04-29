package com.dci.intellij.dbn.database.common.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ResultSetTranslator<R> {
    R read(ResultSet resultSet, List<String> columnNames) throws SQLException;

    Object value(R row, String columnName);

    ResultSetTranslator<Map<String, Object>> BASIC = new ResultSetTranslator<Map<String, Object>>() {
        @Override
        public Map<String, Object> read(ResultSet resultSet, List<String> columnNames) throws SQLException {
            Map<String, Object> columns = new HashMap<>();
            for (String columnName : columnNames) {
                Object columnValue = resultSet.getObject(columnName);
                columns.put(columnName, columnValue);
            }
            return columns;
        }

        @Override
        public Object value(Map<String, Object> row, String columnName) {
            return row.get(columnName);
        }
    };
}
