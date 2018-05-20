package com.dci.intellij.dbn.data.editor.ui;

import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;

import javax.swing.*;

public class BasicDataEditorComponent extends JTextField implements DataEditorComponent{
    private UserValueHolder userValueHolder;
    public JTextField getTextField() {
        return this;
    }

    public void setUserValueHolder(UserValueHolder userValueHolder) {
        this.userValueHolder = userValueHolder;
    }

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
    public void dispose() {
        if (!disposed) {
            disposed = true;
            userValueHolder = null;
        }
    }

    @Override
    public void checkDisposed() {
        if (disposed) throw AlreadyDisposedException.INSTANCE;
    }
}
