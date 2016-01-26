package com.dci.intellij.dbn.database.sqlite;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Timestamp;
import java.sql.Types;

import com.dci.intellij.dbn.data.type.GenericDataType;
import com.dci.intellij.dbn.database.common.DatabaseNativeDataTypes;

public class SqliteNativeDataTypes extends DatabaseNativeDataTypes {
    {
        createNumericDefinition("INT", Integer.class, Types.INTEGER);
        createNumericDefinition("INT2", Integer.class, Types.INTEGER);
        createNumericDefinition("INT8", Integer.class, Types.INTEGER);
        createNumericDefinition("INTEGER", Integer.class, Types.INTEGER);
        createNumericDefinition("TINYINT", Integer.class, Types.TINYINT);
        createNumericDefinition("SMALLINT", Integer.class, Types.SMALLINT);
        createNumericDefinition("MEDIUMINT", Integer.class, Types.INTEGER);
        createNumericDefinition("BIGINT", BigInteger.class, Types.BIGINT);
        createNumericDefinition("UNSIGNED BIG INT", BigInteger.class, Types.BIGINT);

        createNumericDefinition("REAL", Double.class, Types.DOUBLE);
        createNumericDefinition("DOUBLE", Double.class, Types.DOUBLE);
        createNumericDefinition("DOUBLE PRECISION", Double.class, Types.DOUBLE);
        createNumericDefinition("FLOAT", BigDecimal.class, Types.FLOAT);
        createNumericDefinition("NUMERIC", BigDecimal.class, Types.NUMERIC);
        createNumericDefinition("DECIMAL", BigDecimal.class, Types.NUMERIC);
        createNumericDefinition("BOOLEAN", Integer.class, Types.INTEGER);

        createDateTimeDefinition("DATE", Timestamp.class, Types.TIMESTAMP);
        createDateTimeDefinition("DATETIME", Timestamp.class, Types.TIMESTAMP);

        createLiteralDefinition("CHARACTER", String.class, Types.CHAR);
        createLiteralDefinition("VARCHAR", String.class, Types.VARCHAR);
        createLiteralDefinition("VARYING CHARACTER", String.class, Types.VARCHAR);
        createLiteralDefinition("NCHAR", String.class, Types.NCHAR);
        createLiteralDefinition("NATIVE CHARACTER", String.class, Types.NCHAR);
        createLiteralDefinition("NVARCHAR", String.class, Types.NVARCHAR);
        createLiteralDefinition("TEXT", String.class, Types.VARCHAR);
        createLiteralDefinition("NULL", String.class, Types.VARCHAR);
        createLargeValueDefinition("BLOB", Blob.class, Types.BLOB, GenericDataType.BLOB);
        createLargeValueDefinition("CLOB", Clob.class, Types.CLOB, GenericDataType.CLOB);

   }
}