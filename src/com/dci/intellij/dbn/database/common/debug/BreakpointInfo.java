package com.dci.intellij.dbn.database.common.debug;

import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;

public class BreakpointInfo extends BasicOperationInfo {
    private Integer breakpointId;

    @Nullable
    public Integer getBreakpointId() {
        return breakpointId;
    }

    @Override
    public void registerParameters(CallableStatement statement) throws SQLException {
        statement.registerOutParameter(1, Types.NUMERIC);
        statement.registerOutParameter(2, Types.VARCHAR);
    }

    @Override
    public void read(CallableStatement statement) throws SQLException {
        Object object = statement.getObject(1);
        if (object instanceof BigDecimal) {
            BigDecimal bigDecimal = (BigDecimal) object;
            breakpointId = bigDecimal.intValue();
        }

        error = statement.getString(2);
    }

    public void setBreakpointId(Integer breakpointId) {
        this.breakpointId = breakpointId;
    }
}
