package com.dci.intellij.dbn.ddl.options;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.options.CompositeProjectConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.ddl.options.ui.DDFileSettingsForm;
import com.intellij.openapi.project.Project;

public class DDLFileSettings extends CompositeProjectConfiguration<DDFileSettingsForm> {
    private DDLFileExtensionSettings extensionSettings;
    private DDLFileGeneralSettings generalSettings;

    public DDLFileSettings(Project project) {
        super(project);
        extensionSettings = new DDLFileExtensionSettings(project);
        generalSettings = new DDLFileGeneralSettings();
    }

    public static DDLFileSettings getInstance(Project project) {
        return getGlobalProjectSettings(project).getDdlFileSettings();
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

    public String getDisplayName() {
        return "DDL Files";
    }

    public String getHelpTopic() {
        return "ddlFileSettings";
    }
    /********************************************************
    *                     Configuration                     *
    *********************************************************/
    public DDFileSettingsForm createConfigurationEditor() {
        return new DDFileSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "ddl-file-settings";
    }

    protected Configuration[] createConfigurations() {
        return new Configuration[] {
                extensionSettings,
                generalSettings};
    }
}
