package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.DBFunction;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBProcedure;
import com.dci.intellij.dbn.object.DBProgram;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.DBSchemaObjectImpl;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.dci.intellij.dbn.object.common.status.DBObjectStatusHolder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.*;

public abstract class DBProgramImpl<P extends DBProcedure, F extends DBFunction>
        extends DBSchemaObjectImpl implements DBProgram<P, F> {
    protected DBObjectList<P> procedures;
    protected DBObjectList<F> functions;


    DBProgramImpl(DBSchemaObject parent, ResultSet resultSet) throws SQLException {
        super(parent, resultSet);
    }

    DBProgramImpl(DBSchema schema, ResultSet resultSet) throws SQLException {
        super(schema, resultSet);
    }

    @Override
    public void initProperties() {
        super.initProperties();
        properties.set(INVALIDABLE, true);
        properties.set(COMPILABLE, true);
        properties.set(DEBUGABLE, true);
    }

    @Override
    public void initStatus(ResultSet resultSet) throws SQLException {
        String specValidString = resultSet.getString("IS_SPEC_VALID");
        String bodyValidString = resultSet.getString("IS_BODY_VALID");

        String specDebugString = resultSet.getString("IS_SPEC_DEBUG");
        String bodyDebugString = resultSet.getString("IS_BODY_DEBUG");

        DBObjectStatusHolder objectStatus = getStatus();

        boolean specPresent = specValidString != null;
        boolean specValid = !specPresent || specValidString.equals("Y");
        boolean specDebug = !specPresent || specDebugString.equals("Y");

        boolean bodyPresent = bodyValidString != null;
        boolean bodyValid = !bodyPresent || bodyValidString.equals("Y");
        boolean bodyDebug = !bodyPresent || bodyDebugString.equals("Y");

        objectStatus.set(DBContentType.CODE_SPEC, DBObjectStatus.PRESENT, specPresent);
        objectStatus.set(DBContentType.CODE_SPEC, DBObjectStatus.VALID, specValid);
        objectStatus.set(DBContentType.CODE_SPEC, DBObjectStatus.DEBUG, specDebug);

        objectStatus.set(DBContentType.CODE_BODY, DBObjectStatus.PRESENT, bodyPresent);
        objectStatus.set(DBContentType.CODE_BODY, DBObjectStatus.VALID, bodyValid);
        objectStatus.set(DBContentType.CODE_BODY, DBObjectStatus.DEBUG, bodyDebug);

    }

    @Override
    public List<F> getFunctions() {
        return functions.getObjects();
    }

    @Override
    public List<P> getProcedures() {
        return procedures.getObjects();
    }

    @Override
    public F getFunction(String name, int overload) {
        for (F function : functions.getObjects()){
            if (function.getName().equals(name) && function.getOverload() == overload) {
                return function;
            }
        }
        return null;
    }

    @Override
    public P getProcedure(String name, int overload) {
        for (P procedure : procedures.getObjects()){
            if (procedure.getName().equals(name) && procedure.getOverload() == overload) {
                return procedure;
            }
        }
        return null;
    }

    @Override
    public DBMethod getMethod(String name, int overload) {
        DBMethod method = getProcedure(name, overload);
        if (method == null) method = getFunction(name, overload);
        return method;
    }

    @Override
    public boolean isEmbedded() {
        return false;
    }

    @Override
    public boolean isEditable(DBContentType contentType) {
        return getContentType() == DBContentType.CODE_SPEC_AND_BODY && (
                contentType == DBContentType.CODE_SPEC ||
                contentType == DBContentType.CODE_BODY);
    }
}
