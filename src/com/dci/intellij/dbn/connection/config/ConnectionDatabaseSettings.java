package com.dci.intellij.dbn.connection.config;

import java.util.HashMap;
import java.util.Map;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.Authentication;
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
    protected String name;
    protected String description;
    protected DatabaseType databaseType = DatabaseType.UNKNOWN;
    protected double databaseVersion = 9999;
    protected int hashCode;
    private Authentication authentication = new Authentication();
    private Map<String, String> properties = new HashMap<String, String>();

    private ConnectionSettings parent;

    public ConnectionDatabaseSettings(ConnectionSettings parent) {
        this.parent = parent;
    }

    public ConnectionSettings getParent() {
        return parent;
    }

    protected static String nvl(Object value) {
        return (String) (value == null ? "" : value);
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

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }


    public String getConnectionDetails() {
        return "Name:\t"      + name + "\n" +
               "Description:\t" + CommonUtil.nvl(description, "") + "\n" +
               "User:\t"      + authentication.getUser();
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
        authentication.setUser(getString(element, "user", authentication.getUser()));
        authentication.setPassword(decodePassword(getString(element, "password", authentication.getPassword())));
        authentication.setOsAuthentication(getBoolean(element, "os-authentication", authentication.isOsAuthentication()));

        Element propertiesElement = element.getChild("properties");
        if (propertiesElement != null) {
            for (Object o : propertiesElement.getChildren()) {
                Element propertyElement = (Element) o;
                properties.put(
                        propertyElement.getAttributeValue("key"),
                        propertyElement.getAttributeValue("value"));
            }
        }
        updateHashCode();
    }

    public void writeConfiguration(Element element) {
        setString(element, "name", nvl(name));
        setString(element, "description", nvl(description));
        setBoolean(element, "active", active);
        setBoolean(element, "os-authentication", authentication.isOsAuthentication());
        setString(element, "database-type", nvl(databaseType == null ? DatabaseType.UNKNOWN.getName() : databaseType.getName()));
        setDouble(element, "database-version", databaseVersion);
        setString(element, "user", nvl(authentication.getUser()));
        setString(element, "password", encodePassword(authentication.getPassword()));

        if (properties.size() > 0) {
            Element propertiesElement = new Element("properties");
            for (String propertyKey : properties.keySet()) {
                Element propertyElement = new Element("property");
                propertyElement.setAttribute("key", propertyKey);
                propertyElement.setAttribute("value", CommonUtil.nvl(properties.get(propertyKey), ""));

                propertiesElement.addContent(propertyElement);
            }
            element.addContent(propertiesElement);
        }
    }

    private static String encodePassword(String password) {
        try {
            password = StringUtil.isEmpty(password) ? "" : Base64Converter.encode(nvl(password));
        } catch (Exception e) {
            // any exception would break the logic storing the connection settings
            LOGGER.error("Error encoding password", e);
        }
        return password;
    }

    private static String decodePassword(String password) {
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
