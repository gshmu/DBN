package com.dci.intellij.dbn.object.lookup;

import com.dci.intellij.dbn.common.options.PersistentConfiguration;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBProgram;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DBMethodRef<T extends DBMethod> extends DBObjectRef<DBMethod> implements PersistentConfiguration {
    private int overload;

    public DBMethodRef(T method) {
        super(method);
        overload = method.getOverload();
    }

    public DBMethodRef() {
        super();
    }

    @Nullable
    @Override
    public T get() {
        return (T) super.get();
    }

    @Nullable
    @Override
    public T get(Project project) {
        return (T) super.get(project);
    }

    @Override
    public String getFileName() {
        if (overload == 0) {
            return super.getFileName();
        } else {
            return super.getFileName() + "#" + overload;
        }

    }

    @Nullable
    protected T lookup(@NotNull ConnectionHandler connectionHandler) {
        DBSchema schema = getSchema();
        if (schema == null) return null;

        DBMethod method;
        DBProgram program = getProgram();
        if (program == null) {
            method = schema.getMethod(objectName, objectType, overload);
        } else {
            method = program.getMethod(objectName, overload);
        }

        return method != null && method.getObjectType() == objectType ? (T) method : null;

/*
        DBMethod method;
        Node programNode = getProgramNode();
        Node methodNode = getMethodNode();
        DBObjectType methodObjectType = methodNode.getType();
        if (programNode != null) {
            DBProgram program = schema.getProgram(programNode.getName());
            if (program == null || program.getObjectType() != programNode.getType()) return null;

            method = program.getMethod(methodNode.getName(), overload);
        } else {
            method = schema.getMethod(methodNode.getName(), methodObjectType.getName(), overload);
        }
*/
    }

    protected DBProgram getProgram() {
        return (DBProgram) getParentObject(DBObjectType.PROGRAM);
    }


    public String getQualifiedMethodName() {

        String programName = getProgramName();
        String methodName = getMethodName();
        return programName == null ? methodName : programName + "." + methodName;
    }

    public String getProgramName() {
        DBObjectRef programRef = getParentRef(DBObjectType.PROGRAM);
        return programRef == null ? null : programRef.objectName;
    }

    public DBObjectType getProgramObjectType() {
        DBObjectRef programRef = getParentRef(DBObjectType.PROGRAM);
        return programRef == null ? null : programRef.objectType;
    }

    public String getMethodName() {
        return objectName;
    }

    public DBObjectType getMethodObjectType() {
        return objectType;
    }

    public int getOverload() {
        return overload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DBMethodRef that = (DBMethodRef) o;

        return overload == that.overload;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + overload;
        return result;
    }

    @Override
    public String toString() {
        return objectType.getName() + " " + getPath();
    }

    /*********************************************************
     *                   JDOMExternalizable                  *
     *********************************************************/
    public void readConfiguration(Element element) throws InvalidDataException {
        String connectionId = element.getAttributeValue("connection-id");
        String schemaName = element.getAttributeValue("schema-name");
        DBObjectRef<DBSchema> schemaRef = new DBObjectRef<DBSchema>(connectionId, DBObjectType.SCHEMA, schemaName);

        DBObjectRef<DBProgram> programRef = null;
        String programTypeName = element.getAttributeValue("program-type");
        if (programTypeName != null) {
            String programName = element.getAttributeValue("program-name");
            DBObjectType programObjectType = DBObjectType.getObjectType(programTypeName);
            programRef = new DBObjectRef<DBProgram>(schemaRef, programObjectType, programName);
        }

        objectName = element.getAttributeValue("method-name");
        objectType = DBObjectType.getObjectType(element.getAttributeValue("method-type"));
        parent = programRef == null ? schemaRef : programRef;

        String overload = element.getAttributeValue("method-overload");
        this.overload = Integer.parseInt(overload == null ? "0" : overload);
    }

    public void writeConfiguration(Element element) throws WriteExternalException {
        element.setAttribute("connection-id", getConnectionId());
        element.setAttribute("schema-name", getSchemaName());

        DBObjectRef programRef = getParentRef(DBObjectType.PROGRAM);
        if (programRef != null) {
            element.setAttribute("program-type", programRef.objectType.getName());
            element.setAttribute("program-name", programRef.objectName);
        }

        element.setAttribute("method-type", objectType.getName());
        element.setAttribute("method-name", objectName);

        element.setAttribute("method-overload", Integer.toString(overload));
    }
}
