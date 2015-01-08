package com.dci.intellij.dbn.data.value;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.intellij.openapi.diagnostic.Logger;

public class BlobValue implements LargeObjectValue {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    private Blob blob;
    private InputStream inputStream;

    public BlobValue(ResultSet resultSet, int columnIndex) throws SQLException {
        this.blob = resultSet.getBlob(columnIndex);
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
            long totalLength = blob.length();
            int size = (int) (maxSize == 0 ? totalLength : Math.min(maxSize, totalLength));
            try {
                byte[] buffer = new byte[size];
                inputStream = blob.getBinaryStream();
                inputStream.read(buffer, 0, size);
                return new String(buffer);
            } catch (IOException e) {
                throw new SQLException("Could not read value from BLOB.");
            } finally {
                if (totalLength <= size) {
                    release();
                }
            }
        }
    }

    public void release() {
        if (inputStream != null) {
            try {
                inputStream.close();
                inputStream = null;
            } catch (IOException e) {
                LOGGER.error("Could not close BLOB input stream.", e);
            }
        }
    }


    @Override
    public long size() throws SQLException {
        return blob == null ? 0 : blob.length();
    }

    @Override
    public String getContentTypeName() {
        return null;
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
