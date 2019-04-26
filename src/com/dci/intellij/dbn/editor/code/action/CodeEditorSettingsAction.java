package com.dci.intellij.dbn.editor.code.action;

import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.action.ProjectSettingsOpenAction;

public class CodeEditorSettingsAction extends ProjectSettingsOpenAction {

    public CodeEditorSettingsAction() {
        super(ConfigId.CODE_EDITOR, true);
    }
}
