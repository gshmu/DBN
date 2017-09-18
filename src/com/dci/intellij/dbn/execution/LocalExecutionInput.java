package com.dci.intellij.dbn.execution;

import org.jdom.Element;

import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.intellij.openapi.project.Project;

public abstract class LocalExecutionInput extends ExecutionInput{
    private boolean usePoolConnection = false;
    private boolean commitAfterExecution = false;
    private boolean loggingEnabled = false;


    public LocalExecutionInput(Project project, ExecutionTarget executionTarget) {
        super(project, executionTarget);

        ConnectionHandler connectionHandler = getConnectionHandler();
        if (DatabaseFeature.DATABASE_LOGGING.isSupported(connectionHandler)) {
            loggingEnabled = FailsafeUtil.get(connectionHandler).isLoggingEnabled();
        }
    }

    public boolean isUsePoolConnection() {
        return usePoolConnection;
    }

    public void setUsePoolConnection(boolean usePoolConnection) {
        this.usePoolConnection = usePoolConnection;
    }

    public boolean isCommitAfterExecution() {
        return commitAfterExecution;
    }

    public void setCommitAfterExecution(boolean commitAfterExecution) {
        this.commitAfterExecution = commitAfterExecution;
    }

    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    public void setLoggingEnabled(boolean loggingEnabled) {
        this.loggingEnabled = loggingEnabled;
    }

    public abstract boolean hasExecutionVariables();

    public abstract boolean isSchemaSelectionAllowed();

    public abstract boolean isDatabaseLogProducer();

    /*********************************************************
     *                 PersistentConfiguration               *
     *********************************************************/
    public void readConfiguration(Element element) {
        super.readConfiguration(element);
        usePoolConnection = SettingsUtil.getBooleanAttribute(element, "use-pool-connection", true);
        commitAfterExecution = SettingsUtil.getBooleanAttribute(element, "commit-after-execution", true);
        loggingEnabled = SettingsUtil.getBooleanAttribute(element, "enable-logging", true);
    }

    public void writeConfiguration(Element element) {
        super.writeConfiguration(element);
        SettingsUtil.setBooleanAttribute(element, "use-pool-connection", usePoolConnection);
        SettingsUtil.setBooleanAttribute(element, "commit-after-execution", commitAfterExecution);
        SettingsUtil.setBooleanAttribute(element, "enable-logging", loggingEnabled);
    }
}
