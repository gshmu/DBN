package com.dci.intellij.dbn.data.value;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.intellij.openapi.diagnostic.Logger;

public class ClobValue implements LargeObjectValue {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    private Clob clob;
    private Reader reader;

    public ClobValue() {
    }

    public ClobValue(ResultSet resultSet, int columnIndex) throws SQLException {
        this.clob = resultSet.getClob(columnIndex);
    }

    public void write(Connection connection, ResultSet resultSet, int columnIndex, String value) throws SQLException {
        int columnType = resultSet.getMetaData().getColumnType(columnIndex);

        if (clob == null) {
            value = CommonUtil.nvl(value, "");
            if (columnType == Types.NCLOB) {
                resultSet.updateNClob(columnIndex, new StringReader(""));
            } else {
                resultSet.updateClob(columnIndex, new StringReader(""));
            }

            //resultSet.updateCharacterStream(columnIndex, new StringReader(value));
            clob = resultSet.getClob(columnIndex);
        } else {
            if (clob.length() > value.length()) {
                clob.truncate(value.length());
            }
        }
        clob.setString(1, value);
        resultSet.updateClob(columnIndex, clob);

    }

    public String read() throws SQLException {
        return read(0);
    }

    public String read(int maxSize) throws SQLException {
        if (clob == null) {            return null;
        } else {
            long totalLength = clob.length();
            int size = (int) (maxSize == 0 ? totalLength : Math.min(maxSize, totalLength));
            try {
                char[] buffer = new char[size];
                reader = clob.getCharacterStream();
                reader.read(buffer, 0, size);
                return new String(buffer);
            } catch (IOException e) {
                throw new SQLException("Could not read value from CLOB.");
            } finally {
                if (totalLength <= size) {
                    release();
                }
            }
        }
    }

    public void release(){
        if (reader != null) {
            try {
                reader.close();
                reader = null;
            } catch (IOException e) {
                LOGGER.error("Could not close CLOB input reader.", e);
            }
        }
    }

    @Override
    public long size() throws SQLException {
        return clob == null ? 0 : clob.length();
    }

    @Override
    public String getContentTypeName() {
        return null;
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
