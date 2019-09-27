package com.dci.intellij.dbn.connection.config;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.database.DatabaseInfo;
import com.dci.intellij.dbn.common.options.BasicConfiguration;
import com.dci.intellij.dbn.common.util.FileUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.AuthenticationType;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectivityStatus;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.connection.DatabaseUrlPattern;
import com.dci.intellij.dbn.connection.DatabaseUrlType;
import com.dci.intellij.dbn.connection.config.file.DatabaseFiles;
import com.dci.intellij.dbn.connection.config.ui.ConnectionDatabaseSettingsForm;
import com.dci.intellij.dbn.driver.DriverSource;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.*;

public class ConnectionDatabaseSettings extends BasicConfiguration<ConnectionSettings, ConnectionDatabaseSettingsForm> {
    public static final Logger LOGGER = LoggerFactory.createLogger();

    private transient ConnectivityStatus connectivityStatus = ConnectivityStatus.UNKNOWN;
    private String name;
    private String description;
    private DatabaseType databaseType;
    private DatabaseType resolvedDatabaseType = DatabaseType.GENERIC;
    private DatabaseUrlPattern urlPattern;
    private double databaseVersion = 9999;
    private int hashCode;

    private DatabaseInfo databaseInfo;
    private DriverSource driverSource;
    private String driverLibrary;
    private String driver;

    private ConnectionConfigType configType;
    private AuthenticationInfo authenticationInfo = new AuthenticationInfo(this, false);

