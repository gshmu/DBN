package com.dci.intellij.dbn.object.lookup;

import com.dci.intellij.dbn.common.Reference;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.state.PersistentStateElement;
import com.dci.intellij.dbn.common.thread.Timeout;
import com.dci.intellij.dbn.common.util.Lists;
import com.dci.intellij.dbn.common.util.Strings;
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
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jdom.Element;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.connectionIdAttribute;
import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.stringAttribute;
import static com.dci.intellij.dbn.vfs.DatabaseFileSystem.PS;

@Slf4j
@Getter
@Setter
public class DBObjectRef<T extends DBObject> implements Comparable<DBObjectRef<?>>, Reference<T>, PersistentStateElement, ConnectionProvider {
    public static final String QUOTE = "'";
    private short overload;
    private DBObjectRef<?> parent;
    private DBObjectType objectType;
    private String objectName;
    protected ConnectionId connectionId;

    private WeakRef<T> reference;
    private int hashCode = -1;
    private static final Pattern PATH_TOKENIZER = Pattern.compile("[^/']+|'([^']*)'");

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
        String objectIdentifier = stringAttribute(element, "object-ref");
        if (Strings.isNotEmpty(objectIdentifier)) {
            try {
                DBObjectRef<T> objectRef = new DBObjectRef<>();
                objectRef.readState(element);
                return objectRef;
            } catch (Exception ignore) {
                // deserialization exception already logged
            }
        }
        return null;
    }

    @Override
    public void readState(Element element) {
        if (element != null) {
            ConnectionId connectionId = connectionIdAttribute(element, "connection-id");
            String objectIdentifier = stringAttribute(element, "object-ref");
            deserialize(connectionId, objectIdentifier);
        }
    }


    @Override
    public void writeState(Element element) {
        String value = serialize();

        element.setAttribute("connection-id", getConnectionId().id());
        element.setAttribute("object-ref", value);
    }

    private void deserialize(ConnectionId connectionId, String objectIdentifier) {
        try {
            List<String> tokens = tokenizePath(objectIdentifier);

            DBObjectRef<?> objectRef = null;
            DBObjectType objectType = null;
            int tokenCount = tokens.size();
            for (int i = 0; i< tokenCount; i++) {
                String token = tokens.get(i);
                if (objectType == null) {
                    if (i == tokenCount -1) {
                        // last optional "overload" numeric token
                        this.overload = Short.parseShort(token);
                    } else {
                        objectType = DBObjectType.forListName(token, objectRef == null ? null : objectRef.objectType);
                    }
                } else {
                    if (i < tokenCount - 2) {
                        objectRef = objectRef == null ?
                                new DBObjectRef<>(connectionId, objectType, token) :
                                new DBObjectRef<>(objectRef, objectType, token);
                    } else {
                        this.parent = objectRef;
                        this.objectType = objectType;
                        this.objectName = token.intern();
                    }
                    objectType = null;
                }
            }
        } catch (Exception e) {
            log.error("Failed to deserialize object {}", objectIdentifier, e);
            throw e;
        }
    }

    private static List<String> tokenizePath(String objectIdentifier) {
        List<String> tokens = new ArrayList<>();
        Matcher matcher = PATH_TOKENIZER.matcher(objectIdentifier);
        while (matcher.find()) {
            String token = matcher.group(0);
            if (token.startsWith(QUOTE)) {
                token = token.substring(1, token.length() - 1);
            }
            tokens.add(token);
        }
        return tokens;
    }

    private static String quotePathElement(String pathElement) {
        if (pathElement.contains(PS)) {
            return QUOTE + pathElement + QUOTE;
        }
        return pathElement;
    }

    @NotNull
    public String serialize() {
        StringBuilder builder = new StringBuilder();
        builder.append(objectType.getListName());
        builder.append(PS);
        builder.append(quotePathElement(objectName));

        DBObjectRef<?> parent = this.parent;
        while (parent != null) {
            builder.insert(0, PS);
            builder.insert(0, quotePathElement(parent.objectName));
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

    public boolean is(@NotNull DBObject object) {
        return Objects.equals(object.getRef(), this);
    }

    @Contract("null -> null;!null -> !null;")
    public static <T extends DBObject> DBObjectRef<T> of(@Nullable T object) {
        return object == null ? null : (DBObjectRef<T>) object.getRef();
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

    @NotNull
    public static <T extends DBObject> List<T> get(@NotNull List<DBObjectRef<T>> objectRefs) {
        return Lists.convert(objectRefs, ref -> get(ref));
    }

    @NotNull
    public static <T extends DBObject> List<T> ensure(@NotNull List<DBObjectRef<T>> objectRefs) {
        return Lists.convert(objectRefs, ref -> ensure(ref));
    }

    @NotNull
    public static <T extends DBObject> List<DBObjectRef<T>> from(@NotNull List<T> objects) {
        return Lists.convert(objects, obj -> of(obj));
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

    private T load(Project project) {
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

    protected DBSchema getSchema() {
        return (DBSchema) getParentObject(DBObjectType.SCHEMA);
    }

    public String getSchemaName() {
        DBObjectRef<?> schemaRef = getParentRef(DBObjectType.SCHEMA);
        return schemaRef == null ? null : schemaRef.objectName;
    }

    public String getFileName() {
        if (overload == 0) {
            return objectName;
        } else {
            return objectName + PS + overload;
        }
    }

    public boolean isOfType(DBObjectType objectType) {
        return this.objectType.matches(objectType);
    }

    public boolean isLoaded() {
        return (parent == null || parent.isLoaded()) && reference != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DBObjectRef<?> that = (DBObjectRef<?>) o;
        return deepEqual(this, that);
    }

    private static boolean deepEqual(DBObjectRef local, DBObjectRef remote) {
        if (local == null && remote == null) {
            return true;
        }

        if (local == null || remote == null) {
            return false;
        }

        if (local == remote) {
            return true;
        }

        if (local.getObjectType() != remote.getObjectType()) {
            return false;
        }

        if (local.getOverload() != remote.getOverload()) {
            return false;
        }

        if (local.getConnectionId() != remote.getConnectionId()) {
            return false;
        }

        if (!Objects.equals(local.getObjectName(), remote.getObjectName())) {
            return false;
        }

        return deepEqual(local.getParent(), remote.getParent());
    }

    @Override
    public int hashCode() {
        if (hashCode == -1) {
            hashCode = (getConnectionId() + PS + serialize()).hashCode();
        }
        return hashCode;
    }

    @Override
    public int compareTo(@NotNull DBObjectRef<?> that) {
        int result = this.getConnectionId().id().compareTo(that.getConnectionId().id());
        if (result != 0) return result;

        if (this.parent != null && that.parent != null) {
            if (Objects.equals(this.parent, that.parent)) {
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
        return 0;
    }

    @Override
    public String toString() {
        return objectName;
    }

}
