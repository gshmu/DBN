package com.dci.intellij.dbn.connection.config;

import com.dci.intellij.dbn.connection.Authentication;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.connection.DatabaseUrlResolver;
import com.dci.intellij.dbn.connection.config.ui.GuidedDatabaseSettingsForm;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class GuidedDatabaseSettings extends ConnectionDatabaseSettings {
    private String host;
    private String port;
    private String database;

    public GuidedDatabaseSettings(ConnectionSettings connectionSettings, DatabaseType databaseType) {
        super(connectionSettings);
        setDatabaseType(databaseType);
        DatabaseUrlResolver urlResolver = databaseType.getUrlResolver();
        setHost(urlResolver.getDefaultHost());
        setPort(urlResolver.getDefaultPort());
        setDatabase(urlResolver.getDefaultDatabase());
    }

    @NotNull
    public GuidedDatabaseSettingsForm createConfigurationEditor() {
        return new GuidedDatabaseSettingsForm(this);
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
        host          = getString(element, "host", host);
        port          = getString(element, "port", port);
        database      = getString(element, "database", database);
    }

    public void writeConfiguration(Element element) {
        super.writeConfiguration(element);
        setString(element, "host", host);
        setString(element, "port", port);
        setString(element, "database", database);
    }
}
