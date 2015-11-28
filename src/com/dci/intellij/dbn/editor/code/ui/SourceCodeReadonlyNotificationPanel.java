package com.dci.intellij.dbn.editor.code.ui;

import com.dci.intellij.dbn.common.environment.EnvironmentManager;
import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.code.SourceCodeEditor;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.project.Project;

public class SourceCodeReadonlyNotificationPanel extends SourceCodeEditorNotificationPanel{
    public SourceCodeReadonlyNotificationPanel(final DBSchemaObject schemaObject, final SourceCodeEditor sourceCodeEditor) {
        super(isReadonly(sourceCodeEditor) ? MessageType.INFO : MessageType.WARNING);
        final DBSourceCodeVirtualFile sourceCodeFile = sourceCodeEditor.getVirtualFile();
        String environmentName = sourceCodeFile.getEnvironmentType().getName();

        final Project project = schemaObject.getProject();
        final DBContentType contentType = sourceCodeEditor.getContentType();

        if (isReadonly(sourceCodeEditor)) {
            setText("Readonly view - Code editing is disabled by default for \"" + environmentName + "\" environments (see configuration)");
            createActionLabel("Edit Mode", new Runnable() {
                @Override
                public void run() {
                    EnvironmentManager environmentManager = EnvironmentManager.getInstance(project);
                    environmentManager.enableEditing(schemaObject, contentType);
                }
            });
        } else {
            setText("Edit mode active! (the environment \"" + environmentName + "\" is configured with readonly code by default)");
            createActionLabel("Cancel Editing", new Runnable() {
                @Override
                public void run() {
                    EnvironmentManager environmentManager = EnvironmentManager.getInstance(project);
                    environmentManager.disableEditing(schemaObject, contentType);
                }
            });
        }

        createActionLabel("Settings", new Runnable() {
            @Override
            public void run() {
                ProjectSettingsManager settingsManager = ProjectSettingsManager.getInstance(project);
                settingsManager.openProjectSettings(ConfigId.GENERAL);
            }
        });
    }

    private static boolean isReadonly(SourceCodeEditor sourceCodeEditor) {
        Project project = sourceCodeEditor.getProject();
        EnvironmentManager environmentManager = EnvironmentManager.getInstance(project);
        return environmentManager.isReadonly(sourceCodeEditor.getVirtualFile());
    }
}
