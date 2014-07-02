package com.dci.intellij.dbn.common.ui;

import com.dci.intellij.dbn.common.dispose.Disposable;

import javax.swing.JComponent;

public interface DBNForm extends Disposable {
    JComponent getComponent();
}
