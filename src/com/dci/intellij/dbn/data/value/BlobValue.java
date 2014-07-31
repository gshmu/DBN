package com.dci.intellij.dbn.data.value;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.dci.intellij.dbn.common.util.CommonUtil;

public class BlobValue implements LargeObjectValue {
    private Blob blob;

    public BlobValue(Blob blob) {
        this.blob = blob;
    }

    public void write(Connection connection, ResultSet resultSet, int columnIndex, String value) throws SQLException {
        if (blob == null) {
            value = CommonUtil.nvl(value, "");
            resultSet.updateBlob(columnIndex, new ByteArrayInputStream(value.getBytes()));
            //resultSet.updateBinaryStream(columnIndex, new ByteArrayInputStream(value.getBytes()));
            blob = resultSet.getBlob(columnIndex);
        } else {
            if (blob.length() > value.length()) {
                blob.truncate(value.length());
            }

            blob.setBytes(1, value.getBytes());
            resultSet.updateBlob(columnIndex, blob);
            //resultSet.updateBinaryStream(columnIndex, new ByteArrayInputStream(value.getBytes()));
        }
    }

    public String read() throws SQLException {
        return read(0);
    }

    @Override
    public String read(int maxSize) throws SQLException {
        if (blob == null) {
            return null;
        } else {
            try {
                int size = (int) (maxSize == 0 ? blob.length() : Math.min(maxSize, blob.length()));
                byte[] buffer = new byte[size];
                blob.getBinaryStream().read(buffer, 0, size);
                return new String(buffer);
            } catch (IOException e) {
                throw new SQLException("Could not read value from BLOB.");
            }

        }
    }

    @Override
    public long size() throws SQLException {
        return blob == null ? 0 : blob.length();
    }

    public String getDisplayValue() {
        /*try {
            return "[BLOB] " + size() + "";
        } catch (SQLException e) {
            return "[BLOB]";
        }*/

        return "[BLOB]";
    }
}
