package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.database.common.metadata.def.DBFunctionMetadata;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.DBPackage;
import com.dci.intellij.dbn.object.DBPackageFunction;
import com.dci.intellij.dbn.object.DBProgram;
import com.dci.intellij.dbn.object.common.DBObjectType;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.NAVIGABLE;

public class DBPackageFunctionImpl extends DBFunctionImpl implements DBPackageFunction {


    DBPackageFunctionImpl(DBPackage packagee, DBFunctionMetadata metadata) throws SQLException {
        super(packagee, metadata);
    }

    @Override
    public void initStatus(DBFunctionMetadata metadata) throws SQLException {}

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
        return DBObjectType.PACKAGE_FUNCTION;
    }

    @Override
    public void executeUpdateDDL(DBContentType contentType, String oldCode, String newCode) throws SQLException {}
}
