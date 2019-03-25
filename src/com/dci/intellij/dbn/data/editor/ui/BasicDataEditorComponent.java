package com.dci.intellij.dbn.data.editor.ui;

import javax.swing.*;

public class BasicDataEditorComponent extends JTextField implements DataEditorComponent{
    private UserValueHolder userValueHolder;
    @Override
    public JTextField getTextField() {
        return this;
    }

    @Override
    public void setUserValueHolder(UserValueHolder userValueHolder) {
        this.userValueHolder = userValueHolder;
    }

    @Override
    public UserValueHolder getUserValueHolder() {
        return userValueHolder;
    }

    @Override
    public void setEditable(boolean editable) {
        super.setEditable(editable);
    }

    @Override
    public void setEnabled(boolean enabled) {
        setEditable(enabled);
    }

    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    private boolean disposed;

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public void markDisposed() {
        disposed = true;
    }

    @Override
    public void disposeInner() {
        userValueHolder = null;
    }
}
