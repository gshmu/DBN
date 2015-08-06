package com.dci.intellij.dbn.editor.session.action;

import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.action.OpenSettingsAction;

public class SessionBrowserSettingsAction extends OpenSettingsAction {
    public SessionBrowserSettingsAction() {
        super(ConfigId.OPERATIONS, true);
    }
}
