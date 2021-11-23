package com.dci.intellij.dbn.diagnostics.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.diagnostics.DiagnosticsManager;
import com.dci.intellij.dbn.diagnostics.data.DiagnosticCategory;
import com.dci.intellij.dbn.diagnostics.ui.ParserDiagnosticsForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ParserDiagnosticsCloseAction extends AbstractParserDiagnosticsAction {
    public ParserDiagnosticsCloseAction() {
        super("Close", Icons.ACTION_CLOSE);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ParserDiagnosticsForm form) {
        DiagnosticsManager diagnosticsManager = DiagnosticsManager.getInstance(project);
        diagnosticsManager.closeDiagnosticsConsole(DiagnosticCategory.PARSER);
    }
}
