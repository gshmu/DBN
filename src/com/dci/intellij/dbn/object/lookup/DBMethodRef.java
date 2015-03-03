package com.dci.intellij.dbn.object.lookup;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBProgram;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.openapi.project.Project;

public class DBMethodRef<T extends DBMethod> extends DBObjectRef<DBMethod>{

    public static final String STATE_PREFIX = "M";

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
            return super.getFileName() + "#" + getOverload();
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

    @Override
    public String getStatePrefix() {
        return STATE_PREFIX;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DBMethodRef that = (DBMethodRef) o;

        return this.overload == that.overload;

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
}
