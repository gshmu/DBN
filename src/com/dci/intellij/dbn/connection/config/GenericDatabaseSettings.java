package com.dci.intellij.dbn.connection.config;

import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.connection.Authentication;
import com.dci.intellij.dbn.connection.config.ui.GenericDatabaseSettingsForm;

public class GenericDatabaseSettings extends ConnectionDatabaseSettings {
    protected String databaseUrl;

    public GenericDatabaseSettings(ConnectionSettings connectionSettings) {
        super(connectionSettings);
    }

    @NotNull
    public GenericDatabaseSettingsForm createConfigurationEditor() {
        return new GenericDatabaseSettingsForm(this);
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public void updateHashCode() {
        Authentication authentication = getAuthentication();
        hashCode = (name + getDriver() + getDriverLibrary() + databaseUrl + authentication.getUser() + authentication.getPassword() + authentication.isOsAuthentication()).hashCode();
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
               "URL:\t"       + databaseUrl + "\n" +
               "User:\t"      + getAuthentication().getUser();
    }

   /*********************************************************
    *                PersistentConfiguration                *
    *********************************************************/
    public void readConfiguration(Element element) {
        super.readConfiguration(element);
        databaseUrl   = getString(element, "url", databaseUrl);
    }

    public void writeConfiguration(Element element) {
        super.writeConfiguration(element);
        setString(element, "url", nvl(databaseUrl));
    }
}
