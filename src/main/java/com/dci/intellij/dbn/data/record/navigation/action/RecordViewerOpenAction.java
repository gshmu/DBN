package com.dci.intellij.dbn.data.record.navigation.action;

import com.dci.intellij.dbn.common.action.ProjectAction;
import com.dci.intellij.dbn.editor.data.DatasetEditorManager;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterInput;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class RecordViewerOpenAction extends ProjectAction {
    private DatasetFilterInput filterInput;

    RecordViewerOpenAction(DatasetFilterInput filterInput) {
        super("Record Viewer");
        this.filterInput = filterInput;
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        DatasetEditorManager datasetEditorManager = DatasetEditorManager.getInstance(project);
        datasetEditorManager.openRecordViewer(filterInput);
    }
}
