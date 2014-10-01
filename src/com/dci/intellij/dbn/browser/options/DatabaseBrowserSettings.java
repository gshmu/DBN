package com.dci.intellij.dbn.browser.options;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.browser.options.ui.DatabaseBrowserSettingsForm;
import com.dci.intellij.dbn.common.options.CompositeProjectConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.intellij.openapi.project.Project;

public class DatabaseBrowserSettings extends CompositeProjectConfiguration<DatabaseBrowserSettingsForm> {
    private DatabaseBrowserGeneralSettings generalSettings;
    private DatabaseBrowserFilterSettings filterSettings;

    public DatabaseBrowserSettings(Project project) {
        super(project);
        filterSettings = new DatabaseBrowserFilterSettings(project);
        generalSettings = new DatabaseBrowserGeneralSettings(project);
    }

    @Override
    public DatabaseBrowserSettingsForm createConfigurationEditor() {
        return new DatabaseBrowserSettingsForm(this);
    }

    public static DatabaseBrowserSettings getInstance(Project project) {
        return ProjectSettingsManager.getSettings(project).getBrowserSettings();
    }

    @NotNull
    @Override
    public String getId() {
        return "DBNavigator.Project.DatabaseBrowserSettings";
    }

    public String getDisplayName() {
        return "Database Browser";
    }

    public String getHelpTopic() {
        return "browserSettings";
    }

    @Override
    protected Configuration<DatabaseBrowserSettingsForm> getOriginalSettings() {
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

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/

    @Override
    protected Configuration[] createConfigurations() {
        return new Configuration[] {
                generalSettings,
                filterSettings};
    }

    @Override
    public String getConfigElementName() {
        return "browser-settings";
    }
}
