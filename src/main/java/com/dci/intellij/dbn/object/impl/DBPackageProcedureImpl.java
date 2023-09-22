package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.database.common.metadata.def.DBProcedureMetadata;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.DBPackage;
import com.dci.intellij.dbn.object.DBPackageProcedure;
import com.dci.intellij.dbn.object.DBProgram;
import com.dci.intellij.dbn.object.type.DBObjectType;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.NAVIGABLE;

class DBPackageProcedureImpl extends DBProcedureImpl implements DBPackageProcedure {
    DBPackageProcedureImpl(DBPackage packagee, DBProcedureMetadata metadata) throws SQLException {
        super(packagee, metadata);
    }

    @Override
    public void initStatus(DBProcedureMetadata metadata) throws SQLException {}

    @Override
    public void initProperties() {
        properties.set(NAVIGABLE, true);
    }

    @Override
    public DBPackage getPackage() {
        return (DBPackage) getParentObject();
    }

    @Override
    public DBProgram getProgram() {
        return getPackage();
    }


    @Override
    public boolean isProgramMethod() {
        return true;
    }

    @NotNull
    @Override
    public DBObjectType getObjectType() {
        return DBObjectType.PACKAGE_PROCEDURE;
    }

    @Override
    public void executeUpdateDDL(DBContentType contentType, String oldCode, String newCode) throws SQLException {}
}