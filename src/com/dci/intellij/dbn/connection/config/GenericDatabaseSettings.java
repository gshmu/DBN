package com.dci.intellij.dbn.connection.config;

import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.connection.Authentication;
import com.dci.intellij.dbn.connection.config.ui.GenericDatabaseSettingsForm;

public class GenericDatabaseSettings extends ConnectionDatabaseSettings {
    protected String connectionUrl;

    public GenericDatabaseSettings(ConnectionSettings connectionSettings) {
        super(connectionSettings);
    }

    @NotNull
    public GenericDatabaseSettingsForm createConfigurationEditor() {
        return new GenericDatabaseSettingsForm(this);
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    @Override
    public String getTunnelledConnectionUrl() {
        ConnectionSshSslSettings sshSslSettings = getParent().getSshSslSettings();
        if (sshSslSettings.isActive()) {

        }
        return connectionUrl;
    }

    @Override
    public String getHost() {
        return getDatabaseType().resolveHost(connectionUrl);
    }

    @Override
    public String getPort() {
        return getDatabaseType().resolvePort(connectionUrl);
    }

    @Override
    public String getDatabase() {
        return getDatabaseType().resolveDatabase(connectionUrl);
    }

    public void setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }

    public void updateHashCode() {
        Authentication authentication = getAuthentication();
        hashCode = (name + getDriver() + getDriverLibrary() + connectionUrl + authentication.getUser() + authentication.getPassword() + authentication.isOsAuthentication()).hashCode();
    }

    public GenericDatabaseSettings clone() {
        Element connectionElement = new Element(getConfigElementName());
        writeConfiguration(connectionElement);
        GenericDatabaseSettings clone = new GenericDatabaseSettings(getParent());
        clone.readConfiguration(connectionElement);
        clone.setConnectivityStatus(getConnectivityStatus());
        return clone;
    }

    public String getConnectionDetails() {
        return "Name:\t"      + name + "\n" +
                (StringUtils.isNotEmpty(description) ? "Description:\t" + description + "\n" : "")+
               "URL:\t"       + connectionUrl + "\n" +
               "User:\t"      + getAuthentication().getUser();
    }

   /*********************************************************
    *                PersistentConfiguration                *
    *********************************************************/
    public void readConfiguration(Element element) {
        super.readConfiguration(element);
        connectionUrl = getString(element, "url", connectionUrl);
    }

    public void writeConfiguration(Element element) {
        super.writeConfiguration(element);
        setString(element, "url", nvl(connectionUrl));
    }
}
