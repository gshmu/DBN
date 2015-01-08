package com.dci.intellij.dbn.data.value;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import oracle.jdbc.OracleResultSet;
import oracle.sql.OPAQUE;

public class XmlTypeValue implements LargeObjectValue{
    private OPAQUE opaque;

    public XmlTypeValue() {
    }

    public XmlTypeValue(OracleResultSet resultSet, int columnIndex) throws SQLException {
        try {
            opaque = resultSet.getOPAQUE(columnIndex);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public String read() throws SQLException {
        return read(0);
    }

    @Override
    public String read(int maxSize) throws SQLException {
        return new String(opaque.getBytesValue());
    }

    @Override
    public void write(Connection connection, ResultSet resultSet, int columnIndex, String value) throws SQLException {
        opaque.setValue(value.getBytes());
        resultSet.updateObject(columnIndex, opaque);
    }

    @Override
    public void release() {

    }

    @Override
    public long size() throws SQLException {
        return 0;
    }

    @Override
    public String getContentTypeName() {
        return "XML";
    }

    @Override
    public String getDisplayValue() {
        return "[XMLTYPE]";
    }
}
