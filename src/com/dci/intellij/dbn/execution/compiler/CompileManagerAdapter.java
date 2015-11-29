package com.dci.intellij.dbn.execution.compiler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.common.DBSchemaObject;

public abstract class CompileManagerAdapter implements CompileManagerListener {
    @Override
    public void compileFinished(@NotNull ConnectionHandler connectionHandler, @Nullable DBSchemaObject object) {

    }
}
