package com.dci.intellij.dbn.object.lookup;

import com.dci.intellij.dbn.common.Reference;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionCache;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

public class DBObjectRef<T extends DBObject> implements Comparable, Reference<T> {
    protected String connectionId;
    protected Node[] nodes;
    private WeakReference<T> reference;

    public DBObjectRef(T object) {
        reference = new WeakReference<T>(object);
        ConnectionHandler connectionHandler = object.getConnectionHandler();
        connectionId = connectionHandler == null ? null : connectionHandler.getId();

        List<DBObject> chain = new ArrayList<DBObject>();
        chain.add(object);

        DBObject parentObject = object.getParentObject();
        while (parentObject != null) {
            chain.add(0, parentObject);
            parentObject = parentObject.getParentObject();
        }
        int length = chain.size();
        nodes = new Node[length];
        for (int i = 0; i<length; i++) {
            DBObject chainObject = chain.get(i);
            nodes[i] = new Node(chainObject.getObjectType(), chainObject.getName());
        }
    }

    public DBObjectRef(ConnectionHandler connectionHandler) {
        this.connectionId = connectionHandler == null ? null : connectionHandler.getId();
    }

    public DBObjectRef(String connectionId) {
        this.connectionId = connectionId;
    }

    public DBObjectRef() {

    }

    public static DBObjectRef from(Element element) {
        if (StringUtil.isNotEmpty(element.getAttributeValue("object-ref"))) {
            DBObjectRef objectRef = new DBObjectRef();
            objectRef.readState(element);
            return objectRef;
        }
        return null;
    }

    public void readState(Element element) {
        if (element != null) {
            connectionId = element.getAttributeValue("connection-id");
            String databaseObject = element.getAttributeValue("object-ref");
            int typeEndIndex = databaseObject.lastIndexOf("]");
            StringTokenizer objectTypes = new StringTokenizer(databaseObject.substring(1, typeEndIndex), ".");
            StringTokenizer objectNames = new StringTokenizer(databaseObject.substring(typeEndIndex + 1), ".");
            while (objectTypes.hasMoreTokens()) {
                String objectTypeName = objectTypes.nextToken();
                String objectName = objectNames.nextToken();
                append(DBObjectType.getObjectType(objectTypeName), objectName);
            }
        }
    }

    public void writeState(Element element) {
        element.setAttribute("connection-id", connectionId);
        StringBuilder objectTypes = new StringBuilder();
        StringBuilder objectNames = new StringBuilder();

        for (Node node: nodes) {
            if (objectTypes.length() > 0) {
                objectTypes.append(".");
                objectNames.append(".");
            }
            objectTypes.append(node.getType().getName());
            objectNames.append(node.getName());
        }

        element.setAttribute("object-ref", "[" + objectTypes.toString() + "]" + objectNames);
    }

    public DBObjectRef append(DBObjectType objectType, String name) {
        Node node = new Node(objectType, name);
        if (nodes == null) {
            nodes = new Node[1];
            nodes[0] = node;
        } else {
            Node[] newNodes = new Node[nodes.length + 1];
            System.arraycopy(nodes, 0, newNodes, 0, nodes.length);
            newNodes[nodes.length] = node;
            nodes = newNodes;
        }
        return this;
    }

    public String getPath() {
        if (nodes.length == 1) {
            return nodes[0].getName();
        } else {
            StringBuilder buffer = new StringBuilder();
            for (Node node : nodes) {
                if (buffer.length() > 0) buffer.append(".");
                buffer.append(node.getName());
            }
            return buffer.toString();
        }
    }


    public String getConnectionId() {
        return connectionId;
    }

    public boolean is(@NotNull T object) {
        if (object.getConnectionHandler().getId().equals(connectionId)) {
            int index = nodes.length -1;
            DBObject checkObject = object;
            while (checkObject != null) {
                Node checkNode = nodes[index];
                if (!checkNode.matches(checkObject)) return false;
                checkObject = checkObject.getParentObject();
                index--;
            }
            return true;
        }
        return false;
    }

    @Nullable
    public static <T extends DBObject> DBObjectRef<T> from(T object) {
        return object == null ? null : object.getRef();
    }

    @Nullable
    public static <T extends DBObject> T get(DBObjectRef<T> objectRef) {
        return objectRef == null ? null : objectRef.get();
    }

    @Nullable
    public T get() {
        return load(null);
    }

    @Nullable
    public T get(Project project) {
        return load(project);
    }

    protected final T load(Project project) {
        T object = reference == null ? null : reference.get();
        if (reference == null || object == null || object.isDisposed()) {
            object = null;
            if (reference != null) {
                reference.clear();
                reference = null;
            }
            ConnectionHandler connectionHandler =
                    project == null ?
                            ConnectionCache.findConnectionHandler(connectionId) :
                            ConnectionManager.getInstance(project).getConnectionHandler(connectionId);
            if (connectionHandler != null && !connectionHandler.isDisposed() && connectionHandler.isActive()) {
                object = lookup(connectionHandler);
                if (object != null) {
                    reference = new WeakReference<T>(object);
                }
            }

        }
        return object;
    }

    @Nullable
    protected T lookup(@NotNull ConnectionHandler connectionHandler) {
        DBObject object = null;
        for (Node node : nodes) {
            DBObjectType objectType = node.getType();
            String objectName = node.getName();
            if (object == null) {
                object = connectionHandler.getObjectBundle().getObject(objectType, objectName);
            } else {
                object = object.getChildObject(objectType, objectName, true);
            }
            if (object == null) break;
        }
        return (T) object;
    }

    public ConnectionHandler lookupConnectionHandler() {
        return ConnectionCache.findConnectionHandler(connectionId);
    }

    @Override
    public int compareTo(@NotNull Object o) {
        if (o instanceof DBObjectRef) {
            DBObjectRef that = (DBObjectRef) o;
            int result = this.connectionId.compareTo(that.getConnectionId());
            if (result != 0) return result;

            if (this.nodes.length != that.nodes.length) {
                return this.nodes.length - that.nodes.length;
            }

            for (int i=0; i<this.nodes.length; i++) {
                Node thisNode = this.nodes[i];
                Node thatNode = that.nodes[i];
                result = thisNode.getType().compareTo(thatNode.getType());
                if (result != 0) return result;
                result = thisNode.getName().compareTo(thatNode.getName());
                if (result != 0) return result;
            }
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DBObjectRef that = (DBObjectRef) o;

        if (!connectionId.equals(that.connectionId)) return false;
        if (!Arrays.equals(nodes, that.nodes)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = connectionId.hashCode();
        result = 31 * result + Arrays.hashCode(nodes);
        return result;
    }

    public String getName() {
        return nodes[nodes.length-1].getName();
    }

    public String getFileName() {
        return getName();
    }

    public DBObjectType getObjectType() {
        return nodes[nodes.length-1].getType();
    }

    public boolean isOfType(DBObjectType objectType) {
        return getObjectType().matches(objectType);
    }

    public static class Node {
        private DBObjectType type;
        private String name;

        public Node(DBObjectType type, String name) {
            this.type = type;
            this.name = name;
        }

        public DBObjectType getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Node node = (Node) o;

            if (!name.equals(node.name)) return false;
            if (type != node.type) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = type.hashCode();
            result = 31 * result + name.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "[" + type.getName() + "] " + getName();
        }

        public boolean matches(DBObject object) {
            return type == object.getObjectType() && name.equals(object.getName());
        }
    }

}
