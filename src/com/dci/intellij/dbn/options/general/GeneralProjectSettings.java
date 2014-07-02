package com.dci.intellij.dbn.options.general;

import com.dci.intellij.dbn.common.environment.options.EnvironmentSettings;
import com.dci.intellij.dbn.common.locale.options.RegionalSettings;
import com.dci.intellij.dbn.common.options.CompositeProjectConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.options.general.ui.GeneralProjectSettingsForm;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class GeneralProjectSettings extends CompositeProjectConfiguration<GeneralProjectSettingsForm> {
    private RegionalSettings regionalSettings;
    private EnvironmentSettings environmentSettings;

    public GeneralProjectSettings(Project project) {
        super(project);
        regionalSettings = new RegionalSettings();
        environmentSettings = new EnvironmentSettings(project);
    }

    public static GeneralProjectSettings getInstance(Project project) {
        return getGlobalProjectSettings(project).getGeneralSettings();
    }

    @NotNull
    @Override
    public String getId() {
        return "DBNavigator.Project.GeneralSettings";
    }

    public String getDisplayName() {
        return "General";
    }

    /*********************************************************
    *                        Custom                         *
    *********************************************************/
    public RegionalSettings getRegionalSettings() {
        return regionalSettings;
    }

    public EnvironmentSettings getEnvironmentSettings() {
        return environmentSettings;
    }

    /*********************************************************
     *                      Configuration                    *
     *********************************************************/
    public GeneralProjectSettingsForm createConfigurationEditor() {
        return new GeneralProjectSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "general-settings";
    }

    protected Configuration[] createConfigurations() {
        return new Configuration[] {regionalSettings, environmentSettings};
    }

}
