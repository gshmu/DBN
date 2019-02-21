package com.dci.intellij.dbn.common.ui;

import com.dci.intellij.dbn.common.dispose.DisposableProjectComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public interface DBNForm extends DisposableProjectComponent {
    @NotNull
    JComponent getComponent();

    @Nullable
    default JComponent getPreferredFocusedComponent() {return null;}
}
