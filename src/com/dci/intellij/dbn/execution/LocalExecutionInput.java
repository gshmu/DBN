package com.dci.intellij.dbn.execution;

import org.jdom.Element;

import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.intellij.openapi.project.Project;

public abstract class LocalExecutionInput extends ExecutionInput{
    private ExecutionOptions options = new ExecutionOptions();
    private DatabaseSession session;

    public LocalExecutionInput(Project project, ExecutionTarget executionTarget) {
        super(project, executionTarget);

        ConnectionHandler connectionHandler = getConnectionHandler();
        if (DatabaseFeature.DATABASE_LOGGING.isSupported(connectionHandler)) {
            connectionHandler = FailsafeUtil.get(connectionHandler);
            options.setEnableLogging(connectionHandler.isLoggingEnabled());
            session = connectionHandler.getSessionBundle().MAIN;
        }
    }

    public DatabaseSession getSession() {
        return session;
    }

    public void setSession(DatabaseSession session) {
        this.session = session;
    }

    public ExecutionOptions getOptions() {
        return options;
    }

    public void setOptions(ExecutionOptions options) {
        this.options = options;
    }

    public abstract boolean hasExecutionVariables();

    public abstract boolean isSchemaSelectionAllowed();

    public abstract boolean isSessionSelectionAllowed();

    public abstract boolean isDatabaseLogProducer();

    /*********************************************************
     *                 PersistentConfiguration               *
     *********************************************************/
    public void readConfiguration(Element element) {
        super.readConfiguration(element);
        //sessionId = CommonUtil.nvl(SessionId.get(element.getAttributeValue("session-id")), sessionId);
        options.setEnableLogging(SettingsUtil.getBooleanAttribute(element, "enable-logging", true));
        options.setCommitAfterExecution(SettingsUtil.getBooleanAttribute(element, "commit-after-execution", true));
    }

    public void writeConfiguration(Element element) {
        super.writeConfiguration(element);
        //element.setAttribute("session-id", sessionId.id());
        SettingsUtil.setBooleanAttribute(element, "enable-logging", options.isEnableLogging());
        SettingsUtil.setBooleanAttribute(element, "commit-after-execution", options.isCommitAfterExecution());
    }
}
