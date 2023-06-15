package com.dci.intellij.dbn.database.postgres;

import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.database.DatabaseObjectIdentifier;
import com.dci.intellij.dbn.database.interfaces.DatabaseMessageParserInterface;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;

import static com.dci.intellij.dbn.common.dispose.Failsafe.conditionallyLog;

@Slf4j
public class PostgresMessageParserInterface implements DatabaseMessageParserInterface {

    @Override
    @Nullable
    public DatabaseObjectIdentifier identifyObject(SQLException exception) {
         return null;
    }

    @Override
    public boolean isTimeoutException(SQLException e) {
        return e instanceof SQLTimeoutException;
    }

    @Override
    public boolean isModelException(SQLException e) {
        String sqlState = getSqlState(e);
        return Strings.isOneOfIgnoreCase(sqlState, "3D000", "3F000", "42P01", "42703", "42704");
    }

    @Override
    public boolean isAuthenticationException(SQLException e) {
        String sqlState = getSqlState(e);
        return Strings.isOneOfIgnoreCase(sqlState, "28P01");
    }

    private static String getSqlState(SQLException e) {
        try {
            Method method = e.getClass().getMethod("getSQLState");
            return (String) method.invoke(e);
        } catch (Exception ex) {
            conditionallyLog(ex);
            log.error("Could not get exception SQLState", ex);
        }
        return "";
    }

    @Override
    public boolean isSuccessException(SQLException exception) {
        return false;
    }
}