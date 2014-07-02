package com.dci.intellij.dbn.editor.data.action;

import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public abstract class AbstractDataEditorAction extends DumbAwareAction {
    public AbstractDataEditorAction(String text) {
        super(text);
    }

    public AbstractDataEditorAction(String text, Icon icon) {
        super(text, null, icon);
    }

    public static DatasetEditor getActiveDatasetEditor(Project project) {
        if (project != null) {
            FileEditor[] fileEditors = FileEditorManager.getInstance(project).getSelectedEditors();
            for (FileEditor fileEditor : fileEditors) {
                if (fileEditor instanceof DatasetEditor && GUIUtil.isFocused(fileEditor.getComponent(), true)) {
                    return (DatasetEditor) fileEditor;
                }
            }
        }
        return null;
    }

    @Nullable
    public static DatasetEditor getDatasetEditor(AnActionEvent e) {
        FileEditor fileEditor = e.getData(PlatformDataKeys.FILE_EDITOR);
        if (fileEditor instanceof DatasetEditor) {
            return (DatasetEditor) fileEditor;
        }
        return null;
    }
}
