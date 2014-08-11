package com.dci.intellij.dbn.data.value;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.dci.intellij.dbn.common.util.CommonUtil;

public class ClobValue implements LargeObjectValue {
    private Clob clob;

    public ClobValue(Clob clob) {
        this.clob = clob;
    }

    public void write(Connection connection, ResultSet resultSet, int columnIndex, String value) throws SQLException {
        if (clob == null) {
            value = CommonUtil.nvl(value, "");
            resultSet.updateClob(columnIndex, new StringReader(value));
            //resultSet.updateCharacterStream(columnIndex, new StringReader(value));
            clob = resultSet.getClob(columnIndex);
        } else {
            if (clob.length() > value.length()) {
                clob.truncate(value.length());
            }

            clob.setString(1, value);
            resultSet.updateClob(columnIndex, clob);
            //resultSet.updateCharacterStream(columnIndex, new StringReader(value));
        }
    }

    public String read() throws SQLException {
        return read(0);
    }

    public String read(int maxSize) throws SQLException {
        if (clob == null) {
            return null;
        } else {
            try {
                int size = (int) (maxSize == 0 ? clob.length() : Math.min(maxSize, clob.length()));
                char[] buffer = new char[size];
                clob.getCharacterStream().read(buffer, 0, size);
                return new String(buffer);
            } catch (IOException e) {
                throw new SQLException("Could not read value from CLOB.");
            }
        }
    }

    @Override
    public long size() throws SQLException {
        return clob == null ? 0 : clob.length();
    }

    public String getDisplayValue() {
        /*try {
            return "[CLOB] " + size() + "";
        } catch (SQLException e) {
            return "[CLOB]";
        }*/

        return "[CLOB]";
    }
}
