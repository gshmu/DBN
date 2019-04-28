package com.dci.intellij.dbn.object.factory.ui;

import com.dci.intellij.dbn.common.dispose.DisposableProjectComponent;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.factory.ArgumentFactoryInput;
import com.dci.intellij.dbn.object.factory.ui.common.ObjectFactoryInputForm;
import com.dci.intellij.dbn.object.factory.ui.common.ObjectListForm;
import com.dci.intellij.dbn.object.type.DBObjectType;

public class ArgumentFactoryInputListForm extends ObjectListForm<ArgumentFactoryInput> {
    private boolean enforceInArguments;
    public ArgumentFactoryInputListForm(DisposableProjectComponent parentComponent, ConnectionHandler connectionHandler, boolean enforceInArguments) {
        super(parentComponent, connectionHandler);
        this.enforceInArguments = enforceInArguments;
    }

    @Override
    public ObjectFactoryInputForm createObjectDetailsPanel(int index) {
        return new ArgumentFactoryInputForm(getProject(), getConnectionHandler(), enforceInArguments, index);
    }

    @Override
    public DBObjectType getObjectType() {
        return DBObjectType.ARGUMENT;
    }
}
