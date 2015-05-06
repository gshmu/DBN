package com.dci.intellij.dbn.execution.logging.ui;

import java.io.StringReader;
import java.util.Date;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.locale.Formatter;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.logging.LogOutputRequest;
import com.intellij.diagnostic.logging.DefaultLogFilterModel;
import com.intellij.diagnostic.logging.LogConsoleBase;
import com.intellij.diagnostic.logging.LogFilterModel;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.vfs.VirtualFile;

public class DatabaseLogOutputConsole extends LogConsoleBase{
    public static final StringReader EMPTY_READER = new StringReader("");
    public DatabaseLogOutputConsole(@NotNull ConnectionHandler connectionHandler, String title, boolean buildInActions) {
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

    public void writeToConsole(LogOutputRequest request) {
        ConnectionHandler connectionHandler = request.getConnectionHandler();
        if (request.isAddHeadline()) {
            Formatter formatter = Formatter.getInstance(connectionHandler.getProject());
            String date = formatter.formatDateTime(new Date());

            VirtualFile sourceFile = request.getSourceFile();
            String headline = connectionHandler.getName() + (sourceFile == null ? "" : " / " + sourceFile.getName()) + " - " + date + "\n";
            ConsoleView console = getConsole();
            if (console != null && console.getContentSize() > 0) {
                headline = "__________________________________________________\n" + headline;
            }
            writeToConsole(headline, ProcessOutputTypes.SYSTEM);
        }
        String text = request.getText();
        if (!request.isHideEmptyLines() || StringUtil.isNotEmptyOrSpaces(text)) {
            writeToConsole(text + "\n", ProcessOutputTypes.STDOUT);
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
