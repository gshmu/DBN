package com.dci.intellij.dbn.data.value;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.data.type.GenericDataType;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class ClobValue extends LargeObjectValue {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    private Clob clob;
    private Reader reader;

    public ClobValue() {
    }

    public ClobValue(CallableStatement callableStatement, int parameterIndex) throws SQLException {
        clob = callableStatement.getClob(parameterIndex);
    }

    public ClobValue(ResultSet resultSet, int columnIndex) throws SQLException {
        this.clob = resultSet.getClob(columnIndex);
    }

    @Override
    public void write(Connection connection, PreparedStatement preparedStatement, int parameterIndex, @Nullable String value) throws SQLException {
        if (value == null) {
            preparedStatement.setClob(parameterIndex, (Clob) null);
        } else {
            Clob clob = connection.createClob();
            clob.setString(1, value);
            preparedStatement.setClob(parameterIndex, clob);
        }
    }

    public void write(Connection connection, ResultSet resultSet, int columnIndex, @Nullable String value) throws SQLException {
        int columnType = resultSet.getMetaData().getColumnType(columnIndex);
        if (StringUtil.isEmpty(value)) {
            clob = null;
        } else {
            clob = columnType == Types.NCLOB ?
                    connection.createNClob() :
                    connection.createClob();
            clob.setString(1, value);
        }

        if (columnType == Types.NCLOB)
            resultSet.updateNClob(columnIndex, (NClob) clob); else
            resultSet.updateClob(columnIndex, clob);
  }

    @Override
    public GenericDataType getGenericDataType() {
        return GenericDataType.ARRAY;
    }

    @Nullable
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

    public String getDisplayValue() {
        /*try {
            return "[CLOB] " + size() + "";
        } catch (SQLException e) {
            return "[CLOB]";
        }*/

        return "[CLOB]";
    }
}
