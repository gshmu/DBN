package com.dci.intellij.dbn.editor.data.ui;

import com.dci.intellij.dbn.common.environment.EnvironmentManager;
import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.intellij.openapi.project.Project;

public class DatasetEditorReadonlyNotificationPanel extends DatasetEditorNotificationPanel{
    public DatasetEditorReadonlyNotificationPanel(final DBSchemaObject schemaObject) {
        super(isReadonly(schemaObject) ? MessageType.INFO : MessageType.WARNING);
        String environmentName = schemaObject.getEnvironmentType().getName();
        final Project project = schemaObject.getProject();

        if (isReadonly(schemaObject)) {
            setText("Readonly data - Editing is disabled by default for \"" + environmentName + "\" environments (see configuration)");
            createActionLabel("Edit Mode", new Runnable() {
                @Override
                public void run() {
                    EnvironmentManager environmentManager = EnvironmentManager.getInstance(project);
                    environmentManager.enableEditing(schemaObject, DBContentType.DATA);
                }
            });
        } else {
            setText("Edit mode active! (the environment \"" + environmentName + "\" is configured with readonly data by default)");
            createActionLabel("Cancel Editing", new Runnable() {
                @Override
                public void run() {
                    EnvironmentManager environmentManager = EnvironmentManager.getInstance(project);
                    environmentManager.disableEditing(schemaObject, DBContentType.DATA);
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

    private static boolean isReadonly(DBSchemaObject schemaObject) {
        Project project = schemaObject.getProject();
        EnvironmentManager environmentManager = EnvironmentManager.getInstance(project);
        return environmentManager.isReadonly(schemaObject, DBContentType.DATA);
    }
}
