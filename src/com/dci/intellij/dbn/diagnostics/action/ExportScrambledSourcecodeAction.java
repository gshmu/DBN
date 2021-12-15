package com.dci.intellij.dbn.diagnostics.action;

import com.dci.intellij.dbn.common.action.DumbAwareProjectAction;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.diagnostics.Diagnostics;
import com.dci.intellij.dbn.diagnostics.ParserDiagnosticsManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.File;

@Slf4j
public class ExportScrambledSourcecodeAction extends DumbAwareProjectAction {
    public static final FileChooserDescriptor FILE_CHOOSER_DESCRIPTOR = new FileChooserDescriptor(false, true, false, false, false, false).
            withTitle("Select Destination Directory").
            withDescription("Select destination directory for the scrambled sources");

    public ExportScrambledSourcecodeAction() {
        super("Export Scrambled Sourcecode");
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        VirtualFile[] virtualFiles = FileChooser.chooseFiles(FILE_CHOOSER_DESCRIPTOR, project, null);
        if (virtualFiles.length == 1) {
            Progress.modal(project, "Running Project Code Scrambler", true, progress -> {
                progress.setIndeterminate(false);
                ParserDiagnosticsManager manager = ParserDiagnosticsManager.get(project);
                manager.scrambleProjectFiles(progress, new File(virtualFiles[0].getPath()));
            });
        }
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        e.getPresentation().setVisible(Diagnostics.isDeveloperMode());
    }


}
