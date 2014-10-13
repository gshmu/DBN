package com.dci.intellij.dbn.editor.code.options;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.options.CompositeProjectConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.editor.code.options.ui.CodeEditorSettingsForm;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.intellij.openapi.project.Project;

public class CodeEditorSettings extends CompositeProjectConfiguration<CodeEditorSettingsForm> {
    private CodeEditorGeneralSettings generalSettings = new CodeEditorGeneralSettings();

    public CodeEditorSettings(Project project) {
        super(project);
    }

    public static CodeEditorSettings getInstance(Project project) {
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
    protected Configuration<CodeEditorSettingsForm> getOriginalSettings() {
        return getInstance(getProject());
    }

    /*********************************************************
     *                        Custom                         *
     *********************************************************/

    public CodeEditorGeneralSettings getGeneralSettings() {
       return generalSettings;
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/
    public CodeEditorSettingsForm createConfigurationEditor() {
        return new CodeEditorSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "code-editor-settings";
    }

    protected Configuration[] createConfigurations() {
        return new Configuration[] {
                generalSettings};
    }
}
