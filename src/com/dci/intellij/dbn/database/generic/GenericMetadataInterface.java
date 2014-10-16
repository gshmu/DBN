package com.dci.intellij.dbn.database.generic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.common.DatabaseMetadataInterfaceImpl;


public class GenericMetadataInterface extends DatabaseMetadataInterfaceImpl {

    public GenericMetadataInterface(DatabaseInterfaceProvider provider) {
        super("generic_metadata_interface.xml", provider);
    }

    public ResultSet loadCompileObjectErrors(String ownerName, String objectName, Connection connection) throws SQLException {
        return null;
    }

    public ResultSet loadCharsets(Connection connection) throws SQLException {
        return executeQuery(connection, "charsets");
    }

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public String createDateString(Date date) {
        String dateString = DATE_FORMAT.format(date);
        return "str_to_date('" + dateString + "', '%Y-%m-%d %T')";
    }

    @Override
    public boolean isValid(Connection connection) {
        return false;
    }
}
