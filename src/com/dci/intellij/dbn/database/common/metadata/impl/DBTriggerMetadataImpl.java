package com.dci.intellij.dbn.database.common.metadata.impl;

import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadataBase;
import com.dci.intellij.dbn.database.common.metadata.def.DBTriggerMetadata;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DBTriggerMetadataImpl extends DBObjectMetadataBase implements DBTriggerMetadata {

    public DBTriggerMetadataImpl(ResultSet resultSet) {
        super(resultSet);
    }

    @Override
    public String getTriggerName() throws SQLException {
        return resultSet.getString("TRIGGER_NAME");
    }

    @Override
    public String getDatasetName() throws SQLException {
        return resultSet.getString("DATASET_NAME");
    }

    @Override
    public String getTriggerType() throws SQLException {
        return resultSet.getString("TRIGGER_TYPE");
    }

    @Override
    public String getTriggeringEvent() throws SQLException {
        return resultSet.getString("TRIGGERING_EVENT");
    }

    @Override
    public boolean isForEachRow() throws SQLException {
        return resultSet.getString("IS_FOR_EACH_ROW").equals("Y");
    }

    @Override
    public boolean isEnabled() throws SQLException {
        return resultSet.getString("IS_ENABLED").equals("Y");
    }

    @Override
    public boolean isValid() throws SQLException {
        return resultSet.getString("IS_VALID").equals("Y");
    }

    @Override
    public boolean isDebug() throws SQLException {
        return resultSet.getString("IS_DEBUG").equals("Y");
    }
}
