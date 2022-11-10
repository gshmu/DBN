package com.dci.intellij.dbn.editor.data.action;

import com.dci.intellij.dbn.common.action.DumbAwareContextAction;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public abstract class AbstractDataEditorAction extends DumbAwareContextAction<DatasetEditor> {
    public AbstractDataEditorAction(String text) {
        super(text);
    }

    public AbstractDataEditorAction(String text, Icon icon) {
        super(text, null, icon);
    }

    @Override
    protected DatasetEditor getTarget(@NotNull AnActionEvent e) {
        return DatasetEditor.get(e);
    }
}
