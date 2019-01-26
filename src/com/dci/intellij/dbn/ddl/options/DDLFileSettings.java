package com.dci.intellij.dbn.ddl.options;

import com.dci.intellij.dbn.common.options.CompositeProjectConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.ddl.options.ui.DDFileSettingsForm;
import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.dci.intellij.dbn.options.TopLevelConfig;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class DDLFileSettings extends CompositeProjectConfiguration<DDFileSettingsForm> implements TopLevelConfig {
    private DDLFileExtensionSettings extensionSettings;
    private DDLFileGeneralSettings generalSettings;

    public DDLFileSettings(Project project) {
        super(project);
        extensionSettings = new DDLFileExtensionSettings(this);
        generalSettings = new DDLFileGeneralSettings(this);
    }

    public static DDLFileSettings getInstance(@NotNull Project project) {
        return ProjectSettingsManager.getSettings(project).getDdlFileSettings();
    }

    public DDLFileExtensionSettings getExtensionSettings() {
        return extensionSettings;
    }

    public DDLFileGeneralSettings getGeneralSettings() {
        return generalSettings;
    }

    @NotNull
    @Override
    public String getId() {
        return "DBNavigator.Project.DDLFileSettings";
    }

    @Override
    public String getDisplayName() {
        return "DDL Files";
    }

    @Override
    public String getHelpTopic() {
        return "ddlFileSettings";
    }

    @Override
    public ConfigId getConfigId() {
        return ConfigId.DDL_FILES;
    }

    @NotNull
    @Override
    public Configuration<DDFileSettingsForm> getOriginalSettings() {
        return getInstance(getProject());
    }

    /********************************************************
    *                     Configuration                     *
    *********************************************************/
    @Override
    @NotNull
    public DDFileSettingsForm createConfigurationEditor() {
        return new DDFileSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "ddl-file-settings";
    }

    @Override
    protected Configuration[] createConfigurations() {
        return new Configuration[] {
                extensionSettings,
                generalSettings};
    }
}
