package com.dci.intellij.dbn.object.factory.ui;

import com.dci.intellij.dbn.common.ui.component.DBNComponent;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.factory.ArgumentFactoryInput;
import com.dci.intellij.dbn.object.factory.ui.common.ObjectFactoryInputForm;
import com.dci.intellij.dbn.object.factory.ui.common.ObjectListForm;
import com.dci.intellij.dbn.object.type.DBObjectType;

public class ArgumentFactoryInputListForm extends ObjectListForm<ArgumentFactoryInput> {
    private final boolean enforceInArguments;
    public ArgumentFactoryInputListForm(DBNComponent parent, ConnectionHandler connectionHandler, boolean enforceInArguments) {
        super(parent, connectionHandler);
        this.enforceInArguments = enforceInArguments;
    }

    @Override
    public ObjectFactoryInputForm<ArgumentFactoryInput> createObjectDetailsPanel(int index) {
        return new ArgumentFactoryInputForm(this, getConnectionHandler(), enforceInArguments, index);
    }

    @Override
    public DBObjectType getObjectType() {
        return DBObjectType.ARGUMENT;
    }
}
