package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.database.common.metadata.def.DBTriggerMetadata;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBTrigger;
import com.dci.intellij.dbn.object.common.DBSchemaObjectImpl;
import com.dci.intellij.dbn.object.common.loader.DBObjectTimestampLoader;
import com.dci.intellij.dbn.object.common.operation.DBOperationExecutor;
import com.dci.intellij.dbn.object.common.operation.DBOperationType;
import com.dci.intellij.dbn.object.common.operation.DBUnsupportedOperationException;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.dci.intellij.dbn.object.common.status.DBObjectStatusHolder;
import com.dci.intellij.dbn.object.properties.PresentableProperty;
import com.dci.intellij.dbn.object.properties.SimplePresentableProperty;
import com.dci.intellij.dbn.object.type.DBTriggerEvent;
import com.dci.intellij.dbn.object.type.DBTriggerType;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.COMPILABLE;
import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.DEBUGABLE;
import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.DISABLEABLE;
import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.EDITABLE;
import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.FOR_EACH_ROW;
import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.INVALIDABLE;
import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.REFERENCEABLE;
import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.SCHEMA_OBJECT;
import static com.dci.intellij.dbn.object.type.DBTriggerEvent.*;
import static com.dci.intellij.dbn.object.type.DBTriggerType.AFTER;
import static com.dci.intellij.dbn.object.type.DBTriggerType.BEFORE;
import static com.dci.intellij.dbn.object.type.DBTriggerType.INSTEAD_OF;

public abstract class DBTriggerImpl extends DBSchemaObjectImpl<DBTriggerMetadata> implements DBTrigger {
    private DBTriggerType triggerType;
    private DBTriggerEvent[] triggerEvents;

    DBTriggerImpl(DBSchema schema, DBTriggerMetadata metadata) throws SQLException {
        super(schema, metadata);
    }

    DBTriggerImpl(DBDataset dataset, DBTriggerMetadata metadata) throws SQLException {
        super(dataset, metadata);
    }

    @Override
    protected String initObject(DBTriggerMetadata metadata) throws SQLException {
        String name = metadata.getTriggerName();
        set(FOR_EACH_ROW, metadata.isForEachRow());

        String triggerTypeString = metadata.getTriggerType();
        triggerType =
                triggerTypeString.contains("BEFORE") ? BEFORE :
                triggerTypeString.contains("AFTER") ? AFTER :
                triggerTypeString.contains("INSTEAD OF") ? INSTEAD_OF :
                        DBTriggerType.UNKNOWN;


        String triggeringEventString = metadata.getTriggeringEvent();
        List<DBTriggerEvent> eventList = new ArrayList<DBTriggerEvent>();
        if (triggeringEventString.contains("INSERT")) eventList.add(INSERT);
        if (triggeringEventString.contains("UPDATE")) eventList.add(UPDATE);
        if (triggeringEventString.contains("DELETE")) eventList.add(DELETE);
        if (triggeringEventString.contains("TRUNCATE")) eventList.add(TRUNCATE);
        if (triggeringEventString.contains("CREATE")) eventList.add(CREATE);
        if (triggeringEventString.contains("ALTER")) eventList.add(ALTER);
        if (triggeringEventString.contains("DROP")) eventList.add(DROP);
        if (triggeringEventString.contains("RENAME")) eventList.add(RENAME);
        if (triggeringEventString.contains("LOGON")) eventList.add(LOGON);
        if (triggeringEventString.contains("DDL")) eventList.add(DDL);
        if (eventList.size() == 0) eventList.add(DBTriggerEvent.UNKNOWN);

        triggerEvents = eventList.toArray(new DBTriggerEvent[0]);
        return name;
    }

    @Override
    public void initStatus(DBTriggerMetadata metadata) throws SQLException {
        DBObjectStatusHolder objectStatus = getStatus();
        objectStatus.set(DBObjectStatus.ENABLED, metadata.isEnabled());
        objectStatus.set(DBObjectStatus.VALID, metadata.isValid());
        objectStatus.set(DBObjectStatus.DEBUG, metadata.isDebug());
    }

    @Override
    public void initProperties() {
        properties.set(EDITABLE, true);
        properties.set(DISABLEABLE, true);
        properties.set(REFERENCEABLE, true);
        properties.set(COMPILABLE, true);
        properties.set(DEBUGABLE, true);
        properties.set(INVALIDABLE, true);
        properties.set(SCHEMA_OBJECT, true);
    }

    @Override
    public boolean isForEachRow() {
        return is(FOR_EACH_ROW);
    }

    @Override
    public DBTriggerType getTriggerType() {
        return triggerType;
    }

    @Override
    public DBTriggerEvent[] getTriggerEvents() {
        return triggerEvents;
    }

    @Override
    public DBOperationExecutor getOperationExecutor() {
        return operationType -> {
            ConnectionHandler connectionHandler = getConnectionHandler();
            DBNConnection connection = connectionHandler.getPoolConnection(getSchemaIdentifier(), false);
            try {
                DatabaseMetadataInterface metadataInterface = connectionHandler.getInterfaceProvider().getMetadataInterface();
                if (operationType == DBOperationType.ENABLE) {
                    metadataInterface.enableTrigger(getSchema().getName(), getName(), connection);
                    getStatus().set(DBObjectStatus.ENABLED, true);
                } else if (operationType == DBOperationType.DISABLE) {
                    metadataInterface.disableTrigger(getSchema().getName(), getName(), connection);
                    getStatus().set(DBObjectStatus.ENABLED, false);
                } else {
                    throw new DBUnsupportedOperationException(operationType, getObjectType());
                }
            } finally {
                connectionHandler.freePoolConnection(connection);
            }
        };
    }

    @Override
    public List<PresentableProperty> getPresentableProperties() {
        List<PresentableProperty> properties = super.getPresentableProperties();
        StringBuilder events = new StringBuilder(triggerType.getName().toLowerCase());
        events.append(" ");
        for (DBTriggerEvent triggeringEvent : triggerEvents) {
            if (triggeringEvent != triggerEvents[0]) events.append(" or ");
            events.append(triggeringEvent.getName().toUpperCase());
        }

        properties.add(0, new SimplePresentableProperty("Trigger event", events.toString()));
        return properties;
    }

    /*********************************************************
     *                     TreeElement                       *
     *********************************************************/

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    @NotNull
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return EMPTY_TREE_NODE_LIST;
    }

    private static DBObjectTimestampLoader TIMESTAMP_LOADER = new DBObjectTimestampLoader("TRIGGER");

    /*********************************************************
     *                   DBEditableObject                    *
     ********************************************************/

    @Override
    public String getCodeParseRootId(DBContentType contentType) {
        return "trigger_definition";
    }

    @Override
    public DBObjectTimestampLoader getTimestampLoader(DBContentType contentType) {
        return TIMESTAMP_LOADER;
    }

}
