package com.dci.intellij.dbn.connection.config;

import com.dci.intellij.dbn.common.options.ProjectConfiguration;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectivityStatus;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.connection.config.ui.GenericDatabaseSettingsForm;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class ConnectionDatabaseSettings extends ProjectConfiguration<GenericDatabaseSettingsForm>{
    private transient ConnectivityStatus connectivityStatus = ConnectivityStatus.UNKNOWN;
    protected boolean active = true;
    protected boolean osAuthentication = false;
    protected String id;
    protected String name;
    protected String description;
    protected DatabaseType databaseType;
    protected String user;
    protected String password;
    protected int hashCode;
    protected ConnectionBundle connectionBundle;

    private boolean isNew;

    public ConnectionDatabaseSettings(Project project, ConnectionBundle connectionBundle) {
        super(project);
        this.connectionBundle = connectionBundle;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    protected static String nvl(Object value) {
        return (String) (value == null ? "" : value);
    }

    public ConnectionBundle getConnectionBundle() {
        return connectionBundle;
    }

    public ConnectivityStatus getConnectivityStatus() {
        return connectivityStatus;
    }

    public void setConnectivityStatus(ConnectivityStatus connectivityStatus) {
        this.connectivityStatus = connectivityStatus;
    }

    public boolean isOsAuthentication() {
        return osAuthentication;
    }

    public void setOsAuthentication(boolean osAuthentication) {
        this.osAuthentication = osAuthentication;
        updateHashCode();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void generateNewId() {
        id =  UUID.randomUUID().toString();
    }

    @NotNull
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(DatabaseType databaseType) {
        this.databaseType = databaseType;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConnectionDetails() {
        return "Name:\t"      + name + "\n" +
               "Description:\t" + description + "\n" +
               "User:\t"      + user;
    }

    public abstract String getDriverLibrary();

    public abstract void updateHashCode();

    public abstract String getDriver();

    public abstract String getDatabaseUrl();

    @Override
    public int hashCode() {
        return hashCode;
    }

   /*********************************************************
    *                   JDOMExternalizable                 *
    *********************************************************/
    public void readConfiguration(Element element) {
        active = SettingsUtil.getBooleanAttribute(element, "active", active);
        osAuthentication = SettingsUtil.getBooleanAttribute(element, "os-authentication", osAuthentication);
        id = element.getAttributeValue("id");
        name = element.getAttributeValue("name");
        description = element.getAttributeValue("description");
        databaseType = DatabaseType.get(element.getAttributeValue("database-type"));
        user = element.getAttributeValue("user");
        password = element.getAttributeValue("password");

        updateHashCode();
    }

    public void writeConfiguration(Element element) {
        SettingsUtil.setBooleanAttribute(element, "active", active);
        SettingsUtil.setBooleanAttribute(element, "os-authentication", osAuthentication);
        element.setAttribute("id",             id);
        element.setAttribute("name",           nvl(name));
        element.setAttribute("description",    nvl(description));
        element.setAttribute("database-type",  nvl(databaseType == null ? DatabaseType.UNKNOWN.getName() : databaseType.getName()));
        element.setAttribute("user",           nvl(user));
        element.setAttribute("password",       nvl(password));
    }



}
