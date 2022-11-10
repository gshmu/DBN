package com.dci.intellij.dbn.data.model.resultSet;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.model.basic.BasicColumnInfo;
import com.dci.intellij.dbn.data.type.DBDataType;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

@Getter
@EqualsAndHashCode(callSuper = true)
public class ResultSetColumnInfo extends BasicColumnInfo {
    private final int resultSetColumnIndex;

    public ResultSetColumnInfo(ConnectionHandler connection, ResultSet resultSet, int columnIndex) throws SQLException {
        super(null, null, columnIndex);
        resultSetColumnIndex = columnIndex + 1;
        ResultSetMetaData metaData = resultSet.getMetaData();
        name = translateName(metaData.getColumnName(resultSetColumnIndex).intern(), connection);

        String dataTypeName = metaData.getColumnTypeName(resultSetColumnIndex);
        int precision = getPrecision(metaData);
        int scale = metaData.getScale(resultSetColumnIndex);

        dataType = DBDataType.get(connection, dataTypeName, precision, precision, scale, false);
    }

    public ResultSetColumnInfo(String name, DBDataType dataType, int columnIndex, int resultSetColumnIndex ) {
        super(name, dataType, columnIndex);
        this.resultSetColumnIndex = resultSetColumnIndex;
    }


    // lenient approach for oracle bug returning the size of LOBs instead of the precision.
    private int getPrecision(ResultSetMetaData metaData) throws SQLException {
        try {
            return metaData.getPrecision(resultSetColumnIndex);
        } catch (NumberFormatException e) {
            return 4000;
        }
    }

    public String translateName(String name, ConnectionHandler connection) {
        return name;
    }
}
