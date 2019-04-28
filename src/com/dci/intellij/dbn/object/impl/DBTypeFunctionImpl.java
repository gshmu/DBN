package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.database.common.metadata.def.DBFunctionMetadata;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.DBProgram;
import com.dci.intellij.dbn.object.DBType;
import com.dci.intellij.dbn.object.DBTypeFunction;
import com.dci.intellij.dbn.object.type.DBObjectType;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.NAVIGABLE;

public class DBTypeFunctionImpl extends DBFunctionImpl implements DBTypeFunction {
    DBTypeFunctionImpl(DBType type, DBFunctionMetadata metadata) throws SQLException {
        super(type, metadata);
    }

    @Override
    public void initStatus(DBFunctionMetadata metadata) throws SQLException {}

    @Override
    public void initProperties() {
        properties.set(NAVIGABLE, true);
    }

    @Override
    public DBType getType() {
        return (DBType) getParentObject();
    }

    @Override
    public DBProgram getProgram() {
        return getType();
    }

    @Override
    public boolean isProgramMethod() {
        return true;
    }

    @NotNull
    @Override
    public DBObjectType getObjectType() {
        return DBObjectType.TYPE_FUNCTION;
    }

    @Override
    public void executeUpdateDDL(DBContentType contentType, String oldCode, String newCode) throws SQLException {}
}