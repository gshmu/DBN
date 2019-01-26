package com.dci.intellij.dbn.navigation.options;

import com.dci.intellij.dbn.common.options.CompositeProjectConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.navigation.options.ui.NavigationSettingsForm;
import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.dci.intellij.dbn.options.TopLevelConfig;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class NavigationSettings extends CompositeProjectConfiguration<NavigationSettingsForm> implements TopLevelConfig {
    private ObjectsLookupSettings objectsLookupSettings;

    public NavigationSettings(Project project) {
        super(project);
        objectsLookupSettings = new ObjectsLookupSettings(project);
    }

    public static NavigationSettings getInstance(@NotNull Project project) {
        return ProjectSettingsManager.getSettings(project).getNavigationSettings();
    }

    @NotNull
    @Override
    public String getId() {
        return "DBNavigator.Project.NavigationSettings";
    }

    @Override
    public String getDisplayName() {
        return "Navigation";
    }

    @Override
    public String getHelpTopic() {
        return "navigationSettings";
    }

    @Override
    public ConfigId getConfigId() {
        return ConfigId.NAVIGATION;
    }

    @NotNull
    @Override
    public Configuration<NavigationSettingsForm> getOriginalSettings() {
        return getInstance(getProject());
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
    @NotNull
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
