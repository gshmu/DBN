package com.dci.intellij.dbn.data.grid.options;

import com.dci.intellij.dbn.common.options.CompositeProjectConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.data.grid.options.ui.DataGridSettingsForm;
import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.ProjectSettings;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.dci.intellij.dbn.options.TopLevelConfig;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class DataGridSettings extends CompositeProjectConfiguration<ProjectSettings, DataGridSettingsForm> implements TopLevelConfig {
    private DataGridGeneralSettings generalSettings               = new DataGridGeneralSettings(this);
    private DataGridSortingSettings sortingSettings               = new DataGridSortingSettings(this);
    private DataGridTrackingColumnSettings trackingColumnSettings = new DataGridTrackingColumnSettings(this);

    public DataGridSettings(ProjectSettings parent) {
        super(parent);
    }

    public static DataGridSettings getInstance(@NotNull Project project) {
        return ProjectSettingsManager.getSettings(project).getDataGridSettings();
    }

    @NotNull
    @Override
    public String getId() {
        return "DBNavigator.Project.DataGridSettings";
    }

    @Override
    public String getDisplayName() {
        return "Data Grid";
    }

    @Override
    public String getHelpTopic() {
        return "dataGrid";
    }

    @Override
    public ConfigId getConfigId() {
        return ConfigId.DATA_GRID;
    }

    @NotNull
    @Override
    public DataGridSettings getOriginalSettings() {
        return getInstance(getProject());
    }

    /*********************************************************
     *                        Custom                         *
     *********************************************************/

    public DataGridGeneralSettings getGeneralSettings() {
        return generalSettings;
    }

    public DataGridSortingSettings getSortingSettings() {
        return sortingSettings;
    }

    public DataGridTrackingColumnSettings getTrackingColumnSettings() {
       return trackingColumnSettings;
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/
    @Override
    @NotNull
    public DataGridSettingsForm createConfigurationEditor() {
        return new DataGridSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "dataset-grid-settings";
    }

    @Override
    protected Configuration[] createConfigurations() {
        return new Configuration[] {
                generalSettings,
                sortingSettings,
                trackingColumnSettings
        };
    }
}
