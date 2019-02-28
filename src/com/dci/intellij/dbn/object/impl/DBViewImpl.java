package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseDDLInterface;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBType;
import com.dci.intellij.dbn.object.DBView;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationListImpl;
import com.dci.intellij.dbn.object.common.loader.DBSourceCodeLoader;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DBViewImpl extends DBDatasetImpl implements DBView {
    private DBType type;
    DBViewImpl(DBSchema schema, ResultSet resultSet) throws SQLException {
        super(schema, resultSet);
    }

    @Override
    protected String initObject(ResultSet resultSet) throws SQLException {
        String name = resultSet.getString("VIEW_NAME");
        set(DBObjectProperty.SYSTEM_OBJECT, resultSet.getString("IS_SYSTEM_VIEW").equals("Y"));
        String typeOwner = resultSet.getString("VIEW_TYPE_OWNER");
        String typeName = resultSet.getString("VIEW_TYPE");
        if (typeOwner != null && typeName != null) {
            DBObjectBundle objectBundle = getConnectionHandler().getObjectBundle();
            DBSchema typeSchema = objectBundle.getSchema(typeOwner);
            type = typeSchema == null ? null : typeSchema.getType(typeName);
        }
        return name;
    }

    @NotNull
    @Override
    public DBObjectType getObjectType() {
        return DBObjectType.VIEW;
    }

    @Override
    public DBType getType() {
        return type;
    }

    /*********************************************************
     *                     TreeElement                       *
     *********************************************************/
    @Override
    @NotNull
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return DatabaseBrowserUtils.createList(
                columns,
                constraints,
                triggers);
    }

    @Override
    public boolean isEditable(DBContentType contentType) {
        return contentType == DBContentType.CODE;
    }

    @Override
    protected List<DBObjectNavigationList> createNavigationLists() {
        List<DBObjectNavigationList> objectNavigationLists = new ArrayList<DBObjectNavigationList>();

        if (type != null) {
            objectNavigationLists.add(new DBObjectNavigationListImpl<DBType>("Type", type));
        }
        return objectNavigationLists;
    }

    @Override
    public boolean isSystemView() {
        return is(DBObjectProperty.SYSTEM_OBJECT);
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
            DatabaseMetadataInterface metadataInterface = getConnectionHandler().getInterfaceProvider().getMetadataInterface();
            String ownerName = getSchema().getName();
            String viewName = getName();
            return metadataInterface.loadViewSourceCode(
                    ownerName,
                    viewName,
                    connection);
        }
    }

    /*********************************************************
     *                  DBEditableCodeObject                 *
     ********************************************************/

    @Override
    public String loadCodeFromDatabase(DBContentType contentType) throws SQLException {
        SourceCodeLoader loader = new SourceCodeLoader(this);
        return loader.load();
    }

    @Override
    public void executeUpdateDDL(DBContentType contentType, String oldCode, String newCode) throws SQLException {
        ConnectionHandler connectionHandler = getConnectionHandler();
        DBNConnection connection = connectionHandler.getPoolConnection(getSchemaIdentifier(), false);
        try {
            DatabaseDDLInterface ddlInterface = connectionHandler.getInterfaceProvider().getDDLInterface();
            ddlInterface.updateView(getName(), newCode, connection);
        } finally {
            connectionHandler.freePoolConnection(connection);
        }
    }

    @Override
    public String getCodeParseRootId(DBContentType contentType) {
        return "subquery";
    }

    @Override
    public DBLanguage getCodeLanguage(DBContentType contentType) {
        return SQLLanguage.INSTANCE;
    }
}
