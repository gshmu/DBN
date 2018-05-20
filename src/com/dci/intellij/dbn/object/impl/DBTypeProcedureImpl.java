package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.DBProgram;
import com.dci.intellij.dbn.object.DBType;
import com.dci.intellij.dbn.object.DBTypeProcedure;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DBTypeProcedureImpl extends DBProcedureImpl implements DBTypeProcedure {
    public DBTypeProcedureImpl(DBType type, ResultSet resultSet) throws SQLException {
        super(type, resultSet);
    }

    @Override
    public void initStatus(ResultSet resultSet) throws SQLException {}

    @Override
    public void initProperties() {
        properties.set(DBObjectProperty.NAVIGABLE, true);
    }

    @Override
    public DBContentType getContentType() {
        return DBContentType.NONE;
    }

    public DBType getType() {
        return (DBType) getParentObject();
    }

    @Override
    public DBProgram getProgram() {
        return getType();
    }    

    public boolean isProgramMethod() {
        return true;
    }

    @Override
    public DBObjectType getObjectType() {
        return DBObjectType.TYPE_PROCEDURE;
    }

    public void executeUpdateDDL(DBContentType contentType, String oldCode, String newCode) throws SQLException {}
}