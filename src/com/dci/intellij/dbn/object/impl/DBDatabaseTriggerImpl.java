package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.database.common.metadata.def.DBTriggerMetadata;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.DBDatabaseTrigger;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.loader.DBSourceCodeLoader;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.dci.intellij.dbn.object.common.status.DBObjectStatusHolder;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.dci.intellij.dbn.object.type.DBTriggerEvent;
import com.dci.intellij.dbn.object.type.DBTriggerType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBDatabaseTriggerImpl extends DBTriggerImpl implements DBDatabaseTrigger {
    DBDatabaseTriggerImpl(DBSchema schema, DBTriggerMetadata metadata) throws SQLException {
        super(schema, metadata);
    }

    @NotNull
    @Override
    public DBObjectType getObjectType() {
        return DBObjectType.DATABASE_TRIGGER;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        DBObjectStatusHolder objectStatus = getStatus();
        if (objectStatus.is(DBObjectStatus.VALID)) {
            if (objectStatus.is(DBObjectStatus.ENABLED)) {
                if (objectStatus.is(DBObjectStatus.DEBUG)) {
                    return Icons.DBO_DATABASE_TRIGGER_DEBUG;
                } else {
                    return Icons.DBO_DATABASE_TRIGGER;
                }
            } else {
                if (objectStatus.is(DBObjectStatus.DEBUG)) {
                    return Icons.DBO_DATABASE_TRIGGER_DISABLED_DEBUG;
                } else {
                    return Icons.DBO_DATABASE_TRIGGER_DISABLED;
                }
            }
        } else {
            if (objectStatus.is(DBObjectStatus.ENABLED)) {
                return Icons.DBO_DATABASE_TRIGGER_ERR;
            } else {
                return Icons.DBO_DATABASE_TRIGGER_ERR_DISABLED;
            }

        }
    }


    @Override
    public void buildToolTip(HtmlToolTipBuilder ttb) {
        DBTriggerType triggerType = getTriggerType();
        DBTriggerEvent[] triggeringEvents = getTriggerEvents();
        ttb.append(true, getObjectType().getName(), true);
        StringBuilder triggerDesc = new StringBuilder();
        triggerDesc.append(" - ");
        triggerDesc.append(triggerType.getName().toLowerCase());
        triggerDesc.append(" ") ;

        for (DBTriggerEvent triggeringEvent : triggeringEvents) {
            if (triggeringEvent != triggeringEvents[0]) triggerDesc.append(" or ");
            triggerDesc.append(triggeringEvent.getName());
        }

        triggerDesc.append(" on database");

        ttb.append(false, triggerDesc.toString(), false);

        ttb.createEmptyRow();
        super.buildToolTip(ttb);
    }

    /*********************************************************
     *                         Loaders                       *
     *********************************************************/
    private class SourceCodeLoader extends DBSourceCodeLoader {
        SourceCodeLoader(DBObject object) {
            super(object, false);
        }

        @Override
        public ResultSet loadSourceCode(DBNConnection connection) throws SQLException {
            ConnectionHandler connectionHandler = getConnectionHandler();
            DatabaseMetadataInterface metadataInterface = connectionHandler.getInterfaceProvider().getMetadataInterface();
            return metadataInterface.loadDatabaseTriggerSourceCode(getSchema().getName(), getName(), connection);
        }
    }


    @Override
    public String loadCodeFromDatabase(DBContentType contentType) throws SQLException {
        SourceCodeLoader sourceCodeLoader = new SourceCodeLoader(this);
        return sourceCodeLoader.load();
    }
}
