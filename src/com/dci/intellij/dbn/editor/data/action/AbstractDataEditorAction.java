package com.dci.intellij.dbn.editor.data.action;

import com.dci.intellij.dbn.common.action.DataKeys;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class AbstractDataEditorAction extends DumbAwareAction {
    public AbstractDataEditorAction(String text) {
        super(text);
    }

    public AbstractDataEditorAction(String text, Icon icon) {
        super(text, null, icon);
    }


    @Nullable
    public static DatasetEditor getDatasetEditor(DataContext dataContext) {
        DatasetEditor datasetEditor = DataKeys.DATASET_EDITOR.getData(dataContext);
        if (datasetEditor == null) {
            FileEditor fileEditor = PlatformDataKeys.FILE_EDITOR.getData(dataContext);
            if (fileEditor instanceof DatasetEditor) {
                return (DatasetEditor) fileEditor;
            }
        }
        return datasetEditor;
    }


    @Nullable
    public static DatasetEditor getDatasetEditor(AnActionEvent e) {
        DatasetEditor datasetEditor = e.getData((DataKeys.DATASET_EDITOR));
        if (datasetEditor == null) {
            FileEditor fileEditor = ActionUtil.getFileEditor(e);
            if (fileEditor instanceof DatasetEditor) {
                return (DatasetEditor) fileEditor;
            }
        } else {
            return datasetEditor;
        }
        return null;
    }
}
