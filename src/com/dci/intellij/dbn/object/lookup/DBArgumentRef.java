package com.dci.intellij.dbn.object.lookup;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBProgram;
import com.dci.intellij.dbn.object.common.DBObjectType;

public class DBArgumentRef extends DBObjectRef<DBArgument> {
    private int overload;

    public DBArgumentRef(DBArgument argument) {
        super(argument);
        overload = argument.getOverload();
    }

    @Nullable
    public DBArgument lookup(@NotNull ConnectionHandler connectionHandler) {
        DBMethod method = (DBMethod) getParentObject(DBObjectType.METHOD);
        return method == null ? null : method.getArgument(objectName);
    }

    protected DBProgram getProgram() {
        return (DBProgram) DBObjectRef.get(getParentRef(DBObjectType.PROGRAM));
    }

    public int getOverload() {
        return overload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DBArgumentRef that = (DBArgumentRef) o;

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
        return objectType + " " + getPath();
    }

}
