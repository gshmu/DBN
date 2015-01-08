package com.dci.intellij.dbn.data.value;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class XmlTypeValue implements LargeObjectValue{
    //private XMLType xmlType;

    public XmlTypeValue(ResultSet resultSet, int columnIndex) throws SQLException {
        try {
            //this.xmlType = XMLType.createXML(((OracleResultSet)resultSet).getOPAQUE(columnIndex));
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
        }

    }

    public String read() throws SQLException {
        return read(0);
    }

    @Override
    public String read(int maxSize) throws SQLException {
        return "";
        //return xmlType.getStringVal();
    }

    @Override
    public void write(Connection connection, ResultSet resultSet, int columnIndex, String value) throws SQLException {

    }

    @Override
    public void release() {

    }

    @Override
    public long size() throws SQLException {
        return 0;
    }

    @Override
    public String getDisplayValue() {
        return "[XMLTYPE]";
    }
}
