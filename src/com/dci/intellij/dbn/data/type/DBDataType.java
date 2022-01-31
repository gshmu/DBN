package com.dci.intellij.dbn.data.type;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.value.ComplexValue;
import com.dci.intellij.dbn.database.common.metadata.def.DBDataTypeMetadata;
import com.dci.intellij.dbn.object.DBType;
import lombok.Getter;
import lombok.Setter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

@Getter
@Setter
public class DBDataType {
    private DBNativeDataType nativeDataType;
    private DBType declaredType;
    private String name;
    private String qualifiedName;
    private long length;
    private int precision;
    private int scale;
    private boolean set;

    public static DBDataType get(ConnectionHandler connectionHandler, DBDataTypeMetadata metadata) throws SQLException {
        DBDataTypeDefinition definition = new DBDataTypeDefinition(metadata);
        return connectionHandler.getObjectBundle().getDataTypes().getDataType(definition);
    }

    public static DBDataType get(ConnectionHandler connectionHandler, String dataTypeName, long length, int precision, int scale, boolean set) {
        String declaredTypeName = null;
        String declaredTypeOwner = null;
        String declaredTypePackage = null;
        if (dataTypeName.contains(".")) {
            String[] nameChain = dataTypeName.split("\\.");
            if (nameChain.length == 1) {
                declaredTypeName = nameChain[0];
            } else if (nameChain.length == 2) {
                declaredTypeOwner = nameChain[0];
                declaredTypeName = nameChain[1];
            } else if (nameChain.length == 3) {
                declaredTypeOwner = nameChain[0];
                declaredTypePackage = nameChain[1];
                declaredTypeName = nameChain[2];
            }
        }
        DBDataTypeDefinition definition = new DBDataTypeDefinition(dataTypeName, declaredTypeName, declaredTypeOwner, declaredTypePackage, length, precision, scale, set);
        return connectionHandler.getObjectBundle().getDataTypes().getDataType(definition);
    }

    public DBDataType() {}


    public boolean isSet() {
        return set;
    }

    public boolean isDeclared() {
        return declaredType != null;
    }

    public boolean isNative() {
        return nativeDataType != null;
    }

    public boolean isPurelyDeclared() {
        return isDeclared() && !isNative();
    }

    public boolean isPurelyNative() {
        return isNative() && !isDeclared();
    }

    public boolean isNativeDeclared() {
        return nativeDataType != null && declaredType != null;
    }

    public String getName() {
        return (set ? "set of " : "") +
                (nativeDataType == null && declaredType == null ? name :
                 nativeDataType == null ? declaredType.getQualifiedName() :
                 nativeDataType.getName());
    }

    public Class getTypeClass() {
        return nativeDataType == null ? Object.class : nativeDataType.getDefinition().getTypeClass();
    }

    public int getSqlType() {
        return nativeDataType == null ? Types.CHAR : nativeDataType.getSqlType();
    }

    public Object getValueFromResultSet(ResultSet resultSet, int columnIndex) throws SQLException {
        if (nativeDataType != null) {
            return nativeDataType.getValueFromResultSet(resultSet, columnIndex);
        } else {
            return new ComplexValue(resultSet, columnIndex);
        }
    }

    public void setValueToResultSet(ResultSet resultSet, int columnIndex, Object value) throws SQLException {
        if (nativeDataType != null) {
            nativeDataType.setValueToResultSet(resultSet, columnIndex, value);
        }
    }

    public void setValueToPreparedStatement(PreparedStatement preparedStatement, int index, Object value) throws SQLException {
        if (nativeDataType != null) {
            nativeDataType.setValueToStatement(preparedStatement, index, value);
        }
    }

    public String getQualifiedName() {
        if (qualifiedName == null) {
            StringBuilder buffer = new StringBuilder();
            String name = getName();
            buffer.append(name == null ? "" : name.toLowerCase());
            if (precision > 0) {
                buffer.append(" (");
                buffer.append(precision);
                if (scale > 0) {
                    buffer.append(", ");
                    buffer.append(scale);
                }
                buffer.append(')');
            } else if (length > 0) {
                buffer.append(" (");
                buffer.append(length);
                buffer.append(')');
            }
            qualifiedName = buffer.toString();
        }
        return qualifiedName;
    }

    @Override
    public String toString() {
        return getName();
    }

    public GenericDataType getGenericDataType() {
        return nativeDataType != null ? nativeDataType.getGenericDataType() : GenericDataType.OBJECT;
    }

    public String getContentTypeName() {
        return nativeDataType == null ? null : nativeDataType.getDefinition().getContentTypeName();
    }

}