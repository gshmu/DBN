package com.dci.intellij.dbn.data.type;

import com.dci.intellij.dbn.common.util.Safe;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.value.ComplexValue;
import com.dci.intellij.dbn.database.common.metadata.def.DBDataTypeMetadata;
import com.dci.intellij.dbn.object.DBPackage;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBType;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

@Getter
@EqualsAndHashCode
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
        return new Ref(metadata).get(connectionHandler);
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
        return new Ref(dataTypeName, declaredTypeName, declaredTypeOwner, declaredTypePackage, length, precision, scale, set).get(connectionHandler);
    }

    protected DBDataType() {}


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

    public static class Ref {
        private final String dataTypeName;
        private final String declaredTypeName;
        private final String declaredTypeOwner;
        private final String declaredTypeProgram;
        private final long length;
        private final int precision;
        private final int scale;
        private final boolean set;

        public Ref(DBDataTypeMetadata metadata) throws SQLException {
            this.dataTypeName = metadata.getDataTypeName();
            this.declaredTypeName = metadata.getDeclaredTypeName();
            this.declaredTypeOwner = metadata.getDeclaredTypeOwner();
            this.declaredTypeProgram = metadata.getDeclaredTypeProgram();

            this.length = metadata.getDataLength();
            this.precision = metadata.getDataPrecision();
            this.scale = metadata.getDataScale();
            this.set = metadata.isSet();
        }

        Ref(String dataTypeName, String declaredTypeName, String declaredTypeOwner, String declaredTypeProgram, long length, int precision, int scale, boolean set) {
            this.dataTypeName = dataTypeName;
            this.declaredTypeName = declaredTypeName;
            this.declaredTypeOwner = declaredTypeOwner;
            this.declaredTypeProgram = declaredTypeProgram;

            this.length = length;
            this.precision = precision;
            this.scale = scale;
            this.set = set;
        }

        public DBDataType get(ConnectionHandler connectionHandler) {
            String name = null;
            DBType declaredType = null;
            DBNativeDataType nativeDataType = null;

            DBObjectBundle objectBundle = connectionHandler.getObjectBundle();
            if (declaredTypeOwner != null) {
                DBSchema typeSchema = objectBundle.getSchema(declaredTypeOwner);
                if (typeSchema != null) {
                    if (declaredTypeProgram != null) {
                        DBPackage packagee = typeSchema.getPackage(declaredTypeProgram);
                        if (packagee != null) {
                            declaredType = packagee.getType(declaredTypeName);
                        } /*else {
                            DBType type = typeSchema.getType(declaredTypeProgram);
                            if (type != null) {
                                declaredType = packagee.getType(declaredTypeName);
                            }
                        }*/
                    } else {
                        declaredType = typeSchema.getType(declaredTypeName);
                    }
                }
                if (declaredType == null)  {
                    name = declaredTypeName;
                }

                DBNativeDataType nDataType = objectBundle.getNativeDataType(dataTypeName);
                if (nDataType != null && nDataType.getDefinition().isPseudoNative()) {
                    nativeDataType = nDataType;
                }

            } else {
                nativeDataType = objectBundle.getNativeDataType(dataTypeName);
                if (nativeDataType == null) name = dataTypeName;
            }

            List<DBDataType> cachedDataTypes = objectBundle.getCachedDataTypes();
            DBDataType dataType = find(cachedDataTypes, name, declaredType, nativeDataType);
            if (dataType == null) {
                synchronized (cachedDataTypes) {
                    dataType = find(cachedDataTypes, name, declaredType, nativeDataType);
                    if (dataType == null) {
                        dataType = new DBDataType();
                        dataType.nativeDataType = nativeDataType;
                        dataType.declaredType = declaredType;
                        dataType.name = name;
                        dataType.length = length;
                        dataType.precision = precision;
                        dataType.scale = scale;
                        dataType.set = set;
                        cachedDataTypes.add(dataType);
                    }
                }
            }
            return dataType;
        }

        protected DBDataType find(List<DBDataType> cachedDataTypes, String name, DBType declaredType, DBNativeDataType nativeDataType) {
            for (DBDataType dataType : cachedDataTypes) {
                if (Safe.equal(dataType.declaredType, declaredType) &&
                        Safe.equal(dataType.nativeDataType, nativeDataType) &&
                        Safe.equal(dataType.name, name) &&
                        dataType.length == length &&
                        dataType.precision == precision &&
                        dataType.scale == scale &&
                        dataType.set == set) {
                    return dataType;
                }
            }
            return null;
        }
    }
}