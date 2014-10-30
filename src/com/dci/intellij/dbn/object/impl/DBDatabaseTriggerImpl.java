package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.object.DBDatabaseTrigger;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.dci.intellij.dbn.object.common.status.DBObjectStatusHolder;

import javax.swing.Icon;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBDatabaseTriggerImpl extends DBTriggerImpl implements DBDatabaseTrigger {
    public DBDatabaseTriggerImpl(DBSchema schema, ResultSet resultSet) throws SQLException {
        super(schema, resultSet);
    }

    @Override
    public DBObjectType getObjectType() {
        return DBObjectType.DATABASE_TRIGGER;
    }

    @Override
    public Icon getIcon() {
        DBObjectStatusHolder status = getStatus();
        if (status.is(DBObjectStatus.VALID)) {
            if (status.is(DBObjectStatus.ENABLED)) {
                if (status.is(DBObjectStatus.DEBUG)) {
                    return Icons.DBO_DATABASE_TRIGGER_DEBUG;
                } else {
                    return Icons.DBO_DATABASE_TRIGGER;
                }
            } else {
                if (status.is(DBObjectStatus.DEBUG)) {
                    return Icons.DBO_DATABASE_TRIGGER_DISABLED_DEBUG;
                } else {
                    return Icons.DBO_DATABASE_TRIGGER_DISABLED;
                }
            }
        } else {
            if (status.is(DBObjectStatus.ENABLED)) {
                return Icons.DBO_DATABASE_TRIGGER_ERR;
            } else {
                return Icons.DBO_DATABASE_TRIGGER_ERR_DISABLED;
            }

        }
    }
}
