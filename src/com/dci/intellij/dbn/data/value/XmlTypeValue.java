package com.dci.intellij.dbn.data.value;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jetbrains.annotations.Nullable;

import oracle.jdbc.OracleResultSet;
import oracle.sql.OPAQUE;
import oracle.sql.OpaqueDescriptor;

public class XmlTypeValue extends LargeObjectValue{
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

    @Nullable
    public String read() throws SQLException {
        return read(0);
    }

    @Override
    @Nullable
    public String read(int maxSize) throws SQLException {
        if (opaque == null) {
            return null;
        } else {
            byte[] value = opaque.getBytesValue();
            if (value == null) {
                return null;
            } else {
                return new String(value);
            }
        }
    }

    @Override
    public void write(Connection connection, ResultSet resultSet, int columnIndex, @Nullable String value) throws SQLException {
        if (opaque == null) {
            OpaqueDescriptor opaqueDescriptor = OpaqueDescriptor.createDescriptor("SYS.XMLTYPE", connection);
            opaque = new OPAQUE(opaqueDescriptor, new byte[0], connection);
        }
        if (value != null) {
            byte[] bytes = value.getBytes();
            opaque.setValue(bytes);
            opaque.setShareBytes(bytes);
        }
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
