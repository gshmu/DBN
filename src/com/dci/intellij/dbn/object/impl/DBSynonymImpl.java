package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBSynonym;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBSchemaObjectImpl;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationListImpl;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.object.properties.DBObjectPresentableProperty;
import com.dci.intellij.dbn.object.properties.PresentableProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.*;

public class DBSynonymImpl extends DBSchemaObjectImpl implements DBSynonym {
    private DBObjectRef<DBObject> underlyingObject;

    DBSynonymImpl(DBSchema schema, ResultSet resultSet) throws SQLException {
        super(schema, resultSet);
    }

    @Override
    protected String initObject(ResultSet resultSet) throws SQLException {
        String name = resultSet.getString("SYNONYM_NAME");
        String schemaName = resultSet.getString("OBJECT_OWNER");
        String objectName = resultSet.getString("OBJECT_NAME");
        DBObjectType objectType = DBObjectType.get(resultSet.getString("OBJECT_TYPE"), DBObjectType.ANY);

        ConnectionHandler connectionHandler = getConnectionHandler();
        DBSchema schema = connectionHandler.getObjectBundle().getSchema(schemaName);
        if (schema != null) {
            DBObjectRef schemaRef = schema.getRef();
            underlyingObject = new DBObjectRef<DBObject>(schemaRef, objectType, objectName);
        }

        return name;
    }

    @Override
    public void initStatus(ResultSet resultSet) throws SQLException {
        boolean valid = resultSet.getString("IS_VALID").equals("Y");
        getStatus().set(DBObjectStatus.VALID, valid);
    }

    @Override
    public void initProperties() {
        properties.set(SCHEMA_OBJECT, true);
        properties.set(REFERENCEABLE, true);
        properties.set(INVALIDABLE, true);
    }

    @Override
    public DBObjectType getObjectType() {
        return DBObjectType.SYNONYM;
    }

    @Nullable
    @Override
    public DBObject getDefaultNavigationObject() {
        return getUnderlyingObject();
    }

    @Override
    @Nullable
    public Icon getIcon() {
        if (getStatus().is(DBObjectStatus.VALID)) {
            return Icons.DBO_SYNONYM;
        } else {
            return Icons.DBO_SYNONYM_ERR;
        }
    }

    @Override
    public Icon getOriginalIcon() {
        return Icons.DBO_SYNONYM;
    }

    @Override
    @Nullable
    public DBObject getUnderlyingObject() {
        return DBObjectRef.get(underlyingObject);
    }

    @Override
    public String getNavigationTooltipText() {
        DBObject parentObject = getParentObject();
        if (parentObject == null) {
            return "unknown " + getTypeName();
        } else {
            DBObject underlyingObject = getUnderlyingObject();
            if (underlyingObject == null) {
                return "unknown " + getTypeName() +
                        " (" + parentObject.getTypeName() + " " + parentObject.getName() + ")";
            } else {
                return getTypeName() + " of " + underlyingObject.getName() + " " + underlyingObject.getTypeName() +
                        " (" + parentObject.getTypeName() + " " + parentObject.getName() + ")";

            }

        }
    }

    @Override
    protected List<DBObjectNavigationList> createNavigationLists() {
        List<DBObjectNavigationList> objectNavigationLists = super.createNavigationLists();

        DBObject underlyingObject = getUnderlyingObject();
        if (underlyingObject != null) {
            DBObjectNavigationListImpl objectNavigationList = new DBObjectNavigationListImpl("Underlying " + underlyingObject.getTypeName(), underlyingObject);
            objectNavigationLists.add(objectNavigationList);
        }

        return objectNavigationLists;
    }

    @Override
    public void buildToolTip(HtmlToolTipBuilder ttb) {
        DBObject underlyingObject = getUnderlyingObject();
        if (underlyingObject!= null) {
            ttb.append(true, underlyingObject.getObjectType().getName() + " ", true);
        }
        ttb.append(false, getObjectType().getName(), true);
        ttb.createEmptyRow();
        super.buildToolTip(ttb);
    }

    @Override
    public List<PresentableProperty> getPresentableProperties() {
        List<PresentableProperty> properties = super.getPresentableProperties();
        DBObject underlyingObject = getUnderlyingObject();
        if (underlyingObject != null) {
            properties.add(0, new DBObjectPresentableProperty("Underlying object", underlyingObject, true));
        }
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


}
