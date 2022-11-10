package com.dci.intellij.dbn.editor.session.action;

import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.action.ProjectSettingsOpenAction;

public class SessionBrowserSettingsAction extends ProjectSettingsOpenAction {
    public SessionBrowserSettingsAction() {
        super(ConfigId.OPERATIONS, true);
    }
}
