package com.dci.intellij.dbn.object;

import com.dci.intellij.dbn.object.common.DBSchemaObject;

import java.util.List;

public interface DBProgram<P extends DBProcedure, F extends DBFunction> extends DBSchemaObject {
    List<P> getProcedures();
    List<F> getFunctions();
    F getFunction(String name, int overload);
    P getProcedure(String name, int overload);
    DBMethod getMethod(String name, int overload);
    boolean isEmbedded();
}
