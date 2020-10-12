package com.dci.intellij.dbn.common.ui;

import com.dci.intellij.dbn.common.event.ProjectEventAdapter;
import com.dci.intellij.dbn.common.ui.component.DBNComponent;
import com.intellij.openapi.actionSystem.DataProvider;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public interface DBNForm extends DBNComponent, DataProvider, ProjectEventAdapter.Provided {

    @Nullable
    default JComponent getPreferredFocusedComponent() {return null;}

}
