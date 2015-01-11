package com.dci.intellij.dbn.data.type;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentElement;
import com.dci.intellij.dbn.data.value.ArrayValue;
import com.dci.intellij.dbn.data.value.BlobValue;
import com.dci.intellij.dbn.data.value.ClobValue;
import com.dci.intellij.dbn.data.value.XmlTypeValue;
import com.intellij.openapi.diagnostic.Logger;
import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleResultSet;
import oracle.sql.OPAQUE;
import oracle.xdb.XMLType;

public class DBNativeDataType implements DynamicContentElement{
    private static final Logger LOGGER = LoggerFactory.createLogger();

    private DataTypeDefinition dataTypeDefinition;

    public DBNativeDataType(DataTypeDefinition dataTypeDefinition) {
        this.dataTypeDefinition = dataTypeDefinition;
    }

    public boolean isDisposed() {
        return false;
    }

    public String getName() {
        return dataTypeDefinition.getName();
    }

    public DataTypeDefinition getDataTypeDefinition() {
        return dataTypeDefinition;
    }
    
    public GenericDataType getGenericDataType() {
        return dataTypeDefinition.getGenericDataType();
    }

    public boolean isPseudoNative() {
        return dataTypeDefinition.isPseudoNative();
    }

    public boolean isLargeObject() {
        return getGenericDataType().isLOB();
    }

    public Object getValueFromResultSet(ResultSet resultSet, int columnIndex) throws SQLException {
        // FIXME: add support for stream updatable types
        GenericDataType genericDataType = dataTypeDefinition.getGenericDataType();
        if (genericDataType == GenericDataType.BLOB) return new BlobValue(resultSet, columnIndex);
        if (genericDataType == GenericDataType.CLOB) return new ClobValue(resultSet, columnIndex);
        if (genericDataType == GenericDataType.XMLTYPE) return new XmlTypeValue((OracleResultSet) resultSet, columnIndex);
        if (genericDataType == GenericDataType.ARRAY) return new ArrayValue(resultSet, columnIndex);
        if (genericDataType == GenericDataType.ROWID) return "[ROWID]";
        if (genericDataType == GenericDataType.FILE) return "[FILE]";

        Class clazz = dataTypeDefinition.getTypeClass();
        if (Number.class.isAssignableFrom(clazz) && resultSet.getString(columnIndex) == null) {
            // mysql converts null numbers to 0!!!
            // FIXME make this database dependent (e.g. in CompatibilityInterface).
            return null;
        }
        try {
            return
                    clazz == String.class ? resultSet.getString(columnIndex) :
                    clazz == Byte.class ? resultSet.getByte(columnIndex) :
                    clazz == Short.class ? resultSet.getShort(columnIndex) :
                    clazz == Integer.class ? resultSet.getInt(columnIndex) :
                    clazz == Long.class ? resultSet.getLong(columnIndex) :
                    clazz == Float.class ? resultSet.getFloat(columnIndex) :
                    clazz == Double.class ? resultSet.getDouble(columnIndex) :
                    clazz == BigDecimal.class ? resultSet.getBigDecimal(columnIndex) :
                    clazz == Date.class ? resultSet.getDate(columnIndex) :
                    clazz == Time.class ? resultSet.getTime(columnIndex) :
                    clazz == Timestamp.class ? resultSet.getTimestamp(columnIndex) :
                    //clazz == Array.class ? resultSet.getArray(columnIndex) :
                            resultSet.getObject(columnIndex);
        } catch (SQLException e) {
            Object object = resultSet.getObject(columnIndex);
            LOGGER.error("Error resolving result set value for '" + object + "'. (data type definition " + dataTypeDefinition + ")", e);
            return object;
        }
    }

    public void setValueToResultSet(ResultSet resultSet, int columnIndex, Object value) throws SQLException {
        // FIXME: add support for stream updatable types
        GenericDataType genericDataType = dataTypeDefinition.getGenericDataType();
        if (genericDataType == GenericDataType.BLOB) return;
        if (genericDataType == GenericDataType.CLOB) return;
        if (genericDataType == GenericDataType.XMLTYPE) return;
        if (genericDataType == GenericDataType.ROWID) return;
        if (genericDataType == GenericDataType.FILE) return;
        if (genericDataType == GenericDataType.ARRAY) return;

        if (value == null) {
            resultSet.updateObject(columnIndex, null);
        } else {
            Class clazz = dataTypeDefinition.getTypeClass();
            if (value.getClass().isAssignableFrom(clazz)) {
                if(clazz == String.class) resultSet.updateString(columnIndex, (String) value); else
                if(clazz == Byte.class) resultSet.updateByte(columnIndex, (Byte) value); else
                if(clazz == Short.class) resultSet.updateShort(columnIndex, (Short) value); else
                if(clazz == Integer.class) resultSet.updateInt(columnIndex, (Integer) value); else
                if(clazz == Long.class) resultSet.updateLong(columnIndex, (Long) value); else
                if(clazz == Float.class) resultSet.updateFloat(columnIndex, (Float) value); else
                if(clazz == Double.class) resultSet.updateDouble(columnIndex, (Double) value); else
                if(clazz == BigDecimal.class) resultSet.updateBigDecimal(columnIndex, (BigDecimal) value); else
                if(clazz == Date.class) resultSet.updateDate(columnIndex, (Date) value); else
                if(clazz == Time.class) resultSet.updateTime(columnIndex, (Time) value); else
                if(clazz == Timestamp.class) resultSet.updateTimestamp(columnIndex, (Timestamp) value); else
                //if(clazz == Array.class) resultSet.updateArray(columnIndex, (Array) value); else
                        resultSet.updateObject(columnIndex, value);
            } else {
                throw new SQLException("Can not convert \"" + value.toString() + "\" into " + dataTypeDefinition.getName());
            }
        }
    }

