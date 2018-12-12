package com.dci.intellij.dbn.data.value;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.data.type.GenericDataType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BlobValue extends LargeObjectValue {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    private Blob blob;
    private InputStream inputStream;

    public BlobValue() {}

    public BlobValue(CallableStatement callableStatement, int parameterIndex) throws SQLException {
        blob = callableStatement.getBlob(parameterIndex);
    }

    public BlobValue(ResultSet resultSet, int columnIndex) throws SQLException {
        this.blob = resultSet.getBlob(columnIndex);
    }

    @Override
    public void write(Connection connection, PreparedStatement preparedStatement, int parameterIndex, @Nullable String value) throws SQLException {
        if (value == null) {
            preparedStatement.setBlob(parameterIndex, (Blob) null);
        } else {
            Blob blob = connection.createBlob();
            blob.setBytes(1, value.getBytes());
            preparedStatement.setBlob(parameterIndex, blob);
        }
    }

    public void write(Connection connection, ResultSet resultSet, int columnIndex, @Nullable String value) throws SQLException {
        if (StringUtil.isEmpty(value)) {
            blob = null;
        } else {
            blob = connection.createBlob();
            blob.setBytes(1, value.getBytes());
        }
        resultSet.updateBlob(columnIndex, blob);
    }

    @Override
    public GenericDataType getGenericDataType() {
        return GenericDataType.BLOB;
    }

    @Nullable
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

    public String getDisplayValue() {
        /*try {
            return "[BLOB] " + size() + "";
        } catch (SQLException e) {
            return "[BLOB]";
        }*/

        return "[BLOB]";
    }
}
