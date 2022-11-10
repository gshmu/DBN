package com.dci.intellij.dbn.execution.compiler;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CompileManagerAdapter implements CompileManagerListener {
    @Override
    public void compileFinished(@NotNull ConnectionHandler connection, @Nullable DBSchemaObject object) {

    }
}
