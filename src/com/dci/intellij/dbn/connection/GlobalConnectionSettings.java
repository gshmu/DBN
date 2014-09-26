package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.options.ProjectConfiguration;
import com.dci.intellij.dbn.connection.config.ui.GlobalConnectionSettingsForm;
import com.dci.intellij.dbn.options.ProjectSettings;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

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

    @Override
    public String getConfigElementName() {
        return "connections";
    }

    /*********************************************************
    *                   UnnamedConfigurable                 *
    *********************************************************/
    public GlobalConnectionSettingsForm createConfigurationEditor() {
        return new GlobalConnectionSettingsForm(this);
    }

    public boolean isModified() {
        ProjectConnectionBundle projectConnectionManager = ProjectConnectionBundle.getInstance(getProject());
        return projectConnectionManager.isModified();
    }

    public void apply() throws ConfigurationException {
        ProjectConnectionBundle projectConnectionManager = ProjectConnectionBundle.getInstance(getProject());
        projectConnectionManager.apply();
    }

    public void reset() {
        ProjectConnectionBundle projectConnectionManager = ProjectConnectionBundle.getInstance(getProject());
        projectConnectionManager.reset();
    }

    @Override
    public void disposeUIResources() {
        ProjectConnectionBundle projectConnectionManager = ProjectConnectionBundle.getInstance(getProject());
        projectConnectionManager.disposeUIResources();
        super.disposeUIResources();
    }

    public void readConfiguration(Element element) {
    }

    public void writeConfiguration(Element element) {
/*
        ProjectConnectionBundle connectionBundle = ProjectConnectionBundle.getInstance(getProject());
        connectionBundle.writeConfiguration(element);
*/
    }
}
