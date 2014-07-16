package com.dci.intellij.dbn.object;

import java.util.List;

import com.dci.intellij.dbn.object.common.DBSchemaObject;

public interface DBMethod extends DBSchemaObject {
    List<DBArgument> getArguments();
    DBArgument getArgument(String name);
    DBProgram getProgram();
    String getMethodType();
    int getOverload();
    boolean isProgramMethod();
    boolean isDeterministic();
    boolean hasDeclaredArguments();
}
