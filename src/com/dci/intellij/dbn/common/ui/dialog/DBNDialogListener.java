package com.dci.intellij.dbn.common.ui.dialog;

public interface DBNDialogListener {
    enum Action{OPEN, CLOSE};

    void onAction(Action action);
}
