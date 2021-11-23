package com.dci.intellij.dbn.diagnostics.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.diagnostics.ParserDiagnosticsManager;
import com.dci.intellij.dbn.diagnostics.data.ParserDiagnosticsResult;
import com.dci.intellij.dbn.diagnostics.ui.ParserDiagnosticsForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParserDiagnosticsRunAction extends AbstractParserDiagnosticsAction {
    public ParserDiagnosticsRunAction() {
        super("Run Diagnostics", Icons.ACTION_EXECUTE);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ParserDiagnosticsForm form) {
        Progress.modal(project, "Running Parser Diagnostics", true, progress -> {
            ParserDiagnosticsManager manager = getManager(project);
            ParserDiagnosticsResult result = manager.runParserDiagnostics(progress);
            form.refreshResults();
            form.selectResult(result);
        });
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable ParserDiagnosticsForm form) {
        presentation.setEnabled(!getManager(project).hasDraftResults());
    }
}
