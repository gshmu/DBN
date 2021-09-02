package com.dci.intellij.dbn.execution;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.options.setting.SettingsSupport;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.SessionId;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.intellij.openapi.project.Project;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;

import static com.dci.intellij.dbn.execution.ExecutionOption.COMMIT_AFTER_EXECUTION;
import static com.dci.intellij.dbn.execution.ExecutionOption.ENABLE_LOGGING;

@Getter
@Setter
public abstract class LocalExecutionInput extends ExecutionInput{
    private ExecutionOptions options = new ExecutionOptions();
    private SessionId targetSessionId = SessionId.MAIN;

    public LocalExecutionInput(Project project, ExecutionTarget executionTarget) {
        super(project, executionTarget);

        ConnectionHandler connectionHandler = getConnectionHandler();
        if (connectionHandler != null) {
            if (DatabaseFeature.DATABASE_LOGGING.isSupported(connectionHandler)) {
                connectionHandler = Failsafe.nn(connectionHandler);
                options.set(ENABLE_LOGGING, connectionHandler.isLoggingEnabled());
            }
        }
    }

    public void setTargetSession(DatabaseSession databaseSession) {
        setTargetSessionId(databaseSession == null ? SessionId.MAIN : databaseSession.getId());
    }

    public abstract boolean hasExecutionVariables();

    public abstract boolean isSchemaSelectionAllowed();

    public abstract boolean isSessionSelectionAllowed();

    public abstract boolean isDatabaseLogProducer();

    @Override
    public ConnectionId getConnectionHandlerId() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        return connectionHandler == null ? null : connectionHandler.getConnectionId();
    }

    /*********************************************************
     *                 PersistentConfiguration               *
     *********************************************************/
    @Override
    public void readConfiguration(Element element) {
        super.readConfiguration(element);
        targetSessionId = CommonUtil.nvl(SessionId.get(element.getAttributeValue("session-id")), targetSessionId);
        options.set(ENABLE_LOGGING, SettingsSupport.getBooleanAttribute(element, "enable-logging", true));
        options.set(COMMIT_AFTER_EXECUTION, SettingsSupport.getBooleanAttribute(element, "commit-after-execution", true));
    }

    @Override
    public void writeConfiguration(Element element) {
        super.writeConfiguration(element);
        element.setAttribute("session-id", targetSessionId.id());
        SettingsSupport.setBooleanAttribute(element, "enable-logging", options.is(ENABLE_LOGGING));
        SettingsSupport.setBooleanAttribute(element, "commit-after-execution", options.is(COMMIT_AFTER_EXECUTION));
    }
}
