package com.dci.intellij.dbn.database.postgres;

import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.DatabaseObjectTypeId;
import com.dci.intellij.dbn.database.common.DatabaseMetadataInterfaceImpl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class PostgresMetadataInterface extends DatabaseMetadataInterfaceImpl {

    public PostgresMetadataInterface(DatabaseInterfaceProvider provider) {
        super("postgres_metadata_interface.xml", provider);
    }

    public ResultSet loadCompileObjectErrors(String ownerName, String objectName, Connection connection) throws SQLException {
        return null;
    }

    public ResultSet loadCharsets(Connection connection) throws SQLException {
        return executeQuery(connection, "charsets");
    }

    public String createDDLStatement(DatabaseObjectTypeId objectTypeId, String objectName, String code) {
        return objectTypeId == DatabaseObjectTypeId.VIEW ? "create view " + objectName + " as\n" + code :
               objectTypeId == DatabaseObjectTypeId.FUNCTION ? "create function " + objectName + " as\n" + code :
                       "create or replace\n" + code;
    }

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public String createDateString(Date date) {
        String dateString = DATE_FORMAT.format(date);
        return "str_to_date('" + dateString + "', '%Y-%m-%d %T')";
    }

}