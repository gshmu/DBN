package com.dci.intellij.dbn.execution.common.message;

import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.message.Message;
import com.dci.intellij.dbn.common.message.MessageType;
import com.intellij.openapi.Disposable;

public abstract class ConsoleMessage extends Message implements Disposable {
    private boolean isNew = true;

    public ConsoleMessage(MessageType type, String text) {
        super(type, text);
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    @Override
    public void disposeInner() {
        Disposer.nullify(this);
        super.disposeInner();
    }
}
