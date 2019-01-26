package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseDDLInterface;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.object.DBDatasetTrigger;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.loader.DBSourceCodeLoader;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.dci.intellij.dbn.object.common.status.DBObjectStatusHolder;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBDatasetTriggerImpl extends DBTriggerImpl implements DBDatasetTrigger {
    DBDatasetTriggerImpl(DBDataset dataset, ResultSet resultSet) throws SQLException {
        super(dataset, resultSet);
    }

    @Override
    public DBDataset getDataset() {
        return (DBDataset) getParentObject();
    }

    @Override
    public DBObjectType getObjectType() {
        return DBObjectType.DATASET_TRIGGER;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        DBObjectStatusHolder objectStatus = getStatus();
        if (objectStatus.is(DBObjectStatus.VALID)) {
            if (objectStatus.is(DBObjectStatus.ENABLED)) {
                if (objectStatus.is(DBObjectStatus.DEBUG)) {
                    return Icons.DBO_TRIGGER_DEBUG;
                } else {
                    return Icons.DBO_TRIGGER;
                }
            } else {
                if (objectStatus.is(DBObjectStatus.DEBUG)) {
                    return Icons.DBO_TRIGGER_DISABLED_DEBUG;
                } else {
                    return Icons.DBO_TRIGGER_DISABLED;
                }
            }
        } else {
            if (objectStatus.is(DBObjectStatus.ENABLED)) {
                return Icons.DBO_TRIGGER_ERR;
            } else {
                return Icons.DBO_TRIGGER_ERR_DISABLED;
            }

        }
    }

    @Override
    public void buildToolTip(HtmlToolTipBuilder ttb) {
        TriggerType triggerType = getTriggerType();
        TriggeringEvent[] triggeringEvents = getTriggeringEvents();
        ttb.append(true, getObjectType().getName(), true);
        StringBuilder triggerDesc = new StringBuilder();
        triggerDesc.append(" - ");
        triggerDesc.append(triggerType.getName().toLowerCase());
        triggerDesc.append(" ") ;

        for (TriggeringEvent triggeringEvent : triggeringEvents) {
            if (triggeringEvent != triggeringEvents[0]) triggerDesc.append(" or ");
            triggerDesc.append(triggeringEvent.getName());
        }
        triggerDesc.append(" on ");
        triggerDesc.append(getDataset().getName());

        ttb.append(false, triggerDesc.toString(), false);

        ttb.createEmptyRow();
        super.buildToolTip(ttb);
    }

    /*********************************************************
     *                         Loaders                       *
     *********************************************************/
    private class SourceCodeLoader extends DBSourceCodeLoader {
        protected SourceCodeLoader(DBObject object) {
            super(object, false);
        }

        @Override
        public ResultSet loadSourceCode(DBNConnection connection) throws SQLException {
            ConnectionHandler connectionHandler = getConnectionHandler();
            DatabaseMetadataInterface metadataInterface = connectionHandler.getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadDatasetTriggerSourceCode(getDataset().getSchema().getName(), getDataset().getName(), getSchema().getName(), getName(), connection);
        }
    }

    @Override
    public void executeUpdateDDL(DBContentType contentType, String oldCode, String newCode) throws SQLException {
        ConnectionHandler connectionHandler = getConnectionHandler();
        DBNConnection connection = connectionHandler.getPoolConnection(getSchema(), false);
        try {
            DatabaseDDLInterface ddlInterface = connectionHandler.getInterfaceProvider().getDDLInterface();
            ddlInterface.updateTrigger(getDataset().getSchema().getName(), getDataset().getName(), getName(), oldCode, newCode, connection);
        } finally {
            connectionHandler.freePoolConnection(connection);
        }
    }

    @Override
    public String loadCodeFromDatabase(DBContentType contentType) throws SQLException {
        SourceCodeLoader sourceCodeLoader = new SourceCodeLoader(this);
        return sourceCodeLoader.load();
    }
}
