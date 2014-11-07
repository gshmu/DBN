package com.dci.intellij.dbn.execution.common.output.ui;

import java.io.Reader;

import com.intellij.diagnostic.logging.DefaultLogFilterModel;
import com.intellij.diagnostic.logging.LogConsoleBase;
import com.intellij.openapi.project.Project;

public class ExecutionLogOutputConsole extends LogConsoleBase{
    public ExecutionLogOutputConsole(Project project, Reader reader, String title) {
        super(project, reader, title, true, new DefaultLogFilterModel(project));
    }

    @Override
    public boolean isActive() {
        return true;
    }


}
