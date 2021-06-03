package com.dci.intellij.dbn.debugger.common.config.target;

import com.dci.intellij.dbn.common.compatibility.Compatibility;
import com.dci.intellij.dbn.debugger.common.config.DBRunConfig;
import com.intellij.execution.ExecutionTarget;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class DBExecutionTarget extends ExecutionTarget{
    public static DBExecutionTarget INSTANCE = new DBExecutionTarget();
    private static List<ExecutionTarget> LIST = new ArrayList<ExecutionTarget>();
    static {
        LIST.add(INSTANCE);
    }

    public static List<ExecutionTarget> asList() {
        return new ArrayList<ExecutionTarget>(LIST);
    }

    private DBExecutionTarget() {
    }

    @NotNull
    @Override
    public String getId() {
        return "DBN_EXECUTION_TARGET";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Database";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    @Compatibility
    public boolean canRun(@NotNull RunnerAndConfigurationSettings configuration) {
        return canRun(configuration.getConfiguration());
    }

    @Override
    public boolean canRun(@NotNull RunConfiguration configuration) {
        return configuration instanceof DBRunConfig ;
    }
}
