package com.dci.intellij.dbn.database.postgres;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.database.DatabaseMessageParserInterface;
import com.dci.intellij.dbn.database.DatabaseObjectIdentifier;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.sql.SQLException;

public class PostgresMessageParserInterface implements DatabaseMessageParserInterface {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    @Nullable
    public DatabaseObjectIdentifier identifyObject(String message) {
         return null;
    }

    public boolean isTimeoutException(SQLException e) {
        return false;
    }

    public boolean isModelException(SQLException e) {
        try {
            Method method = e.getClass().getMethod("getSQLState");
            String sqlState = (String) method.invoke(e);
            return StringUtil.isOneOfIgnoreCase(sqlState, "3D000", "3F000", "42P01", "42703", "42704");
        } catch (Exception ex) {
            LOGGER.error("Could not get exception SQLState", ex);
        }
        return false;

    }

}