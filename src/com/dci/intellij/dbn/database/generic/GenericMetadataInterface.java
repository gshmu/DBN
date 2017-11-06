package com.dci.intellij.dbn.database.generic;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import com.dci.intellij.dbn.connection.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.common.DatabaseMetadataInterfaceImpl;


public class GenericMetadataInterface extends DatabaseMetadataInterfaceImpl {

    public GenericMetadataInterface(DatabaseInterfaceProvider provider) {
        super("generic_metadata_interface.xml", provider);
    }

    public ResultSet loadCompileObjectErrors(String ownerName, String objectName, DBNConnection connection) throws SQLException {
        return null;
    }

    public String createDateString(Date date) {
        String dateString = META_DATE_FORMAT.get().format(date);
        return "str_to_date('" + dateString + "', '%Y-%m-%d %T')";
    }

    @Override
    public boolean isValid(DBNConnection connection) {
        return false;
    }
}
