package com.dci.intellij.dbn.navigation.options;

import com.dci.intellij.dbn.common.options.CompositeProjectConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.navigation.options.ui.NavigationSettingsForm;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class NavigationSettings extends CompositeProjectConfiguration<NavigationSettingsForm> {
    private ObjectsLookupSettings objectsLookupSettings;

    public NavigationSettings(Project project) {
        super(project);
        objectsLookupSettings = new ObjectsLookupSettings(project);
    }

    public static NavigationSettings getInstance(Project project) {
        return getGlobalProjectSettings(project).getNavigationSettings();
    }

    @NotNull
    @Override
    public String getId() {
        return "DBNavigator.Project.NavigationSettings";
    }

    public String getDisplayName() {
        return "Navigation";
    }

    public String getHelpTopic() {
        return "navigationSettings";
    }

    /*********************************************************
     *                        Custom                         *
     *********************************************************/

    public ObjectsLookupSettings getObjectsLookupSettings() {
        return objectsLookupSettings;
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/
    @Override
    public NavigationSettingsForm createConfigurationEditor() {
        return new NavigationSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "navigation-settings";
    }

    @Override
    protected Configuration[] createConfigurations() {
        return new Configuration[] {objectsLookupSettings};
    }
}
