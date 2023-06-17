package com.dci.intellij.dbn.diagnostics.action;

import com.dci.intellij.dbn.common.action.ToggleAction;
import com.dci.intellij.dbn.common.util.Messages;
import com.dci.intellij.dbn.diagnostics.Diagnostics;
import com.dci.intellij.dbn.diagnostics.DiagnosticsManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.common.dispose.Checks.isNotValid;

@Slf4j
public class DeveloperModeAction extends ToggleAction {
    public DeveloperModeAction() {
        super("Developer Mode");
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return Diagnostics.isDeveloperMode();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        Diagnostics.getDeveloperMode().setEnabled(state);

        if (Diagnostics.isDeveloperMode()) {
            Project project = getEventProject(e);
            if (isNotValid(project)) return;

            if (Diagnostics.hasEnabledFeatures()) {
                Messages.showWarningDialog(project,
                        "Developer Mode",
                        "Developer Mode has been activated\n" +
                                "(it will be automatically disabled after " + Diagnostics.getDeveloperMode().getTimeout() + " minutes)",
                        new String[]{"Ok", "Open Settings..."}, 0,
                        option -> {if (option == 1) openDiagnosticSettings(project);});

            } else {
                openDiagnosticSettings(project);
            }
        }
    }

    private static void openDiagnosticSettings(Project project) {
        DiagnosticsManager diagnosticsManager = DiagnosticsManager.getInstance(project);
        diagnosticsManager.openDiagnosticsSettings();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        Presentation presentation = e.getPresentation();
        presentation.setText("Developer Mode");
        //presentation.setText(Diagnostics.isDeveloperMode() ? "Developer Mode" + Diagnostics.getTimeoutText() : "Enable Developer Mode");
    }
}
