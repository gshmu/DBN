package com.dci.intellij.dbn.data.type;

import com.dci.intellij.dbn.common.content.DynamicContentElement;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.connection.jdbc.DBNCallableStatement;
import com.dci.intellij.dbn.data.value.ValueAdapter;
import com.dci.intellij.dbn.database.common.util.DataTypeParseAdapter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.sql.*;

@Slf4j
@Getter
public class DBNativeDataType extends StatefulDisposable.Base implements DynamicContentElement{
    private final DataTypeDefinition definition;

    public DBNativeDataType(DataTypeDefinition definition) {
        this.definition = definition;
    }

    @NotNull
    @Override
    public String getName() {
        return definition.getName();
    }

    @Override
    public short getOverload() {
        return 0;
    }

    public GenericDataType getGenericDataType() {
        return definition.getGenericDataType();
    }

    public boolean isPseudoNative() {
        return definition.isPseudoNative();
    }

    public boolean isLargeObject() {
        return getGenericDataType().isLOB();
    }

    public Object getValueFromResultSet(ResultSet resultSet, int columnIndex) {
        // FIXME: add support for stream updatable types

        GenericDataType genericDataType = definition.getGenericDataType();
        if (ValueAdapter.supports(genericDataType)) {
            try {
                return ValueAdapter.create(genericDataType, resultSet, columnIndex);
            } catch (Throwable e) {
                return null;
            }

        }

/*
        if (genericDataType == GenericDataType.BLOB) return new BlobValue(resultSet, columnIndex);
        if (genericDataType == GenericDataType.CLOB) return new ClobValue(resultSet, columnIndex);
        if (genericDataType == GenericDataType.XMLTYPE) return new XmlTypeValue((OracleResultSet) resultSet, columnIndex);
        if (genericDataType == GenericDataType.ARRAY) return new ArrayValue(resultSet, columnIndex);
*/
        if (genericDataType == GenericDataType.ROWID) return "[ROWID]";
        if (genericDataType == GenericDataType.FILE) return "[FILE]";

        Class<?> clazz = definition.getTypeClass();
        try {
            if (Number.class.isAssignableFrom(clazz) && resultSet.getString(columnIndex) == null) {
                // mysql converts null numbers to 0!!!
                // FIXME make this database dependent (e.g. in CompatibilityInterface).
                return null;
            }

            DataTypeParseAdapter parseAdapter = definition.getParseAdapter();
            if (parseAdapter != null) {
                return parseAdapter.parse(resultSet.getString(columnIndex));
            }

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
                    clazz == Boolean.class ? resultSet.getBoolean(columnIndex) :
                    //clazz == Array.class ? resultSet.getArray(columnIndex) :
                            resultSet.getObject(columnIndex);
        } catch (Throwable e) {
            try {
                Object object = resultSet.getObject(columnIndex);
                String objectClass = object == null ? "" : object.getClass().getName();
                // TODO odd values resolvers
                if (object instanceof String && Strings.isEmpty((String) object)) {
                    return null;
                } else if (object instanceof Long && Integer.class.isAssignableFrom(clazz)) {
                    // odd jdbc implementations allowing long for data type int java.sql.Types.INTEGER
                    return object;
                } else if (object instanceof Long && java.util.Date.class.isAssignableFrom(clazz)) {
                    // fallback for dates stored as milliseconds (sqlite?)
                    long longValue = (long) object;
                    return
                        clazz == Date.class ? new Date(longValue) :
                        clazz == Time.class ? new Time(longValue) :
                        clazz == Timestamp.class ? new Timestamp(longValue) : null;
                } else {
                    log.error("Error resolving result-set value for " + objectClass + " \"" + object + "\". (data type definition " + definition + ')', e);
                    return object;
                }
            } catch (Throwable e1) {
                return null;
            }
        }
    }

    public void setValueToResultSet(ResultSet resultSet, int columnIndex, Object value) throws SQLException {
        // FIXME: add support for stream updatable types
        GenericDataType genericDataType = definition.getGenericDataType();
        if (genericDataType == GenericDataType.BLOB) return;
        if (genericDataType == GenericDataType.CLOB) return;
        if (genericDataType == GenericDataType.XMLTYPE) return;
        if (genericDataType == GenericDataType.ROWID) return;
        if (genericDataType == GenericDataType.FILE) return;
        if (genericDataType == GenericDataType.ARRAY) return;

        if (value == null) {
            resultSet.updateObject(columnIndex, null);
        } else {
            Class clazz = definition.getTypeClass();
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
                if(clazz == Boolean.class) resultSet.updateBoolean(columnIndex, (Boolean) value); else
                //if(clazz == Array.class) resultSet.updateArray(columnIndex, (Array) value); else
                        resultSet.updateObject(columnIndex, value);
            } else {
                throw new SQLException("Can not convert \"" + value + "\" into " + definition.getName());
            }
        }
    }

    public Object getValueFromStatement(DBNCallableStatement callableStatement, int parameterIndex) throws SQLException {
        GenericDataType genericDataType = definition.getGenericDataType();
        if (ValueAdapter.supports(genericDataType)) {
            return ValueAdapter.create(genericDataType, callableStatement, parameterIndex);
        }
/*
        if (genericDataType == GenericDataType.BLOB) return new BlobValue(callableStatement, parameterIndex);
        if (genericDataType == GenericDataType.CLOB) return new ClobValue(callableStatement, parameterIndex);
        if (genericDataType == GenericDataType.XMLTYPE) return new XmlTypeValue((OracleCallableStatement) callableStatement, parameterIndex);
*/

        return callableStatement.getObject(parameterIndex);
    }

    public <T> void setValueToStatement(PreparedStatement preparedStatement, int parameterIndex, T value) throws SQLException {
        GenericDataType genericDataType = definition.getGenericDataType();
        if (ValueAdapter.supports(genericDataType)) {
            ValueAdapter<T> valueAdapter = ValueAdapter.create(genericDataType);
            if (valueAdapter != null) {
                Connection connection = preparedStatement.getConnection();
                valueAdapter.write(connection, preparedStatement, parameterIndex, value);
            }
            return;
        }
        if (genericDataType == GenericDataType.CURSOR) return;// not supported

        DataTypeParseAdapter<T> parseAdapter = definition.getParseAdapter();
        if (parseAdapter != null) {
            String stringValue =  parseAdapter.toString(value);
            preparedStatement.setString(parameterIndex, stringValue);
            return;
        }

        if (value == null) {
            preparedStatement.setObject(parameterIndex, null);
        } else {
            Class clazz = definition.getTypeClass();
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
                throw new SQLException("Can not convert \"" + value + "\" into " + definition.getName());
            }
        }
    }

    public int getSqlType(){
        return definition.getSqlType();
    }


    public String toString() {
        return definition.getName();
    }

    /*********************************************************
     *                 DynamicContentElement                 *
     *********************************************************/

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void reload() {
    }

    @Override
    public void refresh() {

    }

    @Override
    public DynamicContentType getDynamicContentType() {
        return null;
    }

    @Override
    public int compareTo(@NotNull Object o) {
        DBNativeDataType remote = (DBNativeDataType) o;
        return getName().compareTo(remote.getName());
    }

    @Override
    protected void disposeInner() {
        nullify();
    }
}
