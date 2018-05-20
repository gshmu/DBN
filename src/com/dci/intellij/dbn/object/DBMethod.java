package com.dci.intellij.dbn.object;

import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface DBMethod extends DBSchemaObject {
    List<DBArgument> getArguments();
    DBArgument getArgument(String name);
    DBArgument getReturnArgument();
    DBProgram getProgram();
    String getMethodType();

    int getPosition();

    boolean isProgramMethod();
    boolean isDeterministic();
    boolean hasDeclaredArguments();
    @NotNull
    DBLanguage getLanguage();
}
