package com.dci.intellij.dbn.object.factory.ui;

import com.dci.intellij.dbn.data.type.ui.DataTypeEditor;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.factory.ArgumentFactoryInput;
import com.dci.intellij.dbn.object.factory.MethodFactoryInput;
import com.dci.intellij.dbn.object.factory.ObjectFactoryInput;

public class FunctionFactoryInputForm extends MethodFactoryInputForm {

    public FunctionFactoryInputForm(DBSchema schema, DBObjectType objectType, int index) {
        super(schema, objectType, index);
    }

    public MethodFactoryInput createFactoryInput(ObjectFactoryInput parent) {
        MethodFactoryInput methodFactoryInput = super.createFactoryInput(parent);

        DataTypeEditor returnTypeEditor = (DataTypeEditor) returnArgumentDataTypeEditor;

        ArgumentFactoryInput returnArgument = new ArgumentFactoryInput(
                methodFactoryInput, 0, "return",
                returnTypeEditor.getDataTypeRepresentation(),
                false, true);

        methodFactoryInput.setReturnArgument(returnArgument);
        return methodFactoryInput;
    }

    public boolean hasReturnArgument() {
        return true;
    }

    public void dispose() {
        super.dispose();
    }
}
