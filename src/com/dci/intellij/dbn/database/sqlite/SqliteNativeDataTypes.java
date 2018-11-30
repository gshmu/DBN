package com.dci.intellij.dbn.database.sqlite;

import com.dci.intellij.dbn.common.latent.ThreadLocalLatent;
import com.dci.intellij.dbn.database.common.DatabaseNativeDataTypes;
import com.dci.intellij.dbn.database.common.util.DataTypeParseAdapter;

import java.math.BigInteger;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;

class SqliteNativeDataTypes extends DatabaseNativeDataTypes {
    private static final ThreadLocalLatent<SimpleDateFormat> DATE_FORMAT = ThreadLocalLatent.create(() -> new SimpleDateFormat("yyyy-MM-dd"));
    private static final ThreadLocalLatent<SimpleDateFormat> TIMESTAMP_FORMAT = ThreadLocalLatent.create(() -> new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"));

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
        createNumericDefinition("FLOAT", Double.class, Types.FLOAT);
        createNumericDefinition("NUMERIC", Double.class, Types.NUMERIC);
        createNumericDefinition("DECIMAL", Double.class, Types.NUMERIC);
        createNumericDefinition("BOOLEAN", Integer.class, Types.INTEGER);

        createLiteralDefinition("CHARACTER", String.class, Types.CHAR);
        createLiteralDefinition("VARCHAR", String.class, Types.VARCHAR);
        createLiteralDefinition("VARYING CHARACTER", String.class, Types.VARCHAR);
        createLiteralDefinition("NCHAR", String.class, Types.NCHAR);
        createLiteralDefinition("NATIVE CHARACTER", String.class, Types.NCHAR);
        createLiteralDefinition("NVARCHAR", String.class, Types.NVARCHAR);
        createLiteralDefinition("TEXT", String.class, Types.VARCHAR);
        createLiteralDefinition("NULL", String.class, Types.VARCHAR);
        createLiteralDefinition("BLOB", String.class, Types.BLOB);
        createLiteralDefinition("CLOB", String.class, Types.CLOB);

        createDateTimeDefinition("DATE", Date.class, Types.DATE, new DataTypeParseAdapter<Date>() {

            @Override
            public String toString(Date object) {
                return object == null ? null : getDateFormat().format(object);
            }

            @Override
            public Date parse(String string) throws SQLException {
                try {
                    return string == null ? null : new Date(getDateFormat().parse(string).getTime());
                } catch (ParseException e) {
                    throw new SQLException("Error parsing value \"" + string + "\" into DATE");
                }
            }
        });
        createDateTimeDefinition("DATETIME", Timestamp.class, Types.TIMESTAMP, new DataTypeParseAdapter<Timestamp>() {
            @Override
            public String toString(Timestamp object) {
                return object == null ? null : getTimestampFormat().format(object);
            }

            @Override
            public Timestamp parse(String string) throws SQLException {
                try {
                    return string == null ? null : new Timestamp(getTimestampFormat().parse(string).getTime());
                } catch (ParseException e) {
                    throw new SQLException("Error parsing value \"" + string + "\" into TIMESTAMP");
                }
            }
        });


    }

    private SimpleDateFormat getTimestampFormat() {
        return TIMESTAMP_FORMAT.get();
    }

    private SimpleDateFormat getDateFormat() {
        return DATE_FORMAT.get();
    }
}