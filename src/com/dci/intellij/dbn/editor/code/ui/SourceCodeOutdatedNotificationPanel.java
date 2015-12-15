package com.dci.intellij.dbn.editor.code.ui;

import java.sql.Timestamp;

import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.editor.code.SourceCodeContent;
import com.dci.intellij.dbn.editor.code.SourceCodeEditor;
import com.dci.intellij.dbn.editor.code.SourceCodeManager;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.project.Project;
import com.intellij.util.text.DateFormatUtil;

public class SourceCodeOutdatedNotificationPanel extends SourceCodeEditorNotificationPanel{
    public SourceCodeOutdatedNotificationPanel(final DBSourceCodeVirtualFile sourceCodeFile, final SourceCodeEditor sourceCodeEditor) {
        super(MessageType.WARNING);
        final DBSchemaObject editableObject = sourceCodeFile.getObject();
        final Project project = editableObject.getProject();
        Timestamp timestamp = sourceCodeFile.getChangedInDatabaseTimestamp();
        setText("Outdated version. The " + editableObject.getQualifiedNameWithType() + " was modified by another user (" + DateFormatUtil.formatPrettyDateTime(timestamp).toLowerCase() + ")");
        createActionLabel("Show Diff", new Runnable() {
            @Override
            public void run() {
                if (!project.isDisposed()) {
                    SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
                    sourceCodeManager.opedDatabaseDiffWindow(sourceCodeFile);
                }
            }
        });

        if (sourceCodeFile.isModified()) {
            createActionLabel("Merge", new Runnable() {
                @Override
                public void run() {
                    if (!project.isDisposed()) {
                        try {
                            SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
                            SourceCodeContent sourceCodeContent = sourceCodeManager.loadSourceFromDatabase(editableObject, sourceCodeEditor.getContentType());
                            String databaseContent = sourceCodeContent.getText().toString();
                            sourceCodeManager.openCodeMergeDialog(databaseContent, sourceCodeFile, sourceCodeEditor, false);
                        }catch (Exception e) {
                            MessageUtil.showErrorDialog(project, "Could not load sources from database.", e);

                        }
                    }
                }
            });
        }

        createActionLabel(sourceCodeFile.isModified() ? "Revert local changes" : "Reload", new Runnable() {
            @Override
            public void run() {
                if (!project.isDisposed()) {
                    SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
                    sourceCodeManager.loadSourceCode(sourceCodeFile, true);
                }
            }
        });
    }
}
