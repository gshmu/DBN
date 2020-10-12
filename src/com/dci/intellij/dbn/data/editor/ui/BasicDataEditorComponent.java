package com.dci.intellij.dbn.data.editor.ui;

import lombok.Getter;
import lombok.Setter;

import javax.swing.*;

public class BasicDataEditorComponent extends JTextField implements DataEditorComponent{
    @Getter
    @Setter
    private UserValueHolder<?> userValueHolder;

    @Override
    public JTextField getTextField() {
        return this;
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
    @Getter
    private boolean disposed;

    @Override
    public void dispose() {
        userValueHolder = null;
    }
}
