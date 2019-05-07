package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseDDLInterface;
import com.dci.intellij.dbn.database.DatabaseInterface;
import com.dci.intellij.dbn.database.common.metadata.def.DBViewMetadata;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBType;
import com.dci.intellij.dbn.object.DBView;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationListImpl;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import com.dci.intellij.dbn.object.type.DBObjectType;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DBViewImpl extends DBDatasetImpl<DBViewMetadata> implements DBView {
    private DBType type;
    DBViewImpl(DBSchema schema, DBViewMetadata metadata) throws SQLException {
        super(schema, metadata);
    }

    @Override
    protected String initObject(DBViewMetadata metadata) throws SQLException {
        String name = metadata.getViewName();
        set(DBObjectProperty.SYSTEM_OBJECT, metadata.isSystemView());
        String typeOwner = metadata.getViewTypeOwner();
        String typeName = metadata.getViewType();
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
     *                  DBEditableCodeObject                 *
     ********************************************************/

    @Override
    public void executeUpdateDDL(DBContentType contentType, String oldCode, String newCode) throws SQLException {
        DatabaseInterface.run(this,
                (interfaceProvider) -> {
                    ConnectionHandler connectionHandler = getConnectionHandler();
                    DBNConnection connection = connectionHandler.getPoolConnection(getSchemaIdentifier(), false);
                    try {
                        DatabaseDDLInterface ddlInterface = interfaceProvider.getDDLInterface();
                        ddlInterface.updateView(getName(), newCode, connection);
                    } finally {
                        connectionHandler.freePoolConnection(connection);
                    }
                });
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
