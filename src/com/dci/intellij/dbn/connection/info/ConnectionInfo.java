package com.dci.intellij.dbn.connection.info;

import com.dci.intellij.dbn.common.util.Safe;
import com.dci.intellij.dbn.connection.DatabaseType;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

@Value
public class ConnectionInfo {
    private final DatabaseType databaseType;
    private final String productName;
    private final String productVersion;
    private final String driverName;
    private final String driverVersion;
    private final String driverJdbcType;
    private final String url;
    private final String userName;

    public ConnectionInfo(DatabaseMetaData metaData) throws SQLException {
        productName = metaData.getDatabaseProductName();
        productVersion = resolveProductVersion(metaData);
        driverName = metaData.getDriverName();
        driverVersion = metaData.getDriverVersion();
        url = metaData.getURL();
        userName = metaData.getUserName();
        driverJdbcType = resolveDriverType(metaData);
        databaseType = DatabaseType.resolve( productName.toLowerCase());
    }

    @NotNull
    private static String resolveDriverType(DatabaseMetaData metaData) throws SQLException {
        return metaData.getJDBCMajorVersion() + (metaData.getJDBCMinorVersion() > 0 ? "." + metaData.getJDBCMinorVersion() : "");
    }

    @NotNull
    private static String resolveProductVersion(DatabaseMetaData metaData) throws SQLException {
        String productVersion = Safe.call("UNKNOWN", () -> metaData.getDatabaseProductVersion());
        int index = productVersion.indexOf('\n');
        productVersion = index > -1 ? productVersion.substring(0, index) : productVersion;
        return productVersion;
    }

    public String toString() {
        return  "Product name:\t" + productName + '\n' +
                "Product version:\t" + productVersion + '\n' +
                "Driver name:\t\t" + driverName + '\n' +
                "Driver version:\t" + driverVersion + '\n'+
                "JDBC Type:\t\t" + driverJdbcType + '\n' +
                "URL:\t\t" + url + '\n' +
                "User name:\t\t" + userName;
    }
}
