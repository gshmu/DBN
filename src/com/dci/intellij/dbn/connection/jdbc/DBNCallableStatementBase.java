package com.dci.intellij.dbn.connection.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

abstract class DBNCallableStatementBase extends DBNPreparedStatement<CallableStatement> implements CallableStatement{
    public DBNCallableStatementBase(CallableStatement inner, DBNConnection connection) {
        super(inner, connection);
    }

    public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException {
        inner.registerOutParameter(parameterIndex, sqlType);
    }

    public void registerOutParameter(int parameterIndex, int sqlType, int scale) throws SQLException {
        inner.registerOutParameter(parameterIndex, sqlType, scale);
    }

    public boolean wasNull() throws SQLException {
        return inner.wasNull();
    }

    public String getString(int parameterIndex) throws SQLException {
        return inner.getString(parameterIndex);
    }

    public boolean getBoolean(int parameterIndex) throws SQLException {
        return inner.getBoolean(parameterIndex);
    }

    public byte getByte(int parameterIndex) throws SQLException {
        return inner.getByte(parameterIndex);
    }

    public short getShort(int parameterIndex) throws SQLException {
        return inner.getShort(parameterIndex);
    }

    public int getInt(int parameterIndex) throws SQLException {
        return inner.getInt(parameterIndex);
    }

    public long getLong(int parameterIndex) throws SQLException {
        return inner.getLong(parameterIndex);
    }

    public float getFloat(int parameterIndex) throws SQLException {
        return inner.getFloat(parameterIndex);
    }

    public double getDouble(int parameterIndex) throws SQLException {
        return inner.getDouble(parameterIndex);
    }

    @Deprecated
    public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException {
        return inner.getBigDecimal(parameterIndex, scale);
    }

    public byte[] getBytes(int parameterIndex) throws SQLException {
        return inner.getBytes(parameterIndex);
    }

    public Date getDate(int parameterIndex) throws SQLException {
        return inner.getDate(parameterIndex);
    }

    public Time getTime(int parameterIndex) throws SQLException {
        return inner.getTime(parameterIndex);
    }

    public Timestamp getTimestamp(int parameterIndex) throws SQLException {
        return inner.getTimestamp(parameterIndex);
    }

    public Object getObject(int parameterIndex) throws SQLException {
        return inner.getObject(parameterIndex);
    }

