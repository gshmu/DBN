package com.dci.intellij.dbn.data.value;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jetbrains.annotations.Nullable;

import oracle.jdbc.OracleResultSet;
import oracle.sql.OPAQUE;
import oracle.xdb.XMLType;

public class XmlTypeValue extends LargeObjectValue{
    private XMLType xmlType;

    public XmlTypeValue() {
    }

    public XmlTypeValue(OracleResultSet resultSet, int columnIndex) throws SQLException {
        OPAQUE opaque = resultSet.getOPAQUE(columnIndex);
        xmlType = opaque == null ? null : XMLType.createXML(opaque);
    }

    @Nullable
    public String read() throws SQLException {
        return read(0);
    }

    @Override
    @Nullable
    public String read(int maxSize) throws SQLException {
        return xmlType == null ? null : xmlType.getStringVal();
    }

    @Override
    public void write(Connection connection, ResultSet resultSet, int columnIndex, @Nullable String value) throws SQLException {
        xmlType = XMLType.createXML(connection, value);
        resultSet.updateObject(columnIndex, xmlType);
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
