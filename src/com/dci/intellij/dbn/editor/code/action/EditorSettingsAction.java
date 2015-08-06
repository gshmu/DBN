package com.dci.intellij.dbn.editor.code.action;

import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.action.OpenSettingsAction;

public class EditorSettingsAction extends OpenSettingsAction {

    public EditorSettingsAction() {
        super(ConfigId.CODE_EDITOR, true);
    }
}
