package com.dci.intellij.dbn.data.type;

import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.object.DBPackage;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBType;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DBDataTypeBundle extends StatefulDisposable.Base {
    private final ConnectionHandlerRef connection;

    private final Latent<Map<String, DBNativeDataType>> nativeDataTypes = Latent.basic(() -> createNativeDataTypes());
    private final Map<DBDataTypeDefinition, DBDataType> dataTypes = new ConcurrentHashMap<>();

    public DBDataTypeBundle(@NotNull ConnectionHandler connection) {
        this.connection = connection.getRef();
    }

    @NotNull
    public ConnectionHandler getConnection() {
        return ConnectionHandlerRef.ensure(connection);
    }

    private Map<String, DBNativeDataType> getNativeDataTypes() {
        checkDisposed();
        return nativeDataTypes.get();
    }

    public DBNativeDataType getNativeDataType(String name) {
        if (name != null) {
            String upperCaseName = name.toUpperCase();
            Map<String, DBNativeDataType> dataTypes = getNativeDataTypes();

            DBNativeDataType dataType = dataTypes.get(upperCaseName);
            if (dataType != null) {
                return dataType;
            }

            for (Map.Entry<String, DBNativeDataType> entry : dataTypes.entrySet()) {
                String key = entry.getKey();
                DBNativeDataType value = entry.getValue();
                if (key.startsWith(upperCaseName)) {

                    return value;
                }
            }
        }
        return null;
    }


    private Map<String, DBNativeDataType> createNativeDataTypes() {
        Map<String, DBNativeDataType> nativeDataTypes = new HashMap<>();

        DatabaseInterfaceProvider interfaceProvider = getConnection().getInterfaceProvider();
        List<DataTypeDefinition> dataTypeDefinitions = interfaceProvider.getNativeDataTypes().list();
        for (DataTypeDefinition dataTypeDefinition : dataTypeDefinitions) {
            DBNativeDataType dataType = new DBNativeDataType(dataTypeDefinition);
            nativeDataTypes.put(dataType.getName().toUpperCase(), dataType);
        }
        return nativeDataTypes;
    }

    public DBDataType getDataType(DBDataTypeDefinition definition) {
        return dataTypes.computeIfAbsent(definition, d -> createDataType(d));
    }

    private DBDataType createDataType(DBDataTypeDefinition def) {
        checkDisposed();
        String name = null;
        DBType declaredType = null;
        DBNativeDataType nativeDataType = null;

        DBObjectBundle objectBundle = getConnection().getObjectBundle();
        String declaredTypeOwner = def.getDeclaredTypeOwner();
        String declaredTypeProgram = def.getDeclaredTypeProgram();
        String declaredTypeName = def.getDeclaredTypeName();
        String dataTypeName = def.getDataTypeName();

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
            if (declaredType == null) {
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

        DBDataType dataType = new DBDataType();
        dataType.setNativeType(nativeDataType);
        dataType.setDeclaredType(declaredType);
        dataType.setName(name);
        dataType.setLength(def.getLength());
        dataType.setPrecision(def.getPrecision());
        dataType.setScale(def.getScale());
        dataType.setSet(def.isSet());
        return dataType;
    }


    @Override
    protected void disposeInner() {
        nativeDataTypes.reset();
        dataTypes.clear();
    }
}
