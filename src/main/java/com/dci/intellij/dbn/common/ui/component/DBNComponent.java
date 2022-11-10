package com.dci.intellij.dbn.common.ui.component;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.project.ProjectSupplier;
import com.intellij.openapi.Disposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public interface DBNComponent extends StatefulDisposable, ProjectSupplier {
    @Nullable
    <T extends Disposable> T parent();

    @NotNull
    JComponent getComponent();

    @NotNull
    default <T extends Disposable> T ensureParent() {
        return Failsafe.nn(parent());
    }


}
