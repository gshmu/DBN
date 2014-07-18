package com.dci.intellij.dbn.object;

import java.util.List;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.object.common.DBSchemaObject;

public interface DBMethod extends DBSchemaObject {
    List<DBArgument> getArguments();
    DBArgument getArgument(String name);
    DBProgram getProgram();
    String getMethodType();
    boolean isProgramMethod();
    boolean isDeterministic();
    boolean hasDeclaredArguments();
    @NotNull
    DBLanguage getLanguage();
}
