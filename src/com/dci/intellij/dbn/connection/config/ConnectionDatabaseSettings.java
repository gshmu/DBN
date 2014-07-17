package com.dci.intellij.dbn.connection.config;

import java.util.UUID;

import com.dci.intellij.dbn.common.util.StringUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectivityStatus;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.connection.config.ui.GenericDatabaseSettingsForm;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.Base64Converter;

public abstract class ConnectionDatabaseSettings extends Configuration<GenericDatabaseSettingsForm> {
    public static final Logger LOGGER = LoggerFactory.createLogger();

    private transient ConnectivityStatus connectivityStatus = ConnectivityStatus.UNKNOWN;
    protected boolean active = true;
    protected boolean osAuthentication = false;
    protected String id;
    protected String name;
    protected String description;
    protected DatabaseType databaseType = DatabaseType.UNKNOWN;
    protected String user;
    protected String password;
    protected int hashCode;
    protected ConnectionBundle connectionBundle;
    private ConnectionSettings parent;

    private boolean isNew;

    public ConnectionDatabaseSettings(ConnectionBundle connectionBundle, ConnectionSettings parent) {
        this.connectionBundle = connectionBundle;
        this.parent = parent;
    }

    public ConnectionSettings getParent() {
        return parent;
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

    @Override
    public String getConfigElementName() {
        return "database";
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
        if (element.getName().equals(getConfigElementName())) {
            id               = getString(element, "id", id);
            name             = getString(element, "name", name);
            description      = getString(element, "description", description);
            databaseType     = DatabaseType.get(getString(element, "database-type", databaseType.getName()));
            user             = getString(element, "user", user);
            password         = decodePassword(getString(element, "password", password));
            active           = getBoolean(element, "active", active);
            osAuthentication = getBoolean(element, "os-authentication", osAuthentication);
        } else{
            // TODO: decommission (support old configuration)
            active = getBooleanAttribute(element, "active", active);
            osAuthentication = getBooleanAttribute(element, "os-authentication", osAuthentication);
            id = element.getAttributeValue("id");
            name = element.getAttributeValue("name");
            description = element.getAttributeValue("description");
            databaseType = DatabaseType.get(element.getAttributeValue("database-type"));
            user = element.getAttributeValue("user");
            password = decodePassword(element.getAttributeValue("password"));
        }

        updateHashCode();
    }

    public void writeConfiguration(Element element) {
        setString(element, "id", id);
        setString(element, "name", nvl(name));
        setString(element, "description", nvl(description));
        setBoolean(element, "active", active);
        setBoolean(element, "os-authentication", osAuthentication);
        setString(element, "database-type", nvl(databaseType == null ? DatabaseType.UNKNOWN.getName() : databaseType.getName()));
        setString(element, "user", nvl(user));
        setString(element, "password", encodePassword(password));
    }

    private String encodePassword(String password) {
        try {
            password = StringUtil.isEmpty(password) ? "" : Base64Converter.encode(nvl(password));
        } catch (Exception e) {
            // any exception would break the logic storing the connection settings
            LOGGER.error("Error encoding password", e);
        }
        return password;
    }

    private String decodePassword(String password) {
        try {
            password = StringUtil.isEmpty(password) ? "" : Base64Converter.decode(nvl(password));
        } catch (Exception e) {
            // password may not be encoded yet
        }

        return password;
    }

    public Project getProject() {
        return parent.getProject();
    }
}
