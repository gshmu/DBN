package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.DBFunction;
import com.dci.intellij.dbn.object.DBProgram;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.loader.DBObjectTimestampLoader;
import com.dci.intellij.dbn.object.common.loader.DBSourceCodeLoader;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.dci.intellij.dbn.object.common.status.DBObjectStatusHolder;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBFunctionImpl extends DBMethodImpl implements DBFunction {
    DBFunctionImpl(DBSchemaObject parent, ResultSet resultSet) throws SQLException {
        // type functions are not editable independently
        super(parent, resultSet);
        assert this.getClass() != DBFunctionImpl.class;
    }

    DBFunctionImpl(DBSchema schema, ResultSet resultSet) throws SQLException {
        super(schema, resultSet);
    }

    @Override
    protected String initObject(ResultSet resultSet) throws SQLException {
        super.initObject(resultSet);
        return resultSet.getString("FUNCTION_NAME");
    }

    @Override
    public DBArgument getReturnArgument() {
        for (DBArgument argument : getArguments()) {
            if (argument.getPosition() == 1) {
                return argument;
            }
        }
        return null;
    }

    @Override
    public DBObjectType getObjectType() {
        return DBObjectType.FUNCTION;
    }

    @Override
    @Nullable
    public Icon getIcon() {
        if (getContentType() == DBContentType.CODE) {
            DBObjectStatusHolder objectStatus = getStatus();
            if (objectStatus.is(DBObjectStatus.VALID)) {
                if (objectStatus.is(DBObjectStatus.DEBUG)){
                    return Icons.DBO_FUNCTION_DEBUG;
                }
            } else {
                return Icons.DBO_FUNCTION_ERR;
            }

        }
        return Icons.DBO_FUNCTION;
    }

    @Override
    public Icon getOriginalIcon() {
        return Icons.DBO_FUNCTION;
    }

    @Override
    public DBProgram getProgram() {
        return null;
    }

    @Override
    public String getMethodType() {
        return "FUNCTION";
    }

    @Override
    public void buildToolTip(HtmlToolTipBuilder ttb) {
        ttb.append(true, getObjectType().getName(), true);
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
            return getConnectionHandler().getInterfaceProvider().getMetadataInterface().loadObjectSourceCode(
                   getSchema().getName(),
                   getName(),
                   "FUNCTION",
                   getOverload(),
                   connection);
        }
    }

    private static DBObjectTimestampLoader TIMESTAMP_LOADER = new DBObjectTimestampLoader("FUNCTION") {};

    /*********************************************************
     *              DBEditableCodeSchemaObject               *
     *********************************************************/

    @Override
    public String loadCodeFromDatabase(DBContentType contentType) throws SQLException {
        return new SourceCodeLoader(this).load();
    }

    @Override
    public String getCodeParseRootId(DBContentType contentType) {
        return getParentObject() instanceof DBSchema && contentType == DBContentType.CODE ? "function_declaration" : null;
    }

    @Override
    public DBObjectTimestampLoader getTimestampLoader(DBContentType contentType) {
        return TIMESTAMP_LOADER;
    }

}

