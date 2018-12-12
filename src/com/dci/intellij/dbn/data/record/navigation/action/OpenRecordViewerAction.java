package com.dci.intellij.dbn.data.record.navigation.action;

import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.editor.data.DatasetEditorManager;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterInput;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class OpenRecordViewerAction extends AnAction{
    private DatasetFilterInput filterInput;

    OpenRecordViewerAction(DatasetFilterInput filterInput) {
        super("Record Viewer");
        this.filterInput = filterInput;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = ActionUtil.ensureProject(e);
        DatasetEditorManager datasetEditorManager = DatasetEditorManager.getInstance(project);
        datasetEditorManager.openRecordViewer(filterInput);
    }
}
