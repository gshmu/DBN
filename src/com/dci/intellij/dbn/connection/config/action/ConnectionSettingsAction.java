package com.dci.intellij.dbn.connection.config.action;

import com.dci.intellij.dbn.common.action.DataKeys;
import com.dci.intellij.dbn.common.action.DumbAwareProjectAction;
import com.dci.intellij.dbn.connection.config.ui.ConnectionBundleSettingsForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class ConnectionSettingsAction extends DumbAwareProjectAction {
    public ConnectionSettingsAction(String text, Icon icon) {
        super(text, null, icon);
    }

    @Nullable
    ConnectionBundleSettingsForm getSettingsForm(AnActionEvent e) {
        return e.getData((DataKeys.CONNECTION_BUNDLE_SETTINGS));
    }
}
