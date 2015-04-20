package com.dci.intellij.dbn.connection.config;

import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.util.FileUtil;
import com.dci.intellij.dbn.connection.Authentication;
import com.dci.intellij.dbn.connection.ConnectivityStatus;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.connection.config.ui.ConnectionDatabaseSettingsForm;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

public abstract class ConnectionDatabaseSettings<T extends ConnectionDatabaseSettingsForm> extends Configuration<T> {
    public static final Logger LOGGER = LoggerFactory.createLogger();

    private transient ConnectivityStatus connectivityStatus = ConnectivityStatus.UNKNOWN;
    protected boolean active = true;
    protected String name;
    protected String description;
    protected DatabaseType databaseType = DatabaseType.UNKNOWN;
    protected double databaseVersion = 9999;
    protected int hashCode;

    protected String driverLibrary;
    protected String driver;

    private Authentication authentication = new Authentication();

    private ConnectionSettings parent;

    public ConnectionDatabaseSettings(ConnectionSettings parent) {
        this.parent = parent;
    }

    public ConnectionSettings getParent() {
        return parent;
    }

    public ConnectivityStatus getConnectivityStatus() {
        return connectivityStatus;
    }

    public void setConnectivityStatus(ConnectivityStatus connectivityStatus) {
        this.connectivityStatus = connectivityStatus;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public String getDriverLibrary() {
        return driverLibrary;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriverLibrary(String driverLibrary) {
        this.driverLibrary = driverLibrary;
    }

    public void setDriver(String driver) {
        this.driver = driver;
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

    public double getDatabaseVersion() {
        return databaseVersion;
    }

    public void setDatabaseVersion(double databaseVersion) {
        this.databaseVersion = databaseVersion;
    }

    @NotNull
    public Authentication getAuthentication() {
        return authentication;
    }

    public String getConnectionDetails() {
        return "Name:\t"      + name + "\n" +
                (StringUtils.isNotEmpty(description) ? "Description:\t" + description + "\n" : "")+
               "User:\t"      + authentication.getUser();
    }

    @Override
    public String getConfigElementName() {
        return "database";
    }

    public abstract void updateHashCode();

    public abstract String getDatabaseUrl();

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public abstract ConnectionDatabaseSettings clone();

    @NotNull
    public String getConnectionId() {
        return parent.getConnectionId();
    }

    /*********************************************************
    *                 PersistentConfiguration               *
    *********************************************************/
    public void readConfiguration(Element element) {
        String connectionId = getString(element, "id", null);
        if (connectionId != null) {
            parent.setConnectionId(connectionId);
        }

        active           = getBoolean(element, "active", active);
        name             = getString(element, "name", name);
        description      = getString(element, "description", description);
        databaseType     = DatabaseType.get(getString(element, "database-type", databaseType.getName()));
        databaseVersion  = getDouble(element, "database-version", databaseVersion);

        driverLibrary = FileUtil.convertToAbsolutePath(getProject(), getString(element, "driver-library", driverLibrary));
        driver        = getString(element, "driver", driver);

        authentication.setUser(getString(element, "user", authentication.getUser()));
        authentication.setPassword(PasswordUtil.decodePassword(getString(element, "password", authentication.getPassword())));
        authentication.setOsAuthentication(getBoolean(element, "os-authentication", authentication.isOsAuthentication()));


        // TODO backward compatibility (to remove)
        Element propertiesElement = element.getChild("properties");
        if (propertiesElement != null) {
            for (Object o : propertiesElement.getChildren()) {
                Element propertyElement = (Element) o;
                Map<String, String> properties = getParent().getPropertiesSettings().getProperties();
                properties.put(
                        propertyElement.getAttributeValue("key"),
                        propertyElement.getAttributeValue("value"));
            }
        }
        updateHashCode();
    }

    public void writeConfiguration(Element element) {
        String driverLibrary = ConnectionBundleSettings.IS_IMPORT_EXPORT_ACTION.get() ?
                FileUtil.convertToRelativePath(getProject(), this.driverLibrary) :
                this.driverLibrary;

        setString(element, "driver-library", nvl(driverLibrary));
        setString(element, "driver", nvl(driver));

        setString(element, "name", nvl(name));
        setString(element, "description", nvl(description));
        setBoolean(element, "active", active);
        setBoolean(element, "os-authentication", authentication.isOsAuthentication());
        setString(element, "database-type", nvl(databaseType == null ? DatabaseType.UNKNOWN.getName() : databaseType.getName()));
        setDouble(element, "database-version", databaseVersion);
        setString(element, "user", nvl(authentication.getUser()));
        setString(element, "password", PasswordUtil.encodePassword(authentication.getPassword()));
    }

    public Project getProject() {
        return parent.getProject();
    }
}
