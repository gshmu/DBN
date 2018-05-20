package com.dci.intellij.dbn.editor.data.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.dci.intellij.dbn.editor.data.DatasetLoadInstructions;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.editor.data.DatasetLoadInstruction.DELIBERATE_ACTION;
import static com.dci.intellij.dbn.editor.data.DatasetLoadInstruction.PRESERVE_CHANGES;
import static com.dci.intellij.dbn.editor.data.DatasetLoadInstruction.USE_CURRENT_FILTER;

public class ReloadDataAction extends AbstractDataEditorAction {

    private static final DatasetLoadInstructions LOAD_INSTRUCTIONS = new DatasetLoadInstructions(USE_CURRENT_FILTER, PRESERVE_CHANGES, DELIBERATE_ACTION);

    public ReloadDataAction() {
        super("Reload", Icons.DATA_EDITOR_RELOAD_DATA);
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        DatasetEditor datasetEditor = getDatasetEditor(e);
        if (datasetEditor != null) {
            datasetEditor.loadData(LOAD_INSTRUCTIONS);
        }
    }

    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Reload");
        DatasetEditor datasetEditor = getDatasetEditor(e);

        boolean enabled =
                datasetEditor != null &&
                datasetEditor.isLoaded() &&
                !datasetEditor.isInserting() &&
                !datasetEditor.isLoading();
        presentation.setEnabled(enabled);

    }
}