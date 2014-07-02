package com.dci.intellij.dbn.object;

import com.dci.intellij.dbn.object.common.DBSchemaObject;

import java.util.List;

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
