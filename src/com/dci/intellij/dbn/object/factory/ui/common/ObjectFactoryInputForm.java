package com.dci.intellij.dbn.object.factory.ui.common;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.factory.ObjectFactoryInput;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public abstract class ObjectFactoryInputForm<T extends ObjectFactoryInput> extends DBNFormImpl {
    private int index;
    private ConnectionHandler connectionHandler;
    private DBObjectType objectType;

    protected ObjectFactoryInputForm(Project project, ConnectionHandler connectionHandler, DBObjectType objectType, int index) {
        super(project);
        this.connectionHandler = connectionHandler;
        this.objectType = objectType;
        this.index = index;
    }

    @NotNull
    @Override
    public abstract JPanel ensureComponent();

    public ConnectionHandler getConnectionHandler() {
        return connectionHandler;
    }

    public DBObjectType getObjectType() {
        return objectType;
    }

    public abstract T createFactoryInput(ObjectFactoryInput parent);

    public abstract void focus();

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
