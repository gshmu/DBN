package com.dci.intellij.dbn.object.factory.ui;

import com.dci.intellij.dbn.common.ui.component.DBNComponent;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.type.DBObjectType;
import org.jetbrains.annotations.NotNull;

public class ProcedureFactoryInputForm extends MethodFactoryInputForm {

    public ProcedureFactoryInputForm(@NotNull DBNComponent parent, DBSchema schema, DBObjectType objectType, int index) {
        super(parent, schema, objectType, index);
    }

    @Override
    public boolean hasReturnArgument() {
        return false;
    }
}