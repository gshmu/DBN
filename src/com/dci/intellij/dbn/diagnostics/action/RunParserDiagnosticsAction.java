package com.dci.intellij.dbn.diagnostics.action;

import com.dci.intellij.dbn.common.action.DumbAwareProjectAction;
import com.dci.intellij.dbn.diagnostics.Diagnostics;
import com.dci.intellij.dbn.diagnostics.ParserDiagnosticsManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class RunParserDiagnosticsAction extends DumbAwareProjectAction {
    public RunParserDiagnosticsAction() {
        super("Run Parser Diagnostics");
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        ParserDiagnosticsManager diagnosticsManager = ParserDiagnosticsManager.getInstance(project);
        diagnosticsManager.runParserDiagnostics();
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        e.getPresentation().setVisible(Diagnostics.isDeveloperMode());
    }


}
