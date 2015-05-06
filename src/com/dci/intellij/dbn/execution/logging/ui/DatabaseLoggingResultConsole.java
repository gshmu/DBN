package com.dci.intellij.dbn.execution.logging.ui;

import java.io.StringReader;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.logging.LogOutput;
import com.dci.intellij.dbn.execution.logging.LogOutputContext;
import com.intellij.diagnostic.logging.DefaultLogFilterModel;
import com.intellij.diagnostic.logging.LogConsoleBase;
import com.intellij.diagnostic.logging.LogFilterModel;
import com.intellij.openapi.actionSystem.ActionGroup;

public class DatabaseLoggingResultConsole extends LogConsoleBase{
    public static final StringReader EMPTY_READER = new StringReader("");
    public DatabaseLoggingResultConsole(@NotNull ConnectionHandler connectionHandler, String title, boolean buildInActions) {
        super(connectionHandler.getProject(), EMPTY_READER, title, buildInActions, createFilterModel(connectionHandler));
    }

    private static LogFilterModel createFilterModel(ConnectionHandler connectionHandler) {
        DefaultLogFilterModel defaultLogFilterModel = new DefaultLogFilterModel(connectionHandler.getProject());
        defaultLogFilterModel.setCheckStandartFilters(false);
        return defaultLogFilterModel;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    public void writeToConsole(LogOutputContext context, LogOutput output) {
        String text = output.getText();
        if (!context.isHideEmptyLines() || StringUtil.isNotEmptyOrSpaces(text)) {
            writeToConsole(text + "\n", output.getType().getKey());
        }
    }

    @Override
    public ActionGroup getOrCreateActions() {
        return super.getOrCreateActions();
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
