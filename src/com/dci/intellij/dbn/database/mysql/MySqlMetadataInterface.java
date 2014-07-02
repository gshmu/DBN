package com.dci.intellij.dbn.database.mysql;

import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.DatabaseObjectTypeId;
import com.dci.intellij.dbn.database.common.DatabaseMetadataInterfaceImpl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MySqlMetadataInterface extends DatabaseMetadataInterfaceImpl {

    public MySqlMetadataInterface(DatabaseInterfaceProvider provider) {
        super("mysql_metadata_interface.xml", provider);
    }

    public ResultSet loadCompileObjectErrors(String ownerName, String objectName, Connection connection) throws SQLException {
        return null;
    }

    @Override
    public ResultSet loadMethodArguments(String ownerName, String methodName, String methodType, int overload, Connection connection) throws SQLException {
        try {
            return super.loadMethodArguments(ownerName, methodName, methodType, overload, connection);
        } catch (SQLException e) {
            ResultSet resultSet = executeQuery(connection, "alternative-method-arguments", ownerName, methodName, methodType, overload);
            return new MySqlArgumentsResultSet(resultSet);
        }
    }

    @Override
    public ResultSet loadAllMethodArguments(String ownerName, Connection connection) throws SQLException {
        try {
            return super.loadAllMethodArguments(ownerName, connection);
        } catch (SQLException e) {
            ResultSet resultSet = executeQuery(connection, "alternative-all-method-arguments", ownerName);
            return new MySqlArgumentsResultSet(resultSet);
        }
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