    public Object getValueFromStatement(CallableStatement callableStatement, int parameterIndex) throws SQLException {
        GenericDataType genericDataType = dataTypeDefinition.getGenericDataType();
        if (genericDataType == GenericDataType.XMLTYPE) {
            OracleCallableStatement oracleCallableStatement = (OracleCallableStatement) callableStatement;
            OPAQUE opaque = oracleCallableStatement.getOPAQUE(parameterIndex);
            XMLType xmlType = opaque == null ? null : XMLType.createXML(opaque);
            return xmlType == null ? null : xmlType.getStringVal();
        }
        if (genericDataType == GenericDataType.CLOB) {
            try {
                Clob clob = callableStatement.getClob(parameterIndex);
                Reader reader = clob.getCharacterStream();
                int size = (int) clob.length();
                char[] buffer = new char[size];
                reader.read(buffer, 0, size);
                return new String(buffer);
            } catch (IOException e) {
                throw new SQLException("Could not real CLOB value", e);
            }
        }
        if (genericDataType == GenericDataType.BLOB) {
            try {
                Blob blob = callableStatement.getBlob(parameterIndex);
                int size = (int) blob.length();
                byte[] buffer = new byte[size];
                InputStream inputStream = blob.getBinaryStream();
                inputStream.read(buffer, 0, size);
                return new String(buffer);
            } catch (IOException e) {
                throw new SQLException("Could not real BLOB value", e);
            }
        }

        return callableStatement.getObject(parameterIndex);
    }

    public void setValueToStatement(PreparedStatement preparedStatement, int parameterIndex, Object value) throws SQLException {
        GenericDataType genericDataType = dataTypeDefinition.getGenericDataType();
        if (genericDataType == GenericDataType.CURSOR) {
            // not supported
        }
        else if (genericDataType == GenericDataType.XMLTYPE) {
            XMLType xmlType = XMLType.createXML(preparedStatement.getConnection(), (String) value);
            preparedStatement.setObject(parameterIndex, xmlType);
        }
        else if (genericDataType == GenericDataType.CLOB) {
            if (value == null) {
                preparedStatement.setClob(parameterIndex, (Clob) null);
            } else {
                Clob clob = preparedStatement.getConnection().createClob();
                clob.setString(1, (String) value);
                preparedStatement.setClob(parameterIndex, clob);
            }
        }
        else if (genericDataType == GenericDataType.BLOB) {
            if (value == null) {
                preparedStatement.setBlob(parameterIndex, (Blob) null);
            } else {
                Blob blob = preparedStatement.getConnection().createBlob();
                blob.setBytes(1, ((String) value).getBytes());
                preparedStatement.setBlob(parameterIndex, blob);
            }
        }

        else if (value == null) {
            preparedStatement.setObject(parameterIndex, null);
        } else {
            Class clazz = dataTypeDefinition.getTypeClass();
            if (value.getClass().isAssignableFrom(clazz)) {
                if(clazz == String.class) preparedStatement.setString(parameterIndex, (String) value); else
                if(clazz == Byte.class) preparedStatement.setByte(parameterIndex, (Byte) value); else
                if(clazz == Short.class) preparedStatement.setShort(parameterIndex, (Short) value); else
                if(clazz == Integer.class) preparedStatement.setInt(parameterIndex, (Integer) value); else
                if(clazz == Long.class) preparedStatement.setLong(parameterIndex, (Long) value); else
                if(clazz == Float.class) preparedStatement.setFloat(parameterIndex, (Float) value); else
                if(clazz == Double.class) preparedStatement.setDouble(parameterIndex, (Double) value); else
                if(clazz == BigDecimal.class) preparedStatement.setBigDecimal(parameterIndex, (BigDecimal) value); else
                if(clazz == Date.class) preparedStatement.setDate(parameterIndex, (Date) value); else
                if(clazz == Time.class) preparedStatement.setTime(parameterIndex, (Time) value); else
                if(clazz == Timestamp.class) preparedStatement.setTimestamp(parameterIndex, (Timestamp) value); else
                if(clazz == Boolean.class) preparedStatement.setBoolean(parameterIndex, (Boolean) value); else
                        preparedStatement.setObject(parameterIndex, value);
            } else {
                throw new SQLException("Can not convert \"" + value.toString() + "\" into " + dataTypeDefinition.getName());
            }
        }
    }

    public int getSqlType(){
        return dataTypeDefinition.getSqlType();
    }


    public String toString() {
        return dataTypeDefinition.getName();
    }

    /*********************************************************
     *                 DynamicContentElement                 *
     *********************************************************/
    public boolean isValid() {
        return true;
    }

    public void setValid(boolean valid) {

    }

    public String getDescription() {
        return null;
    }

    public DynamicContent getOwnerContent() {
        return null;
    }

    public void setOwnerContent(DynamicContent ownerContent) {
    }

    public void reload() {
    }

    public void dispose() {

    }

    public int compareTo(@NotNull Object o) {
        DBNativeDataType remote = (DBNativeDataType) o;
        return getName().compareTo(remote.getName());
    }
}
