package com.dci.intellij.dbn.object.factory.ui;

import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectType;

public class ProcedureFactoryInputForm extends MethodFactoryInputForm {

    public ProcedureFactoryInputForm(DBSchema schema, DBObjectType objectType, int index) {
        super(schema, objectType, index);
    }

    public boolean hasReturnArgument() {
        return false;
    }

    public void dispose() {
        super.dispose();
    }
}