    public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
        return inner.getBigDecimal(parameterIndex);
    }

    public Object getObject(int parameterIndex, Map<String, Class<?>> map) throws SQLException {
        return inner.getObject(parameterIndex, map);
    }

    public Ref getRef(int parameterIndex) throws SQLException {
        return inner.getRef(parameterIndex);
    }

    public Blob getBlob(int parameterIndex) throws SQLException {
        return inner.getBlob(parameterIndex);
    }

    public Clob getClob(int parameterIndex) throws SQLException {
        return inner.getClob(parameterIndex);
    }

    public Array getArray(int parameterIndex) throws SQLException {
        return inner.getArray(parameterIndex);
    }

    public Date getDate(int parameterIndex, Calendar cal) throws SQLException {
        return inner.getDate(parameterIndex, cal);
    }

    public Time getTime(int parameterIndex, Calendar cal) throws SQLException {
        return inner.getTime(parameterIndex, cal);
    }

    public Timestamp getTimestamp(int parameterIndex, Calendar cal) throws SQLException {
        return inner.getTimestamp(parameterIndex, cal);
    }

    public void registerOutParameter(int parameterIndex, int sqlType, String typeName) throws SQLException {
        inner.registerOutParameter(parameterIndex, sqlType, typeName);
    }

    public void registerOutParameter(String parameterName, int sqlType) throws SQLException {
        inner.registerOutParameter(parameterName, sqlType);
    }

    public void registerOutParameter(String parameterName, int sqlType, int scale) throws SQLException {
        inner.registerOutParameter(parameterName, sqlType, scale);
    }

    public void registerOutParameter(String parameterName, int sqlType, String typeName) throws SQLException {
        inner.registerOutParameter(parameterName, sqlType, typeName);
    }

    public URL getURL(int parameterIndex) throws SQLException {
        return inner.getURL(parameterIndex);
    }

    public void setURL(String parameterName, URL val) throws SQLException {
        inner.setURL(parameterName, val);
    }

    public void setNull(String parameterName, int sqlType) throws SQLException {
        inner.setNull(parameterName, sqlType);
    }

    public void setBoolean(String parameterName, boolean x) throws SQLException {
        inner.setBoolean(parameterName, x);
    }

    public void setByte(String parameterName, byte x) throws SQLException {
        inner.setByte(parameterName, x);
    }

    public void setShort(String parameterName, short x) throws SQLException {
        inner.setShort(parameterName, x);
    }

    public void setInt(String parameterName, int x) throws SQLException {
        inner.setInt(parameterName, x);
    }

    public void setLong(String parameterName, long x) throws SQLException {
        inner.setLong(parameterName, x);
    }

    public void setFloat(String parameterName, float x) throws SQLException {
        inner.setFloat(parameterName, x);
    }

    public void setDouble(String parameterName, double x) throws SQLException {
        inner.setDouble(parameterName, x);
    }

    public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException {
        inner.setBigDecimal(parameterName, x);
    }

    public void setString(String parameterName, String x) throws SQLException {
        inner.setString(parameterName, x);
    }

    public void setBytes(String parameterName, byte[] x) throws SQLException {
        inner.setBytes(parameterName, x);
    }

    public void setDate(String parameterName, Date x) throws SQLException {
        inner.setDate(parameterName, x);
    }

    public void setTime(String parameterName, Time x) throws SQLException {
        inner.setTime(parameterName, x);
    }

    public void setTimestamp(String parameterName, Timestamp x) throws SQLException {
        inner.setTimestamp(parameterName, x);
    }

    public void setAsciiStream(String parameterName, InputStream x, int length) throws SQLException {
        inner.setAsciiStream(parameterName, x, length);
    }

    public void setBinaryStream(String parameterName, InputStream x, int length) throws SQLException {
        inner.setBinaryStream(parameterName, x, length);
    }

    public void setObject(String parameterName, Object x, int targetSqlType, int scale) throws SQLException {
        inner.setObject(parameterName, x, targetSqlType, scale);
    }

    public void setObject(String parameterName, Object x, int targetSqlType) throws SQLException {
        inner.setObject(parameterName, x, targetSqlType);
    }

    public void setObject(String parameterName, Object x) throws SQLException {
        inner.setObject(parameterName, x);
    }

    public void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException {
        inner.setCharacterStream(parameterName, reader, length);
    }

    public void setDate(String parameterName, Date x, Calendar cal) throws SQLException {
        inner.setDate(parameterName, x, cal);
    }

    public void setTime(String parameterName, Time x, Calendar cal) throws SQLException {
        inner.setTime(parameterName, x, cal);
    }

    public void setTimestamp(String parameterName, Timestamp x, Calendar cal) throws SQLException {
        inner.setTimestamp(parameterName, x, cal);
    }

    public void setNull(String parameterName, int sqlType, String typeName) throws SQLException {
        inner.setNull(parameterName, sqlType, typeName);
    }

    public String getString(String parameterName) throws SQLException {
        return inner.getString(parameterName);
    }

    public boolean getBoolean(String parameterName) throws SQLException {
        return inner.getBoolean(parameterName);
    }

    public byte getByte(String parameterName) throws SQLException {
        return inner.getByte(parameterName);
    }

    public short getShort(String parameterName) throws SQLException {
        return inner.getShort(parameterName);
    }

    public int getInt(String parameterName) throws SQLException {
        return inner.getInt(parameterName);
    }

    public long getLong(String parameterName) throws SQLException {
        return inner.getLong(parameterName);
    }

    public float getFloat(String parameterName) throws SQLException {
        return inner.getFloat(parameterName);
    }

    public double getDouble(String parameterName) throws SQLException {
        return inner.getDouble(parameterName);
    }

    public byte[] getBytes(String parameterName) throws SQLException {
        return inner.getBytes(parameterName);
    }

    public Date getDate(String parameterName) throws SQLException {
        return inner.getDate(parameterName);
    }

    public Time getTime(String parameterName) throws SQLException {
        return inner.getTime(parameterName);
    }

    public Timestamp getTimestamp(String parameterName) throws SQLException {
        return inner.getTimestamp(parameterName);
    }

    public Object getObject(String parameterName) throws SQLException {
        return inner.getObject(parameterName);
    }

    public BigDecimal getBigDecimal(String parameterName) throws SQLException {
        return inner.getBigDecimal(parameterName);
    }

    public Object getObject(String parameterName, Map<String, Class<?>> map) throws SQLException {
        return inner.getObject(parameterName, map);
    }

    public Ref getRef(String parameterName) throws SQLException {
        return inner.getRef(parameterName);
    }

    public Blob getBlob(String parameterName) throws SQLException {
        return inner.getBlob(parameterName);
    }

    public Clob getClob(String parameterName) throws SQLException {
        return inner.getClob(parameterName);
    }

    public Array getArray(String parameterName) throws SQLException {
        return inner.getArray(parameterName);
    }

    public Date getDate(String parameterName, Calendar cal) throws SQLException {
        return inner.getDate(parameterName, cal);
    }

    public Time getTime(String parameterName, Calendar cal) throws SQLException {
        return inner.getTime(parameterName, cal);
    }

    public Timestamp getTimestamp(String parameterName, Calendar cal) throws SQLException {
        return inner.getTimestamp(parameterName, cal);
    }

    public URL getURL(String parameterName) throws SQLException {
        return inner.getURL(parameterName);
    }

    public RowId getRowId(int parameterIndex) throws SQLException {
        return inner.getRowId(parameterIndex);
    }

    public RowId getRowId(String parameterName) throws SQLException {
        return inner.getRowId(parameterName);
    }

    public void setRowId(String parameterName, RowId x) throws SQLException {
        inner.setRowId(parameterName, x);
    }

    public void setNString(String parameterName, String value) throws SQLException {
        inner.setNString(parameterName, value);
    }

    public void setNCharacterStream(String parameterName, Reader value, long length) throws SQLException {
        inner.setNCharacterStream(parameterName, value, length);
    }

    public void setNClob(String parameterName, NClob value) throws SQLException {
        inner.setNClob(parameterName, value);
    }

    public void setClob(String parameterName, Reader reader, long length) throws SQLException {
        inner.setClob(parameterName, reader, length);
    }

    public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException {
        inner.setBlob(parameterName, inputStream, length);
    }

    public void setNClob(String parameterName, Reader reader, long length) throws SQLException {
        inner.setNClob(parameterName, reader, length);
    }

    public NClob getNClob(int parameterIndex) throws SQLException {
        return inner.getNClob(parameterIndex);
    }

    public NClob getNClob(String parameterName) throws SQLException {
        return inner.getNClob(parameterName);
    }

    public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException {
        inner.setSQLXML(parameterName, xmlObject);
    }

    public SQLXML getSQLXML(int parameterIndex) throws SQLException {
        return inner.getSQLXML(parameterIndex);
    }

    public SQLXML getSQLXML(String parameterName) throws SQLException {
        return inner.getSQLXML(parameterName);
    }

    public String getNString(int parameterIndex) throws SQLException {
        return inner.getNString(parameterIndex);
    }

    public String getNString(String parameterName) throws SQLException {
        return inner.getNString(parameterName);
    }

    public Reader getNCharacterStream(int parameterIndex) throws SQLException {
        return inner.getNCharacterStream(parameterIndex);
    }

    public Reader getNCharacterStream(String parameterName) throws SQLException {
        return inner.getNCharacterStream(parameterName);
    }

    public Reader getCharacterStream(int parameterIndex) throws SQLException {
        return inner.getCharacterStream(parameterIndex);
    }

    public Reader getCharacterStream(String parameterName) throws SQLException {
        return inner.getCharacterStream(parameterName);
    }

    public void setBlob(String parameterName, Blob x) throws SQLException {
        inner.setBlob(parameterName, x);
    }

    public void setClob(String parameterName, Clob x) throws SQLException {
        inner.setClob(parameterName, x);
    }

    public void setAsciiStream(String parameterName, InputStream x, long length) throws SQLException {
        inner.setAsciiStream(parameterName, x, length);
    }

    public void setBinaryStream(String parameterName, InputStream x, long length) throws SQLException {
        inner.setBinaryStream(parameterName, x, length);
    }

    public void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException {
        inner.setCharacterStream(parameterName, reader, length);
    }

    public void setAsciiStream(String parameterName, InputStream x) throws SQLException {
        inner.setAsciiStream(parameterName, x);
    }

    public void setBinaryStream(String parameterName, InputStream x) throws SQLException {
        inner.setBinaryStream(parameterName, x);
    }

    public void setCharacterStream(String parameterName, Reader reader) throws SQLException {
        inner.setCharacterStream(parameterName, reader);
    }

    public void setNCharacterStream(String parameterName, Reader value) throws SQLException {
        inner.setNCharacterStream(parameterName, value);
    }

    public void setClob(String parameterName, Reader reader) throws SQLException {
        inner.setClob(parameterName, reader);
    }

    public void setBlob(String parameterName, InputStream inputStream) throws SQLException {
        inner.setBlob(parameterName, inputStream);
    }

    public void setNClob(String parameterName, Reader reader) throws SQLException {
        inner.setNClob(parameterName, reader);
    }

    public <T> T getObject(int parameterIndex, Class<T> type) throws SQLException {
        return inner.getObject(parameterIndex, type);
    }

    public <T> T getObject(String parameterName, Class<T> type) throws SQLException {
        return inner.getObject(parameterName, type);
    }
}
