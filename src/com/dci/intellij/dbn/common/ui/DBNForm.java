package com.dci.intellij.dbn.common.ui;

import javax.swing.JComponent;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.dispose.DisposableProjectComponent;

public interface DBNForm extends DisposableProjectComponent {
    JComponent getComponent();

    @Nullable
    JComponent getPreferredFocusedComponent();
}
