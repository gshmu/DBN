package com.dci.intellij.dbn.common.ui.component;

import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.project.ProjectSupplier;
import com.intellij.openapi.Disposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static com.dci.intellij.dbn.common.dispose.Failsafe.nn;

public interface DBNComponent extends StatefulDisposable, ProjectSupplier {
    @Nullable
    <T extends Disposable> T getParentComponent();

    @NotNull
    default <T extends Disposable> T ensureParentComponent() {
        return nn(getParentComponent());
    }

    @NotNull
    JComponent getComponent();


}
