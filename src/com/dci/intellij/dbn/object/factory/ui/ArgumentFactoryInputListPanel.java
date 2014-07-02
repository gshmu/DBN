package com.dci.intellij.dbn.object.factory.ui;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.factory.ArgumentFactoryInput;
import com.dci.intellij.dbn.object.factory.ui.common.ObjectFactoryInputForm;
import com.dci.intellij.dbn.object.factory.ui.common.ObjectListForm;

public class ArgumentFactoryInputListPanel extends ObjectListForm<ArgumentFactoryInput> {
    private boolean enforceInArguments;
    public ArgumentFactoryInputListPanel(ConnectionHandler connectionHandler, boolean enforceInArguments) {
        super(connectionHandler);
        this.enforceInArguments = enforceInArguments;
    }

    public ObjectFactoryInputForm createObjectDetailsPanel(int index) {
        return new ArgumentFactoryInputForm(getConnectionHandler(), enforceInArguments, index);
    }

    public DBObjectType getObjectType() {
        return DBObjectType.ARGUMENT;
    }
}
