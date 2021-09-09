package com.dci.intellij.dbn.data.value;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.data.type.GenericDataType;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import javax.sql.rowset.serial.SerialClob;
import java.io.IOException;
import java.io.Reader;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;

@Slf4j
public class ClobValue extends LargeObjectValue {
    private Clob clob;
    private Reader reader;

    public ClobValue() {
    }

    private Clob createSerialClob(String charset) throws SQLException {
        if (StringUtil.isNotEmpty(charset)) {
            return new SerialClob(charset.toCharArray());
        }
        return null;
    }

    public ClobValue(CallableStatement callableStatement, int parameterIndex) throws SQLException {
        try {
            clob = callableStatement.getClob(parameterIndex);
        } catch (SQLFeatureNotSupportedException e) {
            String charset = callableStatement.getString(parameterIndex);
            clob = createSerialClob(charset);
        }

    }

    public ClobValue(ResultSet resultSet, int columnIndex) throws SQLException {
        try {
            clob = resultSet.getClob(columnIndex);
        } catch (SQLFeatureNotSupportedException e) {
            String charset = resultSet.getString(columnIndex);
            clob = createSerialClob(charset);
        }
    }

    @Override
    public void write(Connection connection, PreparedStatement preparedStatement, int parameterIndex, @Nullable String value) throws SQLException {
        try {
            if (value == null) {
                preparedStatement.setClob(parameterIndex, (Clob) null);
            } else {
                clob = connection.createClob();
                clob.setString(1, value);
                preparedStatement.setClob(parameterIndex, clob);
            }
        } catch (SQLFeatureNotSupportedException e) {
            if (value == null) {
                preparedStatement.setString(parameterIndex, null);
            } else {
                clob = createSerialClob(value);
                preparedStatement.setString(parameterIndex, value);
            }
        }
    }

    @Override
    public void write(Connection connection, ResultSet resultSet, int columnIndex, @Nullable String value) throws SQLException {
        try {
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
        } catch (SQLFeatureNotSupportedException e) {
            if (StringUtil.isEmpty(value)) {
                clob = null;
            } else {
                clob = createSerialClob(value);
            }
            resultSet.updateString(columnIndex, value);

        }
  }

    @Override
    public GenericDataType getGenericDataType() {
        return GenericDataType.CLOB;
    }

    @Override
    @Nullable
    public String read() throws SQLException {
        return read(0);
    }

    @Nullable
    @Override
    public String export() throws SQLException {
        return read();
    }

    @Override
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

    @Override
    public void release(){
        if (reader != null) {
            try {
                reader.close();
                reader = null;
            } catch (IOException e) {
                log.error("Could not close CLOB input reader.", e);
            }
        }
    }

    @Override
    public long size() throws SQLException {
        return clob == null ? 0 : clob.length();
    }

    @Override
    public String getDisplayValue() {
        /*try {
            return "[CLOB] " + size() + "";
        } catch (SQLException e) {
            return "[CLOB]";
        }*/

        return "[CLOB]";
    }
}
