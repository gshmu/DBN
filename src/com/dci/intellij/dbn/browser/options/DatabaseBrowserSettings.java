package com.dci.intellij.dbn.browser.options;

import com.dci.intellij.dbn.browser.options.ui.DatabaseBrowserSettingsForm;
import com.dci.intellij.dbn.common.options.CompositeProjectConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

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
        return getGlobalProjectSettings(project).getBrowserSettings();
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
        return new Configuration[] {generalSettings, filterSettings};
    }

    @Override
    public String getConfigElementName() {
        return "browser-settings";
    }

    public void readConfiguration(Element element) {
        readConfiguration(element, generalSettings);
        readConfiguration(element, filterSettings);
    }

    public void writeConfiguration(Element element) {
        writeConfiguration(element, generalSettings);
        writeConfiguration(element, filterSettings);
    }

}
