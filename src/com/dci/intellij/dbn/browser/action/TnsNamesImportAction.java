package com.dci.intellij.dbn.browser.action;


import com.dci.intellij.dbn.common.action.DumbAwareProjectAction;
import com.dci.intellij.dbn.connection.config.tns.TnsNamesParser;
import com.dci.intellij.dbn.connection.config.tns.ui.TnsNamesImportDialog;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class TnsNamesImportAction extends DumbAwareProjectAction {
    TnsNamesImportAction() {
        super("Import TNS Names", null, null);
    }

    @Override
    protected void actionPerformed(
            @NotNull AnActionEvent e,
            @NotNull Project project) {

        VirtualFile[] virtualFiles = FileChooser.chooseFiles(TnsNamesParser.FILE_CHOOSER_DESCRIPTOR, project, null);
        if (virtualFiles.length == 1) {
            File file = new File(virtualFiles[0].getPath());
            TnsNamesImportDialog dialog = new TnsNamesImportDialog(project, file);
            dialog.show();
            int exitCode = dialog.getExitCode();
            if (exitCode == DialogWrapper.OK_EXIT_CODE) {
                ProjectSettingsManager settingsManager = ProjectSettingsManager.getInstance(project);
                settingsManager.createConnections(dialog.getTnsNames());
            }
        }

    }
}
