package com.dci.intellij.dbn.editor.code.ui;

import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.editor.code.SourceCodeEditor;
import com.dci.intellij.dbn.editor.code.SourceCodeManagerAdapter;
import com.dci.intellij.dbn.editor.code.SourceCodeManagerListener;
import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.AsyncProcessIcon;

import javax.swing.*;
import java.awt.*;

public class SourceCodeEditorActionsPanel extends DBNFormImpl{
    private JPanel mainPanel;
    private JPanel actionsPanel;
    private JPanel loadingDataPanel;
    private JLabel loadingLabel;
    private JPanel loadingIconPanel;

    private SourceCodeEditor sourceCodeEditor;

    public SourceCodeEditorActionsPanel(SourceCodeEditor sourceCodeEditor) {
        this.sourceCodeEditor = sourceCodeEditor;
        Editor editor = sourceCodeEditor.getEditor();
        Project project = editor.getProject();
        JComponent editorComponent = editor.getComponent();
        ActionToolbar actionToolbar = ActionUtil.createActionToolbar("", true, "DBNavigator.ActionGroup.SourceEditor");
        actionToolbar.setTargetComponent(editorComponent);
        actionsPanel.add(actionToolbar.getComponent(), BorderLayout.CENTER);
        loadingIconPanel.add(new AsyncProcessIcon("Loading"), BorderLayout.CENTER);
        loadingDataPanel.setVisible(false);

        EventUtil.subscribe(project, this, SourceCodeManagerListener.TOPIC, sourceCodeManagerListener);
    }

    SourceCodeManagerListener sourceCodeManagerListener = new SourceCodeManagerAdapter() {
        @Override
        public void sourceCodeLoading(DBSourceCodeVirtualFile sourceCodeFile) {
            DBSourceCodeVirtualFile virtualFile = sourceCodeEditor.getVirtualFile();
            if (virtualFile != null && virtualFile.equals(sourceCodeFile)) {
                new SimpleLaterInvocator() {
                    @Override
                    protected void execute() {
                        loadingDataPanel.setVisible(true);
                    }
                }.start();
            }
        }

        @Override
        public void sourceCodeLoaded(DBSourceCodeVirtualFile sourceCodeFile, boolean initialLoad) {
            DBSourceCodeVirtualFile virtualFile = sourceCodeEditor.getVirtualFile();
            if (virtualFile != null && virtualFile.equals(sourceCodeFile)) {
                new SimpleLaterInvocator() {
                    @Override
                    protected void execute() {
                        loadingDataPanel.setVisible(false);
                    }
                }.start();

            }
        }
    };

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    @Override
    public void dispose() {
        super.dispose();
        sourceCodeEditor = null;
    }
}
