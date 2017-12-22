package com.dci.intellij.dbn.connection.config;

import java.nio.charset.Charset;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.environment.EnvironmentTypeId;
import com.dci.intellij.dbn.common.environment.options.EnvironmentSettings;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.config.ui.ConnectionDetailSettingsForm;
import com.dci.intellij.dbn.options.general.GeneralProjectSettings;
import com.intellij.openapi.project.Project;

public class ConnectionDetailSettings extends Configuration<ConnectionDetailSettingsForm> {
    private Charset charset = Charset.forName("UTF-8");
    private EnvironmentTypeId environmentTypeId = EnvironmentTypeId.DEFAULT;
    private boolean enableSessionManagement = true;
    private boolean enableDdlFileBinding = true;
    private boolean enableDatabaseLogging = false;
    private boolean connectAutomatically = true;
    private boolean restoreWorkspace = true;
    private boolean restoreWorkspaceDeep = true;
    private int idleTimeToDisconnect = 30;
    private int passwordExpiryTime = 10;
    private int maxConnectionPoolSize = 7;
    private String alternativeStatementDelimiter;
    private ConnectionSettings parent;

    public ConnectionDetailSettings(ConnectionSettings parent) {
        this.parent = parent;
    }

    public String getDisplayName() {
        return "Connection Detail Settings";
    }

    public String getHelpTopic() {
        return "connectionPropertySettings";
    }

    /*********************************************************
     *                        Custom                         *
     *********************************************************/

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    @NotNull
    public EnvironmentType getEnvironmentType() {
        EnvironmentSettings environmentSettings = GeneralProjectSettings.getInstance(getProject()).getEnvironmentSettings();
        return environmentSettings.getEnvironmentType(environmentTypeId);
    }

    public void setEnvironmentTypeId(EnvironmentTypeId environmentTypeId) {
        this.environmentTypeId = environmentTypeId;
    }

    public EnvironmentTypeId getEnvironmentTypeId() {
        return environmentTypeId;
    }

    public boolean isEnableSessionManagement() {
        return enableSessionManagement;
    }

    public void setEnableSessionManagement(boolean enableSessionManagement) {
        this.enableSessionManagement = enableSessionManagement;
    }

    public boolean isEnableDdlFileBinding() {
        return enableDdlFileBinding;
    }

    public void setEnableDdlFileBinding(boolean enableDdlFileBinding) {
        this.enableDdlFileBinding = enableDdlFileBinding;
    }

    public boolean isEnableDatabaseLogging() {
        return enableDatabaseLogging;
    }

    public void setEnableDatabaseLogging(boolean enableDatabaseLogging) {
        this.enableDatabaseLogging = enableDatabaseLogging;
    }

    public boolean isRestoreWorkspace() {
        return restoreWorkspace;
    }

    public void setRestoreWorkspace(boolean restoreWorkspace) {
        this.restoreWorkspace = restoreWorkspace;
    }

    public boolean isRestoreWorkspaceDeep() {
        return restoreWorkspace && restoreWorkspaceDeep;
    }

    public void setRestoreWorkspaceDeep(boolean restoreWorkspaceDeep) {
        this.restoreWorkspaceDeep = restoreWorkspaceDeep;
    }

    public boolean isConnectAutomatically() {
        return connectAutomatically;
    }

    public void setConnectAutomatically(boolean connectAutomatically) {
        this.connectAutomatically = connectAutomatically;
    }

    public int getMaxConnectionPoolSize() {
        return maxConnectionPoolSize;
    }

    public void setMaxConnectionPoolSize(int maxConnectionPoolSize) {
        this.maxConnectionPoolSize = maxConnectionPoolSize;
    }

    public int getIdleTimeToDisconnect() {
        return idleTimeToDisconnect;
    }

    public void setIdleTimeToDisconnect(int idleTimeToDisconnect) {
        this.idleTimeToDisconnect = idleTimeToDisconnect;
    }

    public int getPasswordExpiryTime() {
        return passwordExpiryTime;
    }

    public void setPasswordExpiryTime(int passwordExpiryTime) {
        this.passwordExpiryTime = passwordExpiryTime;
    }

    public String getAlternativeStatementDelimiter() {
        return alternativeStatementDelimiter;
    }

    public void setAlternativeStatementDelimiter(String alternativeStatementDelimiter) {
        this.alternativeStatementDelimiter = alternativeStatementDelimiter;
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/
    @NotNull
    @Override
    public ConnectionDetailSettingsForm createConfigurationEditor() {
        return new ConnectionDetailSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "details";
    }

    @Override
    public void readConfiguration(Element element) {
        String charsetName = getString(element, "charset", "UTF-8");
        charset = Charset.forName(charsetName);

        enableSessionManagement = getBoolean(element, "session-management", enableSessionManagement);
        enableDdlFileBinding = getBoolean(element, "ddl-file-binding", enableDdlFileBinding);
        enableDatabaseLogging = getBoolean(element, "database-logging", enableDatabaseLogging);
        connectAutomatically = getBoolean(element, "connect-automatically", connectAutomatically);
        restoreWorkspace = getBoolean(element, "restore-workspace", restoreWorkspace);
        restoreWorkspaceDeep = getBoolean(element, "restore-workspace-deep", restoreWorkspaceDeep);
        environmentTypeId = EnvironmentTypeId.get(getString(element, "environment-type", EnvironmentTypeId.DEFAULT.id()));
        idleTimeToDisconnect = getInteger(element, "idle-time-to-disconnect", idleTimeToDisconnect);
        passwordExpiryTime = getInteger(element, "password-expiry-time", passwordExpiryTime);
        maxConnectionPoolSize = getInteger(element, "max-connection-pool-size", maxConnectionPoolSize);
        alternativeStatementDelimiter = getString(element, "alternative-statement-delimiter", null);
    }

    @Override
    public void writeConfiguration(Element element) {
        setString(element, "charset", charset.name());
        
        setBoolean(element, "session-management", enableSessionManagement);
        setBoolean(element, "ddl-file-binding", enableDdlFileBinding);
        setBoolean(element, "database-logging", enableDatabaseLogging);
        setBoolean(element, "connect-automatically", connectAutomatically);
        setBoolean(element, "restore-workspace", restoreWorkspace);
        setBoolean(element, "restore-workspace-deep", restoreWorkspaceDeep);
        setString(element, "environment-type", environmentTypeId.id());
        setInteger(element, "idle-time-to-disconnect", idleTimeToDisconnect);
        setInteger(element, "password-expiry-time", passwordExpiryTime);
        setInteger(element, "max-connection-pool-size", maxConnectionPoolSize);
        setString(element, "alternative-statement-delimiter", CommonUtil.nvl(alternativeStatementDelimiter, ""));
    }

    public Project getProject() {
        return parent.getProject();
    }

    public ConnectionId getConnectionId() {
        return parent.getConnectionId();
    }
}
