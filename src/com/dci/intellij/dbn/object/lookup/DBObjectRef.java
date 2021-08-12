package com.dci.intellij.dbn.object.lookup;

import com.dci.intellij.dbn.common.Reference;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.state.PersistentStateElement;
import com.dci.intellij.dbn.common.thread.Timeout;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionCache;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.ConnectionProvider;
import com.dci.intellij.dbn.language.common.WeakRef;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.object.common.DBVirtualObject;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static com.dci.intellij.dbn.vfs.DatabaseFileSystem.PS;

public class DBObjectRef<T extends DBObject> implements Comparable, Reference<T>, PersistentStateElement, ConnectionProvider {
    public short overload;
    public DBObjectRef<?> parent;
    public DBObjectType objectType;
    public String objectName;
    protected ConnectionId connectionId;

    private WeakRef<T> reference;
    private int hashCode = -1;

    public DBObjectRef(ConnectionId connectionId, String identifier) {
        deserialize(connectionId, identifier);
    }

    public DBObjectRef(T object, String name) {
        this(object, object.getObjectType(), name);
    }
    public DBObjectRef(T object, DBObjectType objectType, String name) {
        this.reference = WeakRef.of(object);
        this.objectName = name.intern();
        this.objectType = objectType;
        this.overload = object.getOverload();
        DBObject parentObj = object.getParentObject();
        if (parentObj != null) {
            this.parent = parentObj.getRef();
        } else if (!(object instanceof DBVirtualObject)){
            ConnectionHandler connectionHandler = object.getConnectionHandler();
            this.connectionId = connectionHandler.getConnectionId();
        }
    }

    public DBObjectRef(DBObjectRef<?> parent, DBObjectType objectType, String objectName) {
        this.parent = parent;
        this.objectType = objectType;
        this.objectName = objectName.intern();
    }

    public DBObjectRef(ConnectionId connectionId, DBObjectType objectType, String objectName) {
        this.connectionId = connectionId;
        this.objectType = objectType;
        this.objectName = objectName.intern();
    }

