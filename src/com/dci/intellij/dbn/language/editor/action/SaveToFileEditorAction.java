package com.dci.intellij.dbn.language.editor.action;

import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.thread.WriteActionRunner;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.vfs.DBConsoleVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class SaveToFileEditorAction extends DumbAwareAction {
    public SaveToFileEditorAction() {
        super("Save to file", "Save console to file", Icons.CODE_EDITOR_SAVE_TO_FILE);
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        final Project project = ActionUtil.getProject(e);
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (project != null && virtualFile instanceof DBConsoleVirtualFile) {
            final DBConsoleVirtualFile consoleVirtualFile = (DBConsoleVirtualFile) virtualFile;

            FileSaverDescriptor fileSaverDescriptor = new FileSaverDescriptor(
                    Constants.DBN_TITLE_PREFIX + "Save Console to File",
                    "Save content of the console \"" + consoleVirtualFile.getName() + "\" to file", "sql");

            FileSaverDialog fileSaverDialog = FileChooserFactory.getInstance().createSaveFileDialog(fileSaverDescriptor, project);
            final Document document = DocumentUtil.getDocument(virtualFile);
            final VirtualFileWrapper virtualFileWrapper = fileSaverDialog.save(null, consoleVirtualFile.getName());
            if (document != null && virtualFileWrapper != null) {
                new WriteActionRunner() {
                    @Override
                    public void run() {
                        try {
                            VirtualFile newVirtualFile = virtualFileWrapper.getVirtualFile(true);
                            if (newVirtualFile != null) {
                                newVirtualFile.setBinaryContent(document.getCharsSequence().toString().getBytes());
                                FileConnectionMappingManager fileConnectionMappingManager = FileConnectionMappingManager.getInstance(project);
                                fileConnectionMappingManager.setConnectionHandler(newVirtualFile, consoleVirtualFile.getConnectionHandler());
                                fileConnectionMappingManager.setDatabaseSchema(newVirtualFile, consoleVirtualFile.getDatabaseSchema());

                                FileEditorManager.getInstance(project).openFile(newVirtualFile, true);
                            }
                        } catch (IOException e1) {
                            MessageUtil.showErrorDialog(project, "Error saving to file", "Could not save console content to file \"" + virtualFileWrapper.getFile().getName() + "\"", e1);
                        }
                    }
                }.start();
            }
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        Presentation presentation = e.getPresentation();
        presentation.setText("Save to File");
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        presentation.setVisible(virtualFile instanceof DBConsoleVirtualFile);
        presentation.setEnabled(true);
        presentation.setVisible(isVisible(e));
    }

    public static boolean isVisible(AnActionEvent e) {
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        return virtualFile instanceof DBConsoleVirtualFile && !DatabaseDebuggerManager.isDebugConsole(virtualFile);
    }

}