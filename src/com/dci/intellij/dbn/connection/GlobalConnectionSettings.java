package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.options.ProjectSettings;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.options.ProjectConfiguration;
import com.dci.intellij.dbn.connection.config.ui.GlobalConnectionSettingsForm;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;

public class GlobalConnectionSettings extends ProjectConfiguration<GlobalConnectionSettingsForm> {

    public GlobalConnectionSettings(Project project) {
        super(project);
    }

    public static GlobalConnectionSettings getInstance(Project project) {
        return ProjectSettings.getInstance(project).getConnectionSettings();
    }

    @NotNull
    @Override
    public String getId() {
        return "DBNavigator.Project.ConnectionSettings";
    }

    public String getDisplayName() {
        return "Connections";
    }

    public String getHelpTopic() {
        return "connectionBundle";
    }

    /*********************************************************
    *                   UnnamedConfigurable                 *
    *********************************************************/
    public GlobalConnectionSettingsForm createConfigurationEditor() {
        return new GlobalConnectionSettingsForm(this);
    }

    public boolean isModified() {
        ProjectConnectionBundle projectConnectionManager = ProjectConnectionBundle.getInstance(getProject());
        if (projectConnectionManager.isModified()) return true;

        Module[] modules =  ModuleManager.getInstance(getProject()).getModules();
        for (Module module : modules) {
            ModuleConnectionBundle moduleConnectionManager = ModuleConnectionBundle.getInstance(module);
            if (moduleConnectionManager.isModified()) return true;
        }
        return false;
    }

    public void apply() throws ConfigurationException {
        ProjectConnectionBundle projectConnectionManager = ProjectConnectionBundle.getInstance(getProject());
        projectConnectionManager.apply();

        Module[] modules =  ModuleManager.getInstance(getProject()).getModules();
        for (Module module : modules) {
            ModuleConnectionBundle moduleConnectionManager = ModuleConnectionBundle.getInstance(module);
            moduleConnectionManager.apply();
        }
    }

    public void reset() {
        ProjectConnectionBundle projectConnectionManager = ProjectConnectionBundle.getInstance(getProject());
        projectConnectionManager.reset();

        Module[] modules =  ModuleManager.getInstance(getProject()).getModules();
        for (Module module : modules) {
            ModuleConnectionBundle moduleConnectionManager = ModuleConnectionBundle.getInstance(module);
            moduleConnectionManager.reset();
        }
    }

    @Override
    public void disposeUIResources() {
        ProjectConnectionBundle projectConnectionManager = ProjectConnectionBundle.getInstance(getProject());
        projectConnectionManager.disposeUIResources();

        Module[] modules =  ModuleManager.getInstance(getProject()).getModules();
        for (Module module : modules) {
            ModuleConnectionBundle moduleConnectionManager = ModuleConnectionBundle.getInstance(module);
            moduleConnectionManager.disposeUIResources();
        }
        super.disposeUIResources();
    }

    public void readConfiguration(Element element) {

    }

    public void writeConfiguration(Element element) {
        
    }
}
