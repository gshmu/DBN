package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.DBProcedure;
import com.dci.intellij.dbn.object.DBProgram;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.loader.DBObjectTimestampLoader;
import com.dci.intellij.dbn.object.common.loader.DBSourceCodeLoader;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.dci.intellij.dbn.object.common.status.DBObjectStatusHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBProcedureImpl extends DBMethodImpl implements DBProcedure {
    DBProcedureImpl(DBSchemaObject parent, ResultSet resultSet) throws SQLException {
        // type functions are not editable independently
        super(parent, resultSet);
        assert this.getClass() != DBProcedureImpl.class;
    }

    DBProcedureImpl(DBSchema schema, ResultSet resultSet) throws SQLException {
        super(schema, resultSet);
    }

    @Override
    protected String initObject(ResultSet resultSet) throws SQLException {
        super.initObject(resultSet);
        return resultSet.getString("PROCEDURE_NAME");
    }

    @NotNull
    @Override
    public DBObjectType getObjectType() {
        return DBObjectType.PROCEDURE;
    }

    @Override
    @Nullable
    public Icon getIcon() {
        if (getContentType() == DBContentType.CODE) {
            DBObjectStatusHolder objectStatus = getStatus();
            if (objectStatus.is(DBObjectStatus.VALID)) {
                if (objectStatus.is(DBObjectStatus.DEBUG)){
                    return Icons.DBO_PROCEDURE_DEBUG;
                }
            } else {
                return Icons.DBO_PROCEDURE_ERR;
            }

        }
        return Icons.DBO_PROCEDURE;
    }

    @Override
    public Icon getOriginalIcon() {
        return Icons.DBO_PROCEDURE;
    }

    @Override
    public void buildToolTip(HtmlToolTipBuilder ttb) {
        ttb.append(true, getObjectType().getName(), true);
        ttb.createEmptyRow();
        super.buildToolTip(ttb);
    }


    @Override
    public DBProgram getProgram() {
        return null;
    }

    @Override
    public String getMethodType() {
        return "PROCEDURE";
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
            return getConnectionHandler().getInterfaceProvider().getMetadataInterface().loadObjectSourceCode(
                   getSchema().getName(), getName(), "PROCEDURE", getOverload(), connection);
        }
    }
    private static DBObjectTimestampLoader TIMESTAMP_LOADER = new DBObjectTimestampLoader("PROCEDURE") {};

    /*********************************************************
     *                  DBCompilableObject                   *
     *********************************************************/

    @Override
    public String loadCodeFromDatabase(DBContentType contentType) throws SQLException {
        return new SourceCodeLoader(this).load();
    }

    @Override
    public String getCodeParseRootId(DBContentType contentType) {
        return getParentObject() instanceof DBSchema && contentType == DBContentType.CODE ? "procedure_declaration" : null;
    }

    @Override
    public DBObjectTimestampLoader getTimestampLoader(DBContentType contentType) {
        return TIMESTAMP_LOADER;
    }

}