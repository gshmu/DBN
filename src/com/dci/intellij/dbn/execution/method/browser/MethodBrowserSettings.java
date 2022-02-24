package com.dci.intellij.dbn.execution.method.browser;

import com.dci.intellij.dbn.common.options.PersistentConfiguration;
import com.dci.intellij.dbn.connection.ConnectionCache;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.object.type.DBObjectType;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.connectionIdAttribute;
import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.stringAttribute;

public class MethodBrowserSettings implements PersistentConfiguration {
    private ConnectionId connectionId;
    private String schemaName;
    private DBObjectRef<DBMethod> method;
    private final Map<DBObjectType, Boolean> objectVisibility = new EnumMap<>(DBObjectType.class);

    public MethodBrowserSettings() {
        objectVisibility.put(DBObjectType.FUNCTION, true);
        objectVisibility.put(DBObjectType.PROCEDURE, true);
    }

    public ConnectionHandler getConnection() {
        return ConnectionCache.resolveConnection(connectionId);
    }

    public void setConnection(ConnectionHandler connection) {
        this.connectionId = connection == null ? null : connection.getConnectionId();
    }

    public DBSchema getSchema() {
        return getConnection() == null || schemaName == null ? null : getConnection().getObjectBundle().getSchema(schemaName);
    }

    public Set<DBObjectType> getVisibleObjectTypes() {
        Set<DBObjectType> objectTypes = EnumSet.noneOf(DBObjectType.class);

        for (Map.Entry<DBObjectType, Boolean> entry : objectVisibility.entrySet()) {
            DBObjectType objectType = entry.getKey();
            Boolean visible = entry.getValue();

            if (visible) {
                objectTypes.add(objectType);
            }
        }
        return objectTypes;
    }

    public boolean getObjectVisibility(DBObjectType objectType) {
        return objectVisibility.get(objectType);
    }

    public boolean setObjectVisibility(DBObjectType objectType, boolean visibility) {
        if (getObjectVisibility(objectType) != visibility) {
            objectVisibility.put(objectType, visibility);
            return true;
        }
        return false;        
    }

    public void setSchema(DBSchema schema) {
        this.schemaName = schema == null ? null : schema.getName();
    }

    @Nullable
    public DBMethod getMethod() {
        return method == null ? null : method.get();
    }

    public void setMethod(DBMethod method) {
        this.method = DBObjectRef.of(method);
    }

    @Override
    public void readConfiguration(Element element) {
        connectionId = connectionIdAttribute(element, "connection-id");
        schemaName = stringAttribute(element, "schema");

        Element methodElement = element.getChild("selected-method");
        if (methodElement != null) {
            method = new DBObjectRef<>();
            method.readState(methodElement);
        }
    }

    @Override
    public void writeConfiguration(Element element) {
        ConnectionHandler connection = getConnection();
        if (connection != null) element.setAttribute("connection-id", connection.getConnectionId().id());
        if (schemaName != null) element.setAttribute("schema", schemaName);
        if(method != null) {
            Element methodElement = new Element("selected-method");
            method.writeState(methodElement);
            element.addContent(methodElement);
        }
    }
}
