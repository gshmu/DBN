package com.dci.intellij.dbn.common.ui.component;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public interface DBNComponent extends StatefulDisposable {
    @Nullable
    <T extends DBNComponent> T getParentComponent();

    @NotNull
    default <T extends DBNComponent> T ensureParentComponent() {
        return Failsafe.nn(getParentComponent());
    }

    @NotNull
    JComponent getComponent();

    @NotNull
    Project getProject();
}
