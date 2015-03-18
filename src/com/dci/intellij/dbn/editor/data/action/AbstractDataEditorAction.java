package com.dci.intellij.dbn.editor.data.action;

import javax.swing.Icon;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.action.DBNDataKeys;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.DumbAwareAction;

public abstract class AbstractDataEditorAction extends DumbAwareAction {
    public AbstractDataEditorAction(String text) {
        super(text);
    }

    public AbstractDataEditorAction(String text, Icon icon) {
        super(text, null, icon);
    }


    @Nullable
    public static DatasetEditor getDatasetEditor(DataContext dataContext) {
        DatasetEditor datasetEditor = DBNDataKeys.DATASET_EDITOR.getData(dataContext);
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
        DatasetEditor datasetEditor = e.getData((DBNDataKeys.DATASET_EDITOR));
        if (datasetEditor == null) {
            FileEditor fileEditor = e.getData(PlatformDataKeys.FILE_EDITOR);
            if (fileEditor instanceof DatasetEditor) {
                return (DatasetEditor) fileEditor;
            }
        } else {
            return datasetEditor;
        }
        return null;
    }
}
