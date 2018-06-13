package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.DBPackage;
import com.dci.intellij.dbn.object.DBPackageProcedure;
import com.dci.intellij.dbn.object.DBProgram;
import com.dci.intellij.dbn.object.common.DBObjectType;

import java.sql.ResultSet;
import java.sql.SQLException;

import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.NAVIGABLE;

public class DBPackageProcedureImpl extends DBProcedureImpl implements DBPackageProcedure {
    DBPackageProcedureImpl(DBPackage packagee, ResultSet resultSet) throws SQLException {
        super(packagee, resultSet);
    }

    @Override
    public void initStatus(ResultSet resultSet) throws SQLException {}

    @Override
    public void initProperties() {
        properties.set(NAVIGABLE, true);
    }

    @Override
    public DBContentType getContentType() {
        return DBContentType.NONE;
    }

    public DBPackage getPackage() {
        return (DBPackage) getParentObject();
    }

    @Override
    public DBProgram getProgram() {
        return getPackage();
    }


    public boolean isProgramMethod() {
        return true;
    }

    @Override
    public DBObjectType getObjectType() {
        return DBObjectType.PACKAGE_PROCEDURE;
    }

    public void executeUpdateDDL(DBContentType contentType, String oldCode, String newCode) throws SQLException {}
}