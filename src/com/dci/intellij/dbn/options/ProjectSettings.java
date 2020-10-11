package com.dci.intellij.dbn.options;

import com.dci.intellij.dbn.browser.options.DatabaseBrowserSettings;
import com.dci.intellij.dbn.code.common.completion.options.CodeCompletionSettings;
import com.dci.intellij.dbn.code.common.style.options.ProjectCodeStyleSettings;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.options.CompositeProjectConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.ProjectConfiguration;
import com.dci.intellij.dbn.common.util.Cloneable;
import com.dci.intellij.dbn.connection.config.ConnectionBundleSettings;
import com.dci.intellij.dbn.connection.operation.options.OperationSettings;
import com.dci.intellij.dbn.data.grid.options.DataGridSettings;
import com.dci.intellij.dbn.ddl.options.DDLFileSettings;
import com.dci.intellij.dbn.editor.code.options.CodeEditorSettings;
import com.dci.intellij.dbn.editor.data.options.DataEditorSettings;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.navigation.options.NavigationSettings;
import com.dci.intellij.dbn.options.general.GeneralProjectSettings;
import com.dci.intellij.dbn.options.ui.ProjectSettingsEditorForm;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import lombok.Getter;
import org.jdom.Element;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ProjectSettings
        extends CompositeProjectConfiguration<ProjectConfiguration, ProjectSettingsEditorForm>
        implements SearchableConfigurable.Parent, Cloneable<ProjectSettings> {

    private static final Logger LOGGER = LoggerFactory.createLogger();

    private final @Getter GeneralProjectSettings generalSettings           = new GeneralProjectSettings(this);
    private final @Getter DatabaseBrowserSettings browserSettings          = new DatabaseBrowserSettings(this);
    private final @Getter NavigationSettings navigationSettings            = new NavigationSettings(this);
    private final @Getter DataGridSettings dataGridSettings                = new DataGridSettings(this);
    private final @Getter DataEditorSettings dataEditorSettings            = new DataEditorSettings(this);
    private final @Getter CodeEditorSettings codeEditorSettings            = new CodeEditorSettings(this);
    private final @Getter CodeCompletionSettings codeCompletionSettings    = new CodeCompletionSettings(this);
    private final @Getter ProjectCodeStyleSettings codeStyleSettings       = new ProjectCodeStyleSettings(this);
    private final @Getter ExecutionEngineSettings executionEngineSettings  = new ExecutionEngineSettings(this);
    private final @Getter OperationSettings operationSettings              = new OperationSettings(this);
    private final @Getter DDLFileSettings ddlFileSettings                  = new DDLFileSettings(this);
    private final @Getter ConnectionBundleSettings connectionSettings      = new ConnectionBundleSettings(this);

    public ProjectSettings(Project project) {
        super(project);
    }

    @Override
    public String getHelpTopic() {
        ProjectSettingsEditorForm settingsEditor = getSettingsEditor();
        if (settingsEditor == null) {
            return super.getHelpTopic();
        } else {
            Configuration selectedConfiguration = settingsEditor.getActiveSettings();
            return selectedConfiguration.getHelpTopic();
        }
    }

    @Override
    public void apply() throws ConfigurationException {
        super.apply();
    }

    @NotNull
    @Override
    public JComponent createComponent() {
        return new JPanel();
    }

    public JComponent createCustomComponent() {
        return super.createComponent();
    }


    @Override
    @Nls
    public String getDisplayName() {
        return "Database Navigator";
    }

    @Override
    @Nullable
    public Icon getIcon() {
        return Icons.DATABASE_NAVIGATOR;
    }

    @NotNull
    @Override
    public Configurable[] getConfigurables() {
        return getConfigurations();
    }

    @Nullable
    public Configuration getConfiguration(ConfigId settingsId) {
        for (Configurable configurable : getConfigurables()) {
            TopLevelConfig topLevelConfig = (TopLevelConfig) configurable;
            if (topLevelConfig.getConfigId() == settingsId) {
                return (Configuration) configurable;
            }
        }
        return null;
    }


    /*********************************************************
     *                    Configuration                      *
     *********************************************************/
    @Override
    @NotNull
    public ProjectSettingsEditorForm createConfigurationEditor() {
        return new ProjectSettingsEditorForm(this);
    }

    @Override
    protected Configuration[] createConfigurations() {
        return new Configuration[] {
                connectionSettings,
                browserSettings,
                navigationSettings,
                //codeStyleSettings,
                dataGridSettings,
                dataEditorSettings,
                codeEditorSettings,
                codeCompletionSettings,
                executionEngineSettings,
                operationSettings,
                ddlFileSettings,
                generalSettings};
    }

    /*********************************************************
    *              SearchableConfigurable.Parent             *
    *********************************************************/
    @Override
    public boolean hasOwnContent() {
        return false;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    @NotNull
    public String getId() {
        return "DBNavigator.Project.Settings";
    }

    @Override
    public Runnable enableSearch(String option) {
        return null;
    }

    @Override
    public ProjectSettings clone() {
        try {
            IS_TRANSITORY.set(true);
            Element element = new Element("project-settings");
            writeConfiguration(element);
            ProjectSettings projectSettings = new ProjectSettings(getProject());
            projectSettings.readConfiguration(element);
            return projectSettings;
        } finally {
            IS_TRANSITORY.set(false);
        }
    }
}