    public DBObjectRef() {

    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public DBObjectType getObjectType() {
        return objectType;
    }

    public DBObject getParentObject(DBObjectType objectType) {
        DBObjectRef<?> parentRef = getParentRef(objectType);
        return DBObjectRef.get(parentRef);
    }

    public DBObjectRef<?> getParentRef(DBObjectType objectType) {
        DBObjectRef<?> parent = this;
        while (parent != null) {
            if (parent.objectType.matches(objectType)) {
                return parent;
            }
            parent = parent.parent;
        }
        return null;
    }

    public static <T extends DBObject> DBObjectRef<T> from(Element element) {
        String objectRefDefinition = element.getAttributeValue("object-ref");
        if (StringUtil.isNotEmpty(objectRefDefinition)) {
            DBObjectRef<T> objectRef = new DBObjectRef<>();
            objectRef.readState(element);
            return objectRef;
        }
        return null;
    }

    @Override
    public void readState(Element element) {
        if (element != null) {
            ConnectionId connectionId = ConnectionId.get(element.getAttributeValue("connection-id"));
            String objectIdentifier = element.getAttributeValue("object-ref");
            deserialize(connectionId, objectIdentifier);
        }
    }

    public void deserialize(ConnectionId connectionId, String objectIdentifier) {
        if (objectIdentifier.contains("]")) {
            deserializeOld(connectionId, objectIdentifier);
        } else {
            String[] tokens = objectIdentifier.split(PS);

            DBObjectRef<?> objectRef = null;
            DBObjectType objectType = null;
            for (int i=0; i<tokens.length; i++) {
                String token = tokens[i];
                if (objectType == null) {
                    if (i == tokens.length -1) {
                        // last optional "overload" numeric token
                        this.overload = Short.parseShort(token);
                    } else {
                        objectType = DBObjectType.forListName(token, objectRef == null ? null : objectRef.objectType);
                    }
                } else {
                    if (i < tokens.length - 2) {
                        objectRef = objectRef == null ?
                                new DBObjectRef<>(connectionId, objectType, token) :
                                new DBObjectRef<>(objectRef, objectType, token);
                    } else {
                        this.parent = objectRef;
                        this.objectType = objectType;
                        this.objectName = token;
                    }
                    objectType = null;
                }
            }
        }
    }

    @Deprecated
    private void deserializeOld(ConnectionId connectionId, String objectIdentifier) {
        int typeEndIndex = objectIdentifier.indexOf("]");
        StringTokenizer objectTypes = new StringTokenizer(objectIdentifier.substring(1, typeEndIndex), PS);

        int objectStartIndex = typeEndIndex + 2;
        int objectEndIndex = objectIdentifier.lastIndexOf("]");

        StringTokenizer objectNames = new StringTokenizer(objectIdentifier.substring(objectStartIndex, objectEndIndex), PS);

        DBObjectRef<?> objectRef = null;
        while (objectTypes.hasMoreTokens()) {
            String objectTypeName = objectTypes.nextToken();
            String objectName = objectNames.nextToken();
            DBObjectType objectType = DBObjectType.get(objectTypeName);
            if (objectTypes.hasMoreTokens()) {
                objectRef = objectRef == null ?
                        new DBObjectRef<>(connectionId, objectType, objectName) :
                        new DBObjectRef<>(objectRef, objectType, objectName);
            } else {
                if (objectNames.hasMoreTokens()) {
                    String overloadToken = objectNames.nextToken();
                    this.overload = Short.parseShort(overloadToken);
                }
                this.parent = objectRef;
                this.objectType = objectType;
                this.objectName = objectName;
            }
        }

    }

    @Override
    public void writeState(Element element) {
        String value = serialize();

        element.setAttribute("connection-id", getConnectionId().id());
        element.setAttribute("object-ref", value);
    }

    @NotNull
    public String serialize() {
        StringBuilder builder = new StringBuilder();
        builder.append(objectType.getListName());
        builder.append(PS);
        builder.append(objectName);

        DBObjectRef<?> parent = this.parent;
        while (parent != null) {
            builder.insert(0, PS);
            builder.insert(0, parent.objectName);
            builder.insert(0, PS);
            builder.insert(0, parent.objectType.getListName());
            parent = parent.parent;
        }

        if (overload > 0) {
            builder.append(PS);
            builder.append(overload);
        }

        return builder.toString();
    }


    public String getPath() {
        DBObjectRef<?> parent = this.parent;
        if (parent == null) {
            return objectName;
        } else {
            StringBuilder buffer = new StringBuilder(objectName);
            while(parent != null) {
                buffer.insert(0, ".");
                buffer.insert(0, parent.objectName);
                parent = parent.parent;
            }
            return buffer.toString();
        }
    }

    public String getQualifiedName() {
        return getPath();
    }

    /**
     * qualified object name without schema (e.g. PROGRAM.METHOD)
     */
    public String getQualifiedObjectName() {
        DBObjectRef<?> parent = this.parent;
        if (parent == null || parent.objectType == DBObjectType.SCHEMA) {
            return objectName;
        } else {
            StringBuilder buffer = new StringBuilder(objectName);
            while(parent != null && parent.objectType != DBObjectType.SCHEMA) {
                buffer.insert(0, '.');
                buffer.insert(0, parent.objectName);
                parent = parent.parent;
            }
            return buffer.toString();
        }    }

    public String getQualifiedNameWithType() {
        return objectType.getName() + " \"" + getPath() + "\"";
    }

    public String getTypePath() {
        DBObjectRef<?> parent = this.parent;
        if (parent == null) {
            return objectType.getName();
        } else {
            StringBuilder buffer = new StringBuilder(objectType.getName());
            while(parent != null) {
                buffer.insert(0, '.');
                buffer.insert(0, parent.objectType.getName());
                parent = parent.parent;
            }
            return buffer.toString();
        }
    }


    public ConnectionId getConnectionId() {
        return parent == null ? connectionId : parent.getConnectionId();
    }

/*
    public DBObjectRef getParent() {
        return parent;
    }
*/

    public boolean is(@NotNull DBObject object) {
        return object.getRef().equals(this);
    }

    @Nullable
    public static <T extends DBObject> DBObjectRef<T> of(T object) {
        return object == null ? null : object.getRef();
    }

    @Nullable
    public static <T extends DBObject> T get(DBObjectRef<T> objectRef) {
        return objectRef == null ? null : objectRef.get();
    }

    @NotNull
    public static <T extends DBObject> T ensure(DBObjectRef<T> objectRef) {
        T object = get(objectRef);
        return Failsafe.nn(object);
    }

    public static List<DBObject> get(List<DBObjectRef<?>> objectRefs) {
        List<DBObject> objects = new ArrayList<>(objectRefs.size());
        for (DBObjectRef<?> objectRef : objectRefs) {
            objects.add(get(objectRef));
        }
        return objects;
    }

    public static List<DBObject> ensure(List<DBObjectRef<?>> objectRefs) {
        List<DBObject> objects = new ArrayList<>(objectRefs.size());
        for (DBObjectRef<?> objectRef : objectRefs) {
            objects.add(ensure(objectRef));
        }
        return objects;
    }

    public static List<DBObjectRef<?>> from(List<DBObject> objects) {
        List<DBObjectRef<?>> objectRefs = new ArrayList<>(objects.size());
        for (DBObject object : objects) {
            objectRefs.add(of(object));
        }
        return objectRefs;
    }

    @Override
    @Nullable
    public T get() {
        return load(null);
    }

    @Nullable
    public T ensure(long timeoutSeconds) {
        return Timeout.call(timeoutSeconds, null, true, () -> get());
    }

    public T ensure(){
        return Failsafe.nn(get());
    }

    @Nullable
    public T get(Project project) {
        return load(project);
    }

    protected final T load(Project project) {
        T object = getObject();
        if (object == null) {
            clearReference();
            ConnectionHandler connectionHandler = resolveConnectionHandler(project);
            if (Failsafe.check(connectionHandler) && connectionHandler.isEnabled()) {
                object = lookup(connectionHandler);
                if (object != null) {
                    reference = WeakRef.of(object);
                }
            }
        }
        return object;
    }

    private T getObject() {
        try {
            if (reference == null) {
                return null;
            }

            T object = reference.get();
            if (object == null || object.isDisposed()) {
                return null;
            }
            return object;
        } catch (Exception e) {
            return null;
        }
    }

    private void clearReference() {
        if (reference != null) {
            reference.clear();
            reference = null;
        }
    }


    @Nullable
    private T lookup(@NotNull ConnectionHandler connectionHandler) {
        DBObject object = null;
        if (parent == null) {
            DBObjectBundle objectBundle = connectionHandler.getObjectBundle();
            object = objectBundle.getObject(objectType, objectName, overload);
        } else {
            Project project = connectionHandler.getProject();
            DBObject parentObject = parent.get(project);
            if (parentObject != null) {
                object = parentObject.getChildObject(objectType, objectName, overload, true);
                DBObjectType genericType = objectType.getGenericType();
                if (object == null && genericType != objectType) {
                    object = parentObject.getChildObject(genericType, objectName, overload, true);
                }
            }
        }
        return (T) object;
    }


    private ConnectionHandler resolveConnectionHandler(Project project) {
        ConnectionId connectionId = getConnectionId();
        return project == null || project.isDisposed() ?
                ConnectionCache.findConnectionHandler(connectionId) :
                ConnectionManager.getInstance(project).getConnectionHandler(connectionId);
    }

    @Nullable
    public ConnectionHandler resolveConnectionHandler() {
        return ConnectionCache.findConnectionHandler(getConnectionId());
    }

    @Nullable
    @Override
    public ConnectionHandler getConnectionHandler() {
        return resolveConnectionHandler();
    }

    @Override
    public int compareTo(@NotNull Object o) {
        if (o instanceof DBObjectRef) {
            DBObjectRef<?> that = (DBObjectRef<?>) o;
            int result = this.getConnectionId().id().compareTo(that.getConnectionId().id());
            if (result != 0) return result;

            if (this.parent != null && that.parent != null) {
                if (this.parent.equals(that.parent)) {
                    result = this.objectType.compareTo(that.objectType);
                    if (result != 0) return result;

                    int nameCompare = this.objectName.compareTo(that.objectName);
                    return nameCompare == 0 ? this.overload - that.overload : nameCompare;
                } else {
                    return this.parent.compareTo(that.parent);
                }
            } else if(this.parent == null && that.parent == null) {
                result = this.objectType.compareTo(that.objectType);
                if (result != 0) return result;

                return this.objectName.compareTo(that.objectName);
            } else if (this.parent == null) {
                return -1;
            } else if (that.parent == null) {
                return 1;
            }
        }
        return 0;
    }

    protected DBSchema getSchema() {
        return (DBSchema) getParentObject(DBObjectType.SCHEMA);
    }

    public String getSchemaName() {
        DBObjectRef<?> schemaRef = getParentRef(DBObjectType.SCHEMA);
        return schemaRef == null ? null : schemaRef.objectName;
    }

/*
    public int getOverload() {
        return overload;
    }
*/

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DBObjectRef<?> that = (DBObjectRef<?>) o;
        return this.hashCode() == that.hashCode();
    }

    @Override
    public String toString() {
        return objectName;
    }

    @Override
    public synchronized int hashCode() {
        if (hashCode == -1) {
            hashCode = (getConnectionId() + PS + serialize()).hashCode();
        }
        return hashCode;
    }

    /*
        public String getObjectName() {
            return objectName;
        }

    */
    public String getFileName() {
        if (overload == 0) {
            return objectName;
        } else {
            return objectName + PS + overload;
        }
    }

/*
    @NotNull
    public DBObjectType getObjectType() {
        return objectType;
    }
*/

    public boolean isOfType(DBObjectType objectType) {
        return this.objectType.matches(objectType);
    }

    public boolean isLoaded() {
        return (parent == null || parent.isLoaded()) && reference != null;
    }
}