    public ConnectionDatabaseSettings(ConnectionSettings parent, @NotNull DatabaseType databaseType, ConnectionConfigType configType) {
        super(parent);
        this.databaseType = databaseType;
        this.configType = configType;
        this.urlPattern = databaseType.getDefaultUrlPattern();
        this.databaseInfo = urlPattern.getDefaultInfo();
        this.driverSource = databaseType == DatabaseType.GENERIC ?
                DriverSource.EXTERNAL :
                DriverSource.BUILTIN;

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

    @Override
    @NotNull
    public ConnectionDatabaseSettingsForm createConfigurationEditor() {
        return new ConnectionDatabaseSettingsForm(this);
    }

    public ConnectivityStatus getConnectivityStatus() {
        return connectivityStatus;
    }

    public void setConnectivityStatus(ConnectivityStatus connectivityStatus) {
        this.connectivityStatus = connectivityStatus;
    }

    public String getName() {
        return nvl(name);
    }

    public DriverSource getDriverSource() {
        return driverSource;
    }

    public void setDriverSource(DriverSource driverSource) {
        this.driverSource = driverSource;
    }

    public String getDriverLibrary() {
        return driverLibrary;
    }

    public String getDriver() {
        return driverSource == DriverSource.BUILTIN ? databaseType.getDriverClassName() : driver;
    }

    public void setDriverLibrary(String driverLibrary) {
        this.driverLibrary = driverLibrary;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    @Override
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

    @NotNull
    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public DatabaseType getResolvedDatabaseType() {
        return resolvedDatabaseType;
    }

    public void setResolvedDatabaseType(DatabaseType resolvedDatabaseType) {
        this.resolvedDatabaseType = resolvedDatabaseType;
    }

    public ConnectionConfigType getConfigType() {
        return configType;
    }

    public void setDatabaseType(DatabaseType databaseType) {
        if (this.databaseType != databaseType) {
            this.databaseType = databaseType;
            urlPattern = databaseType.getDefaultUrlPattern();
            databaseInfo.setUrlType(urlPattern.getUrlType());
        }
        initAuthType(databaseType);
    }

    public DatabaseUrlPattern getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(DatabaseUrlPattern urlPattern) {
        this.urlPattern = urlPattern;
    }

    public double getDatabaseVersion() {
        return databaseVersion;
    }

    public void setDatabaseVersion(double databaseVersion) {
        this.databaseVersion = databaseVersion;
    }

    @NotNull
    public AuthenticationInfo getAuthenticationInfo() {
        return authenticationInfo;
    }

    @Override
    public String getConfigElementName() {
        return "database";
    }


    public DatabaseInfo getDatabaseInfo() {
        return databaseInfo;
    }

    public boolean isDatabaseInitialized() {
        DatabaseInfo databaseInfo = getDatabaseInfo();
        if (databaseInfo.getUrlType() == DatabaseUrlType.FILE) {
            // only for file based databases
            String file = databaseInfo.getMainFile();
            return StringUtil.isNotEmpty(file) && new File(file).exists();
        }
        return true;
    }

    public String getConnectionUrl() {
        return configType == ConnectionConfigType.BASIC ?
                urlPattern.getUrl(databaseInfo) :
                databaseInfo.getUrl();
    };

    public String getConnectionUrl(String host, String port) {
        if (configType == ConnectionConfigType.BASIC) {
            return urlPattern.getUrl(
                    databaseInfo.getVendor(),
                    host,
                    port,
                    databaseInfo.getDatabase(),
                    databaseInfo.getMainFile());
        } else {
            return databaseInfo.getUrl();
        }
    }


    public void updateHashCode() {
        AuthenticationInfo authenticationInfo = getAuthenticationInfo();
        hashCode = (name + driver + driverLibrary +
                databaseInfo.getHost() +
                databaseInfo.getPort() +
                databaseInfo.getDatabase() +
                databaseInfo.getFilesForHash() +
                databaseInfo.getUrl() +
                authenticationInfo.getUser() +
                authenticationInfo.getPassword() +
                authenticationInfo.getType()).hashCode();
    }

    @Override
    public int hashCode() {
        return hashCode;
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

    public void checkConfiguration() throws ConfigurationException{
        List<String> errors = new ArrayList<String>();
        DatabaseType databaseType = getDatabaseType();
// TODO: clean up. Now it is allowed generic JDBC database configuration
//        if (databaseType == DatabaseType.UNKNOWN) {
//            errors.add("Database type not provided");
//        }

        String connectionUrl = getConnectionUrl();
        if (StringUtil.isEmpty(connectionUrl)) {
            errors.add(configType == ConnectionConfigType.BASIC ?
                    "Database information not provided (host, port, database)" :
                    "Database connection url not provided");
        } else {
            if (configType == ConnectionConfigType.BASIC && !urlPattern.isValid(connectionUrl)) {
                errors.add("Database information incomplete or invalid (host, port, database, file)");
            }
        }

        if (getDriverSource() == DriverSource.EXTERNAL) {
            if (StringUtil.isEmpty(getDriverLibrary())) {
                errors.add("JDBC driver library not provided");
            } else {
                String driver = getDriver();
                if (StringUtil.isEmpty(driver)) {
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
            databaseInfo.setHost(getString(element, "host", databaseInfo.getHost()));
            databaseInfo.setPort(getString(element, "port", databaseInfo.getPort()));
            databaseInfo.setDatabase(getString(element, "database", databaseInfo.getDatabase()));

            DatabaseUrlType urlType = getEnum(element, "url-type", databaseType.getDefaultUrlPattern().getUrlType());
            databaseInfo.setUrlType(urlType);
            urlPattern = DatabaseUrlPattern.get(databaseType, urlType);

            if (urlType == DatabaseUrlType.FILE) {
                Element filesElement = element.getChild("files");
                if (filesElement != null) {
                    DatabaseFiles databaseFiles = new DatabaseFiles();
                    databaseFiles.readConfiguration(filesElement);
                    databaseInfo.setFiles(databaseFiles);
                } else {
                    // TODO backward compatibility. Do cleanup
                    String file = getString(element, "file", null);
                    DatabaseFiles databaseFiles = new DatabaseFiles(file);
                    databaseInfo.setFiles(databaseFiles);
                }
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
                if (StringUtil.isNotEmptyOrSpaces(file)) {
                    databaseInfo.setMainFile(file);
                }

            }
        }

        driverSource  = getEnum(element, "driver-source", driverSource);
        driverLibrary = FileUtil.convertToAbsolutePath(getProject(), getString(element, "driver-library", driverLibrary));
        driver        = getString(element, "driver", driver);

        authenticationInfo.readConfiguration(element);

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

    @Override
    public void writeConfiguration(Element element) {
        String driverLibrary = ConnectionBundleSettings.IS_IMPORT_EXPORT_ACTION.get() ?
                FileUtil.convertToRelativePath(getProject(), this.driverLibrary) :
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
