package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBTrigger;
import com.dci.intellij.dbn.object.common.DBSchemaObjectImpl;
import com.dci.intellij.dbn.object.common.loader.DBObjectTimestampLoader;
import com.dci.intellij.dbn.object.common.operation.DBOperationExecutor;
import com.dci.intellij.dbn.object.common.operation.DBOperationNotSupportedException;
import com.dci.intellij.dbn.object.common.operation.DBOperationType;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.dci.intellij.dbn.object.common.status.DBObjectStatusHolder;
import com.dci.intellij.dbn.object.properties.PresentableProperty;
import com.dci.intellij.dbn.object.properties.SimplePresentableProperty;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.*;

public abstract class DBTriggerImpl extends DBSchemaObjectImpl implements DBTrigger {
    private TriggerType triggerType;
    private TriggeringEvent[] triggeringEvents;

    DBTriggerImpl(DBSchema schema, ResultSet resultSet) throws SQLException {
        super(schema, resultSet);
    }

    DBTriggerImpl(DBDataset dataset, ResultSet resultSet) throws SQLException {
        super(dataset, resultSet);
    }

    @Override
    protected String initObject(ResultSet resultSet) throws SQLException {
        String name = resultSet.getString("TRIGGER_NAME");
        set(FOR_EACH_ROW, resultSet.getString("IS_FOR_EACH_ROW").equals("Y"));

        String triggerTypeString = resultSet.getString("TRIGGER_TYPE");
        triggerType =
                triggerTypeString.contains("BEFORE") ? TRIGGER_TYPE_BEFORE :
                triggerTypeString.contains("AFTER") ? TRIGGER_TYPE_AFTER :
                triggerTypeString.contains("INSTEAD OF") ? TRIGGER_TYPE_INSTEAD_OF :
                                        TRIGGER_TYPE_UNKNOWN;


        String triggeringEventString = resultSet.getString("TRIGGERING_EVENT");
        List<TriggeringEvent> triggeringEventList = new ArrayList<TriggeringEvent>();
        if (triggeringEventString.contains("INSERT")) triggeringEventList.add(TRIGGERING_EVENT_INSERT);
        if (triggeringEventString.contains("UPDATE")) triggeringEventList.add(TRIGGERING_EVENT_UPDATE);
        if (triggeringEventString.contains("DELETE")) triggeringEventList.add(TRIGGERING_EVENT_DELETE);
        if (triggeringEventString.contains("TRUNCATE")) triggeringEventList.add(TRIGGERING_EVENT_TRUNCATE);
        if (triggeringEventString.contains("CREATE")) triggeringEventList.add(TRIGGERING_EVENT_CREATE);
        if (triggeringEventString.contains("ALTER")) triggeringEventList.add(TRIGGERING_EVENT_ALTER);
        if (triggeringEventString.contains("DROP")) triggeringEventList.add(TRIGGERING_EVENT_DROP);
        if (triggeringEventString.contains("RENAME")) triggeringEventList.add(TRIGGERING_EVENT_RENAME);
        if (triggeringEventString.contains("LOGON")) triggeringEventList.add(TRIGGERING_EVENT_LOGON);
        if (triggeringEventString.contains("DDL")) triggeringEventList.add(TRIGGERING_EVENT_DDL);
        if (triggeringEventList.size() == 0) triggeringEventList.add(TRIGGERING_EVENT_UNKNOWN);

        triggeringEvents = triggeringEventList.toArray(new TriggeringEvent[0]);
        return name;
    }

    @Override
    public void initStatus(ResultSet resultSet) throws SQLException {
        boolean isEnabled = resultSet.getString("IS_ENABLED").equals("Y");
        boolean isValid = resultSet.getString("IS_VALID").equals("Y");
        boolean isDebug = resultSet.getString("IS_DEBUG").equals("Y");
        DBObjectStatusHolder objectStatus = getStatus();
        objectStatus.set(DBObjectStatus.ENABLED, isEnabled);
        objectStatus.set(DBObjectStatus.VALID, isValid);
        objectStatus.set(DBObjectStatus.DEBUG, isDebug);
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
    public TriggerType getTriggerType() {
        return triggerType;
    }

    @Override
    public TriggeringEvent[] getTriggeringEvents() {
        return triggeringEvents;
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
                    throw new DBOperationNotSupportedException(operationType, getObjectType());
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
        for (TriggeringEvent triggeringEvent : triggeringEvents) {
            if (triggeringEvent != triggeringEvents[0]) events.append(" or ");
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
