package com.dci.intellij.dbn.connection.config;

import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.connection.Authentication;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.connection.DatabaseUrlResolver;
import com.dci.intellij.dbn.connection.config.ui.ConnectionDatabaseSettingsForm;
import com.dci.intellij.dbn.driver.DriverSource;

@Deprecated
public class GuidedDatabaseSettings extends ConnectionDatabaseSettings {
    private String host;
    private String port;
    private String database;

    public GuidedDatabaseSettings(ConnectionSettings connectionSettings, DatabaseType databaseType) {
        super(connectionSettings);
        setDriverSource(DriverSource.BUILTIN);
        setDatabaseType(databaseType);
        DatabaseUrlResolver urlResolver = databaseType.getUrlResolver();
        setHost(urlResolver.getDefaultHost());
        setPort(urlResolver.getDefaultPort());
        setDatabase(urlResolver.getDefaultDatabase());
    }

    @NotNull
    public ConnectionDatabaseSettingsForm createConfigurationEditor() {
        return new ConnectionDatabaseSettingsForm(this);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    @Override
    public String getDriver() {
        if (driverSource == DriverSource.BUILTIN) {
            return databaseType.getDriverClassName();
        } else {
            return super.getDriver();
        }
    }

    public String getConnectionUrl() {
        return databaseType.getUrlResolver().getUrl(host, port, database);
    }

    @Override
    public String getConnectionUrl(String host, String port) {
        ConnectionSshTunnelSettings sshSslSettings = getParent().getSshTunnelSettings();
        if (sshSslSettings.isActive()) {
            return databaseType.getUrlResolver().getUrl(
                    sshSslSettings.getHost(),
                    sshSslSettings.getPort(),
                    database);
        }
        return getConnectionUrl();
    }

    public void updateHashCode() {
        Authentication authentication = getAuthentication();
        hashCode = (name + driver + driverLibrary + host + port + database + authentication.getUser() + authentication.getPassword() + authentication.isOsAuthentication()).hashCode();
    }

    public GuidedDatabaseSettings clone() {
        Element connectionElement = new Element(getConfigElementName());
        writeConfiguration(connectionElement);
        GuidedDatabaseSettings clone = new GuidedDatabaseSettings(getParent(), getDatabaseType());
        clone.readConfiguration(connectionElement);
        clone.setConnectivityStatus(getConnectivityStatus());
        return clone;
    }

    public String getConnectionDetails() {
        return "Name:\t"      + name + "\n" +
                (StringUtils.isNotEmpty(description) ? "Description:\t" + description + "\n" : "")+
               "Host:\t"       + host + "\n" +
               "Port:\t"       + port + "\n" +
               "Database:\t"   + database + "\n" +
               "User:\t"      + getAuthentication().getUser();
    }

   /*********************************************************
    *                PersistentConfiguration                *
    *********************************************************/
    public void readConfiguration(Element element) {
        super.readConfiguration(element);
        host             = getString(element, "host", host);
        port             = getString(element, "port", port);
        database         = getString(element, "database", database);
        driverSource = getEnum(element, "driver-lookup", DriverSource.BUILTIN);
    }

    public void writeConfiguration(Element element) {
        super.writeConfiguration(element);
        setString(element, "host", host);
        setString(element, "port", port);
        setString(element, "database", database);
        setEnum(element, "driver-lookup", driverSource);
    }
}
