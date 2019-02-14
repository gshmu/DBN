package com.dci.intellij.dbn.browser.options;

import com.dci.intellij.dbn.browser.options.ui.DatabaseBrowserSettingsForm;
import com.dci.intellij.dbn.common.options.CompositeProjectConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.ProjectSettings;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.dci.intellij.dbn.options.TopLevelConfig;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class DatabaseBrowserSettings
        extends CompositeProjectConfiguration<ProjectSettings, DatabaseBrowserSettingsForm>
        implements TopLevelConfig {

    private DatabaseBrowserGeneralSettings generalSettings = new DatabaseBrowserGeneralSettings(this);
    private DatabaseBrowserFilterSettings filterSettings   = new DatabaseBrowserFilterSettings(this);
    private DatabaseBrowserSortingSettings sortingSettings = new DatabaseBrowserSortingSettings(this);
    private DatabaseBrowserEditorSettings editorSettings   = new DatabaseBrowserEditorSettings(this);

    public DatabaseBrowserSettings(ProjectSettings parent) {
        super(parent);
    }

    @NotNull
    @Override
    public DatabaseBrowserSettingsForm createConfigurationEditor() {
        return new DatabaseBrowserSettingsForm(this);
    }

    public static DatabaseBrowserSettings getInstance(@NotNull Project project) {
        return ProjectSettingsManager.getSettings(project).getBrowserSettings();
    }

    @NotNull
    @Override
    public String getId() {
        return "DBNavigator.Project.DatabaseBrowserSettings";
    }

    @Override
    public String getDisplayName() {
        return "Database Browser";
    }

    @Override
    public String getHelpTopic() {
        return "browserSettings";
    }

    @Override
    public ConfigId getConfigId() {
        return ConfigId.BROWSER;
    }

    @NotNull
    @Override
    public DatabaseBrowserSettings getOriginalSettings() {
        return getInstance(getProject());
    }

    /*********************************************************
     *                        Custom                         *
     *********************************************************/

    public DatabaseBrowserGeneralSettings getGeneralSettings() {
        return generalSettings;
    }

    public DatabaseBrowserFilterSettings getFilterSettings() {
        return filterSettings;
    }

    public DatabaseBrowserSortingSettings getSortingSettings() {
        return sortingSettings;
    }

    public DatabaseBrowserEditorSettings getEditorSettings() {
        return editorSettings;
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/

    @Override
    protected Configuration[] createConfigurations() {
        return new Configuration[] {
                generalSettings,
                filterSettings,
                sortingSettings,
                editorSettings};
    }

    @Override
    public String getConfigElementName() {
        return "browser-settings";
    }
}
