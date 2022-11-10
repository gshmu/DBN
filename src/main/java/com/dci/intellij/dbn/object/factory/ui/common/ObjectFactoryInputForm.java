package com.dci.intellij.dbn.object.factory.ui.common;

import com.dci.intellij.dbn.common.ui.form.DBNFormBase;
import com.dci.intellij.dbn.common.ui.component.DBNComponent;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionRef;
import com.dci.intellij.dbn.object.factory.ObjectFactoryInput;
import com.dci.intellij.dbn.object.type.DBObjectType;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.swing.JPanel;

public abstract class ObjectFactoryInputForm<T extends ObjectFactoryInput> extends DBNFormBase {
    @Getter
    @Setter
    private int index;
    private final ConnectionRef connection;
    private final DBObjectType objectType;

    protected ObjectFactoryInputForm(@NotNull DBNComponent parent, @NotNull ConnectionHandler connection, DBObjectType objectType, int index) {
        super(parent);
        this.connection = connection.ref();
        this.objectType = objectType;
        this.index = index;
    }

    @NotNull
    @Override
    public abstract JPanel getMainComponent();

    @NotNull
    public ConnectionHandler getConnection() {
        return connection.ensure();
    }

    public DBObjectType getObjectType() {
        return objectType;
    }

    public abstract T createFactoryInput(ObjectFactoryInput parent);

    public abstract void focus();
}
