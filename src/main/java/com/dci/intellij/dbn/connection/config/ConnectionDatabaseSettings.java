package com.dci.intellij.dbn.connection.config;

import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.database.DatabaseInfo;
import com.dci.intellij.dbn.common.options.BasicConfiguration;
import com.dci.intellij.dbn.common.util.Files;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.connection.*;
import com.dci.intellij.dbn.connection.config.file.DatabaseFiles;
import com.dci.intellij.dbn.connection.config.ui.ConnectionDatabaseSettingsForm;
import com.dci.intellij.dbn.driver.DriverSource;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.*;

@Slf4j
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class ConnectionDatabaseSettings extends BasicConfiguration<ConnectionSettings, ConnectionDatabaseSettingsForm> {

    private String name;
    private String description;
    private DatabaseType databaseType;
    private DatabaseType derivedDatabaseType = DatabaseType.GENERIC;
    private DatabaseType confirmedDatabaseType = DatabaseType.GENERIC;
    private DatabaseUrlPattern urlPattern;
    private double databaseVersion = 9999;

    private final DatabaseInfo databaseInfo;
    private DriverSource driverSource;
    private String driverLibrary;
    private String driver;

    private ConnectionConfigType configType;
    private final AuthenticationInfo authenticationInfo = new AuthenticationInfo(this, false);

    private transient ConnectivityStatus connectivityStatus = ConnectivityStatus.UNKNOWN;
    private transient long signature = 0;

    public ConnectionDatabaseSettings(ConnectionSettings parent, @NotNull DatabaseType databaseType, ConnectionConfigType configType) {
        super(parent);
        this.databaseType = databaseType;
        this.configType = configType;
        this.urlPattern = databaseType.getDefaultUrlPattern();
        this.databaseInfo = urlPattern.getDefaultInfo();
        this.driverSource = databaseType == DatabaseType.GENERIC ?
                DriverSource.EXTERNAL :
                DriverSource.BUNDLED;

        initAuthType(databaseType);
    }

    private void initAuthType(DatabaseType databaseType) {
        AuthenticationType authenticationType = AuthenticationType.USER_PASSWORD;
        AuthenticationType[] authTypes = databaseType.getAuthTypes();
        if (!authenticationType.isOneOf(authTypes)) {
            authenticationType = authTypes[0];
        }
        authenticationInfo.setType(authenticationType);
    }

    private void deriveDatabaseType() {
        String driver = getDriver();
        derivedDatabaseType = databaseType;
        confirmedDatabaseType = databaseType;
        if (databaseType == DatabaseType.GENERIC && Strings.isNotEmptyOrSpaces(driver)) {
            derivedDatabaseType = DatabaseType.derive(driver);
            confirmedDatabaseType = DatabaseType.resolve(driver);
        }
    }

    @Override
    @NotNull
    public ConnectionDatabaseSettingsForm createConfigurationEditor() {
        return new ConnectionDatabaseSettingsForm(this);
    }

    public String getName() {
        return nvl(name);
    }

    public String getDriver() {
        return driverSource == DriverSource.BUNDLED ? databaseType.getDriverClassName() : driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
        deriveDatabaseType();
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    public void setDatabaseType(DatabaseType databaseType) {
        if (this.databaseType != databaseType) {
            this.databaseType = databaseType;
            urlPattern = databaseType.getDefaultUrlPattern();
            databaseInfo.setUrlType(urlPattern.getUrlType());
            deriveDatabaseType();
        }
        initAuthType(databaseType);
    }

    public void setConfirmedDatabaseType(DatabaseType confirmedDatabaseType) {
        this.confirmedDatabaseType = confirmedDatabaseType;
        this.derivedDatabaseType = confirmedDatabaseType;
    }

    @Override
    public String getConfigElementName() {
        return "database";
    }

    public boolean isDatabaseInitialized() {
        DatabaseInfo databaseInfo = getDatabaseInfo();
        if (databaseInfo.getUrlType() == DatabaseUrlType.FILE) {
            // only for file based databases
            String file = databaseInfo.getMainFile();
            return Strings.isNotEmpty(file) && new File(file).exists();
        }
        return true;
    }

    public String getConnectionUrl() {
        return configType == ConnectionConfigType.BASIC ?
                urlPattern.buildUrl(databaseInfo) :
                databaseInfo.getUrl();
    }

    public String getConnectionUrl(String host, String port) {
        if (configType == ConnectionConfigType.BASIC) {
            return urlPattern.buildUrl(
                    databaseInfo.getVendor(),
                    host,
                    port,
                    databaseInfo.getDatabase(),
                    databaseInfo.getMainFile());
        } else {
            return databaseInfo.getUrl();
        }
    }


    public void updateSignature() {
        signature = hashCode();
    }

    @Override
    public ConnectionDatabaseSettings clone() {
        Element connectionElement = new Element(getConfigElementName());
        writeConfiguration(connectionElement);
        ConnectionDatabaseSettings clone = new ConnectionDatabaseSettings(getParent(), databaseType, configType);
        clone.readConfiguration(connectionElement);
        clone.setConnectivityStatus(getConnectivityStatus());
        return clone;
    }

    public void validate() throws ConfigurationException{
        List<String> errors = new ArrayList<>();
        DatabaseType databaseType = getDatabaseType();
// TODO: clean up. Now it is allowed generic JDBC database configuration
//        if (databaseType == DatabaseType.UNKNOWN) {
//            errors.add("Database type not provided");
//        }

        String connectionUrl = getConnectionUrl();
        if (Strings.isEmpty(connectionUrl)) {
            errors.add(configType == ConnectionConfigType.BASIC ?
                    "Database information not provided (host, port, database)" :
                    "Database connection url not provided");
        } else {
            if (configType == ConnectionConfigType.BASIC && !urlPattern.isValid(connectionUrl)) {
                errors.add("Database information incomplete or invalid (host, port, database, file)");
            }
        }

        if (getDriverSource() == DriverSource.EXTERNAL) {
            if (Strings.isEmpty(getDriverLibrary())) {
                errors.add("JDBC driver library not provided");
            } else {
                String driver = getDriver();
                if (Strings.isEmpty(driver)) {
                    errors.add("JDBC driver not provided");
                } else {
                    DatabaseType driverDatabaseType = DatabaseType.resolve(driver);
                    if (databaseType != DatabaseType.GENERIC && driverDatabaseType != databaseType) {
                        errors.add("JDBC driver does not match the selected database type");
                    }
                }
            }
        }

        if (!errors.isEmpty()) {
            StringBuilder message = new StringBuilder("Invalid or incomplete database configuration:");
            for (String error : errors) {
                message.append("\n - ").append(error);
            }
            throw new ConfigurationException(message.toString());
        }
    }

    @NotNull
    public ConnectionId getConnectionId() {
        return getParent().getConnectionId();
    }

    /*********************************************************
    *                 PersistentConfiguration               *
    *********************************************************/
    @Override
    public void readConfiguration(Element element) {
        ConnectionId connectionId = ConnectionId.get(getString(element, "id", null));
        if (connectionId != null) {
            getParent().setConnectionId(connectionId);
        }

        name             = getString(element, "name", name);
        description      = getString(element, "description", description);

        databaseType     = getEnum(element, "database-type", databaseType);

        // TODO temporary backward compatibility
        if (databaseType == DatabaseType.UNKNOWN) databaseType = DatabaseType.GENERIC;


        configType       = getEnum(element, "config-type", configType);
        databaseVersion  = getDouble(element, "database-version", databaseVersion);

        if (configType == ConnectionConfigType.BASIC) {
            databaseInfo.setHost(getString(element, "host", null));
            databaseInfo.setPort(getString(element, "port", null));
            databaseInfo.setDatabase(getString(element, "database", null));

            DatabaseUrlType urlType = getEnum(element, "url-type", databaseType.getDefaultUrlPattern().getUrlType());
            databaseInfo.setUrlType(urlType);
            urlPattern = DatabaseUrlPattern.get(databaseType, urlType);

            if (urlType == DatabaseUrlType.FILE) {
                Element filesElement = element.getChild("files");
                DatabaseFiles databaseFiles = new DatabaseFiles();
                databaseFiles.readConfiguration(filesElement);
                databaseInfo.setFiles(databaseFiles);
            }
        } else if (configType == ConnectionConfigType.CUSTOM){
            String url = getString(element, "url", databaseInfo.getUrl());
            databaseInfo.setUrl(url);
            if (databaseType != DatabaseType.GENERIC) {
                urlPattern = databaseType.resolveUrlPattern(url);
                databaseInfo.setUrlType(urlPattern.getUrlType());
                databaseInfo.setHost(urlPattern.resolveHost(url));
                databaseInfo.setPort(urlPattern.resolvePort(url));
                databaseInfo.setDatabase(urlPattern.resolveDatabase(url));

                String file = urlPattern.resolveFile(url);
                if (Strings.isNotEmptyOrSpaces(file)) {
                    databaseInfo.setMainFile(file);
                }

            }
        }

        driverSource  = getEnum(element, "driver-source", driverSource);
        // TODO temporary backward compatibility
        if (driverSource == DriverSource.BUILTIN) driverSource = DriverSource.BUNDLED;

        driverLibrary = Files.convertToAbsolutePath(getProject(), getString(element, "driver-library", driverLibrary));
        driver        = getString(element, "driver", driver);

        authenticationInfo.readConfiguration(element);

        // TODO backward compatibility (to remove)
        Element propertiesElement = element.getChild("properties");
        if (propertiesElement != null) {
            for (Element propertyElement : propertiesElement.getChildren()) {
                Map<String, String> properties = getParent().getPropertiesSettings().getProperties();
                properties.put(
                        stringAttribute(propertyElement, "key"),
                        stringAttribute(propertyElement, "value"));
            }
        }
        deriveDatabaseType();
        updateSignature();
    }

    public File getDriverLibraryFile() {
        return new File(driverLibrary);
    }

    @Override
    public void writeConfiguration(Element element) {
        String driverLibrary = ConnectionBundleSettings.IS_IMPORT_EXPORT_ACTION.get() ?
                Files.convertToRelativePath(getProject(), this.driverLibrary) :
                this.driverLibrary;

        setString(element, "name", nvl(name));
        setString(element, "description", nvl(description));

        setEnum(element, "database-type", databaseType);
        setEnum(element, "config-type", configType);
        setDouble(element, "database-version", databaseVersion);

        setEnum(element, "driver-source", driverSource);
        setString(element, "driver-library", nvl(driverLibrary));
        setString(element, "driver", nvl(driver));

        if (configType == ConnectionConfigType.BASIC) {
            setEnum(element, "url-type", databaseInfo.getUrlType());
            setString(element, "host", nvl(databaseInfo.getHost()));
            setString(element, "port", nvl(databaseInfo.getPort()));
            setString(element, "database", nvl(databaseInfo.getDatabase()));
            DatabaseFiles files = databaseInfo.getFiles();
            if (files != null) {
                Element filesElement = new Element("files");
                element.addContent(filesElement);
                files.writeConfiguration(filesElement);
            }
        } else if (configType == ConnectionConfigType.CUSTOM) {
            setString(element, "url", nvl(databaseInfo.getUrl()));
        }

        authenticationInfo.writeConfiguration(element);
    }

    public Project getProject() {
        return getParent().getProject();
    }
}
