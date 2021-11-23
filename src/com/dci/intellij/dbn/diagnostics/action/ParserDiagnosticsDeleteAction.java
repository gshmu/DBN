package com.dci.intellij.dbn.diagnostics.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.diagnostics.ParserDiagnosticsManager;
import com.dci.intellij.dbn.diagnostics.data.ParserDiagnosticsResult;
import com.dci.intellij.dbn.diagnostics.ui.ParserDiagnosticsForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParserDiagnosticsDeleteAction extends AbstractParserDiagnosticsAction {
    public ParserDiagnosticsDeleteAction() {
        super("Run Diagnostics", Icons.ACTION_DELETE);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ParserDiagnosticsForm form) {
        ParserDiagnosticsResult result = form.getSelectedResult();
        if (result != null) {
            MessageUtil.showQuestionDialog(project,
                    "Delete diagnostics result",
                    "Are you sure you want to delete the diagnostic result " + result.getName(),
                    MessageUtil.OPTIONS_YES_NO, 0,
                    option -> {
                        if (option == 0) {
                            ParserDiagnosticsManager manager = getManager(project);
                            manager.deleteResult(result);
                            ParserDiagnosticsResult latestResult = manager.getLatestResult();
                            form.refreshResults();
                            form.selectResult(latestResult);
                        }
                    });
        }


    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable ParserDiagnosticsForm form) {
        if (form != null) {
            ParserDiagnosticsResult result = form.getSelectedResult();
            presentation.setEnabled(result != null);
        } else {
            presentation.setEnabled(false);
        }

    }
}
