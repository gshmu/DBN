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
import org.jdom.Element;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ProjectSettings
        extends CompositeProjectConfiguration<ProjectConfiguration, ProjectSettingsEditorForm>
        implements SearchableConfigurable.Parent, Cloneable<ProjectSettings> {

    private static final Logger LOGGER = LoggerFactory.createLogger();

    private GeneralProjectSettings generalSettings           = new GeneralProjectSettings(this);
    private DatabaseBrowserSettings browserSettings          = new DatabaseBrowserSettings(this);
    private NavigationSettings navigationSettings            = new NavigationSettings(this);
    private DataGridSettings dataGridSettings                = new DataGridSettings(this);
    private DataEditorSettings dataEditorSettings            = new DataEditorSettings(this);
    private CodeEditorSettings codeEditorSettings            = new CodeEditorSettings(this);
    private CodeCompletionSettings codeCompletionSettings    = new CodeCompletionSettings(this);
    private ProjectCodeStyleSettings codeStyleSettings       = new ProjectCodeStyleSettings(this);
    private ExecutionEngineSettings executionEngineSettings  = new ExecutionEngineSettings(this);
    private OperationSettings operationSettings              = new OperationSettings(this);
    private DDLFileSettings ddlFileSettings                  = new DDLFileSettings(this);
    private ConnectionBundleSettings connectionSettings      = new ConnectionBundleSettings(this);

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



    /*********************************************************
    *                         Custom                        *
    *********************************************************/
    public GeneralProjectSettings getGeneralSettings() {
        return generalSettings;
    }

    public DatabaseBrowserSettings getBrowserSettings() {
        return browserSettings;
    }

    public NavigationSettings getNavigationSettings() {
        return navigationSettings;
    }

    public ConnectionBundleSettings getConnectionSettings() {
        return connectionSettings;
    }

    public DataGridSettings getDataGridSettings() {
        return dataGridSettings;
    }

    public DataEditorSettings getDataEditorSettings() {
        return dataEditorSettings;
    }

    public CodeEditorSettings getCodeEditorSettings() {
        return codeEditorSettings;
    }

    public CodeCompletionSettings getCodeCompletionSettings() {
        return codeCompletionSettings;
    }

    public ProjectCodeStyleSettings getCodeStyleSettings() {
        return codeStyleSettings;
    }

    public ExecutionEngineSettings getExecutionEngineSettings() {
        return executionEngineSettings;
    }

    public OperationSettings getOperationSettings() {
        return operationSettings;
    }

    public DDLFileSettings getDdlFileSettings() {
        return ddlFileSettings;
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
