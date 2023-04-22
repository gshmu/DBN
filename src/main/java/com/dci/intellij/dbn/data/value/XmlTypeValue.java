package com.dci.intellij.dbn.data.value;

import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.jdbc.DBNResultSet;
import com.dci.intellij.dbn.data.type.GenericDataType;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.sql.*;

public class XmlTypeValue extends LargeObjectValue{
    //private XMLType xmlType;
    private Object xmlType;

    @Getter(lazy = true)
    private final XmlTypeDelegate delegate = XmlTypeDelegate.getInstance();


    public XmlTypeValue() {
    }

    public XmlTypeValue(CallableStatement callableStatement, int parameterIndex) throws SQLException {
/*
        OracleCallableStatement oracleCallableStatement = (OracleCallableStatement) callableStatement;
        OPAQUE opaque = oracleCallableStatement.getOPAQUE(parameterIndex);
        if (opaque instanceof XMLType) {
            xmlType = (XMLType) opaque;
        } else {
            xmlType = opaque == null ? null : XMLType.createXML(opaque);
        }
*/

        XmlTypeDelegate delegate = getDelegate();
        Object opaque = delegate.invoke(delegate.getStatementOpaqueMethod(), callableStatement, parameterIndex);
        if (opaque == null) return;

        xmlType = delegate.isXmlType(opaque) ? opaque :
                delegate.invoke(delegate.getCreateXmlFromOpaqueMethod(), null, opaque);
    }

    public XmlTypeValue(ResultSet resultSet, int columnIndex) throws SQLException {
/*
        resultSet = DBNResultSet.getInner(resultSet);

        OracleResultSet oracleResultSet = (OracleResultSet) resultSet;
        OPAQUE opaque = oracleResultSet.getOPAQUE(columnIndex);
        if (opaque instanceof XMLType) {
            xmlType = (XMLType) opaque;
        } else {
            xmlType = opaque == null ? null : XMLType.createXML(opaque);
        }
*/

        XmlTypeDelegate delegate = getDelegate();
        resultSet = DBNResultSet.getInner(resultSet);

        Object opaque = delegate.invoke(delegate.getResultSetOpaqueMethod(), resultSet, columnIndex);
        if (opaque == null) return;

        xmlType = delegate.isXmlType(opaque) ? opaque :
                delegate.invoke(delegate.getCreateXmlFromOpaqueMethod(), null, opaque);
    }

    @Override
    public GenericDataType getGenericDataType() {
        return GenericDataType.XMLTYPE;
    }

    @Override
    @Nullable
    public String read() throws SQLException {
        return read(0);
    }

    @Nullable
    @Override
    public String export() throws SQLException {
        return read();
    }

    @Override
    @Nullable
    public String read(int maxSize) throws SQLException {
        XmlTypeDelegate delegate = getDelegate();
        return xmlType == null ? null : delegate.invoke(delegate.getStringValueMethod(), xmlType);
    }

    @Override
    public void write(Connection connection, PreparedStatement preparedStatement, int parameterIndex, @Nullable String value) throws SQLException {
/*
        connection = DBNConnection.getInner(connection);
        xmlType = XMLType.createXML(connection, value);
        preparedStatement.setObject(parameterIndex, xmlType);
*/

        XmlTypeDelegate delegate = getDelegate();
        connection = DBNConnection.getInner(connection);
        xmlType = delegate.invoke(delegate.getCreateXmlFromStringMethod(), null, connection, value);
        preparedStatement.setObject(parameterIndex, xmlType);
    }

    @Override
    public void write(Connection connection, ResultSet resultSet, int columnIndex, @Nullable String value) throws SQLException {
/*
        connection = DBNConnection.getInner(connection);
        resultSet = DBNResultSet.getInner(resultSet);

        OracleResultSet oracleResultSet = (OracleResultSet) resultSet;
        xmlType = value == null ? null : XMLType.createXML(connection, value);
        oracleResultSet.updateOracleObject(columnIndex, xmlType);
*/

        XmlTypeDelegate delegate = getDelegate();
        connection = DBNConnection.getInner(connection);
        resultSet = DBNResultSet.getInner(resultSet);

        xmlType = value == null ? null : delegate.invoke(delegate.getCreateXmlFromStringMethod(), null, connection, value);
        delegate.invoke(delegate.getUpdateResultSetObjectMethod(), resultSet, columnIndex, xmlType);
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
