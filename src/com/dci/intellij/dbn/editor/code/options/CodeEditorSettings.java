package com.dci.intellij.dbn.editor.code.options;

import com.dci.intellij.dbn.common.options.CompositeProjectConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.editor.code.options.ui.CodeEditorSettingsForm;
import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.dci.intellij.dbn.options.TopLevelConfig;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class CodeEditorSettings extends CompositeProjectConfiguration<CodeEditorSettingsForm> implements TopLevelConfig {
    private CodeEditorGeneralSettings generalSettings = new CodeEditorGeneralSettings();
    private CodeEditorConfirmationSettings confirmationSettings = new CodeEditorConfirmationSettings();

    public CodeEditorSettings(Project project) {
        super(project);
    }

    public static CodeEditorSettings getInstance(@NotNull Project project) {
        return ProjectSettingsManager.getSettings(project).getCodeEditorSettings();
    }

    @NotNull
    @Override
    public String getId() {
        return "DBNavigator.Project.CodeEditorSettings";
    }

    public String getDisplayName() {
        return "Code Editor";
    }

    public String getHelpTopic() {
        return "codeEditor";
    }

    @Override
    public ConfigId getConfigId() {
        return ConfigId.CODE_EDITOR;
    }

    @NotNull
    @Override
    public Configuration<CodeEditorSettingsForm> getOriginalSettings() {
        return getInstance(getProject());
    }

    /*********************************************************
     *                        Custom                         *
     *********************************************************/

    public CodeEditorGeneralSettings getGeneralSettings() {
       return generalSettings;
    }

    public CodeEditorConfirmationSettings getConfirmationSettings() {
        return confirmationSettings;
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/
    @NotNull
    public CodeEditorSettingsForm createConfigurationEditor() {
        return new CodeEditorSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "code-editor-settings";
    }

    protected Configuration[] createConfigurations() {
        return new Configuration[] {
            generalSettings,
            confirmationSettings};
    }
}
