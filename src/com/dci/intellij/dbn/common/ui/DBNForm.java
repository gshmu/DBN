package com.dci.intellij.dbn.common.ui;

import javax.swing.JComponent;

import com.dci.intellij.dbn.common.dispose.Disposable;

public interface DBNForm extends Disposable {
    JComponent getComponent();
}
