package com.dci.intellij.dbn.execution.logging.ui;

import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.common.util.Unsafe;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.logging.LogOutput;
import com.dci.intellij.dbn.execution.logging.LogOutputContext;
import com.intellij.diagnostic.logging.DefaultLogFilterModel;
import com.intellij.diagnostic.logging.LogConsoleBase;
import com.intellij.diagnostic.logging.LogFilterModel;
import com.intellij.openapi.actionSystem.ActionGroup;
import org.jetbrains.annotations.NotNull;

import java.io.StringReader;

public class DatabaseLoggingResultConsole extends LogConsoleBase{
    public static final StringReader EMPTY_READER = new StringReader("");
    public DatabaseLoggingResultConsole(@NotNull ConnectionHandler connection, String title, boolean buildInActions) {
        super(connection.getProject(), EMPTY_READER, title, buildInActions, createFilterModel(connection));
    }

    private static LogFilterModel createFilterModel(ConnectionHandler connection) {
        DefaultLogFilterModel defaultLogFilterModel = new DefaultLogFilterModel(connection.getProject());
        defaultLogFilterModel.setCheckStandartFilters(false);
        return defaultLogFilterModel;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    public void writeToConsole(LogOutputContext context, LogOutput output) {
        String text = output.getText();
        boolean isEmpty = Strings.isEmptyOrSpaces(text);
        boolean hideEmptyLines = context.isHideEmptyLines();

        if (!hideEmptyLines || !isEmpty) {
            writeToConsole(text + '\n', output.getType().getKey());
        }
    }

    @Override
    public ActionGroup getOrCreateActions() {
        return super.getOrCreateActions();
    }

    @Override
    public void dispose() {
        Unsafe.silent(() -> super.dispose());
    }
}
