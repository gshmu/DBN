package com.dci.intellij.dbn.common.ui;

import com.dci.intellij.dbn.common.dispose.DisposableProjectComponent;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public interface DBNForm extends DisposableProjectComponent {
    JComponent getComponent();

    @Nullable
    JComponent getPreferredFocusedComponent();
}
