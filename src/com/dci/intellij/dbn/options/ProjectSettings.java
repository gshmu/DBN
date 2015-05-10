package com.dci.intellij.dbn.options;

import com.dci.intellij.dbn.browser.options.DatabaseBrowserSettings;
import com.dci.intellij.dbn.code.common.completion.options.CodeCompletionSettings;
import com.dci.intellij.dbn.code.common.style.options.ProjectCodeStyleSettings;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.action.DBNDataKeys;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.options.CompositeProjectConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.SettingsChangeNotifier;
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
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.util.List;

@State(
        name = "DBNavigator.Project.Settings",
        storages = {
                @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/dbnavigator.xml", scheme = StorageScheme.DIRECTORY_BASED),
                @Storage(file = StoragePathMacros.PROJECT_FILE)}
)
public class ProjectSettings
        extends CompositeProjectConfiguration<ProjectSettingsEditorForm>
        implements SearchableConfigurable.Parent, ProjectComponent, PersistentStateComponent<Element>  {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    private GeneralProjectSettings generalSettings;
    private DatabaseBrowserSettings browserSettings;
    private NavigationSettings navigationSettings;
    private DataGridSettings dataGridSettings;
    private DataEditorSettings dataEditorSettings;
    private CodeEditorSettings codeEditorSettings;
    private CodeCompletionSettings codeCompletionSettings;
    private ProjectCodeStyleSettings codeStyleSettings;
    private ExecutionEngineSettings executionEngineSettings;
    private OperationSettings operationSettings;
    private DDLFileSettings ddlFileSettings;
    private ConnectionBundleSettings connectionSettings;

    public static ProjectSettings getInstance(@NotNull Project project) {
        return FailsafeUtil.getComponent(project, ProjectSettings.class);
    }

    public ProjectSettings(Project project) {
        super(project);
        generalSettings = new GeneralProjectSettings(project);
        browserSettings = new DatabaseBrowserSettings(project);
        navigationSettings = new NavigationSettings(project);
        codeStyleSettings = new ProjectCodeStyleSettings(project);
        codeEditorSettings = new CodeEditorSettings(project);
        dataGridSettings = new DataGridSettings(project);
        dataEditorSettings = new DataEditorSettings(project);
        codeCompletionSettings = new CodeCompletionSettings(project);
        executionEngineSettings = new ExecutionEngineSettings(project);
        operationSettings = new OperationSettings(project);
        ddlFileSettings = new DDLFileSettings(project);
        connectionSettings = new ConnectionBundleSettings(project);
    }

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
        notifyChanges();
    }

    protected void notifyChanges() {
        List<SettingsChangeNotifier> changeNotifiers = SETTINGS_CHANGE_NOTIFIERS.get();
        if (changeNotifiers != null) {
            try {
                for (SettingsChangeNotifier changeNotifier : changeNotifiers) {
                    try {
                        changeNotifier.notifyChanges();
                    } catch (Exception e){
                        if (!(e instanceof ProcessCanceledException)) {
                            LOGGER.error("Error notifying configuration changes", e);
                        }
                    }
                }
            } finally {
                SETTINGS_CHANGE_NOTIFIERS.set(null);
            }
        }
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

    @Nls
    public String getDisplayName() {
        return "Database Navigator";
    }

    @Nullable
    public Icon getIcon() {
        return Icons.DATABASE_NAVIGATOR;
    }

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
    @NotNull
    public ProjectSettingsEditorForm createConfigurationEditor() {
        return new ProjectSettingsEditorForm(this);
    }

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
    public boolean hasOwnContent() {
        return false;
    }

    public boolean isVisible() {
        return true;
    }

    @NotNull
    public String getId() {
        return "DBNavigator.Project.Settings";
    }

    public Runnable enableSearch(String option) {
        return null;
    }

    /****************************************
     *       PersistentStateComponent       *
     *****************************************/
    @Nullable
    @Override
    public Element getState() {
        Element element = new Element("state");
        writeConfiguration(element);
        return element;
    }

    @Override
    public void loadState(Element element) {
        readConfiguration(element);
        getProject().putUserData(DBNDataKeys.PROJECT_SETTINGS_LOADED_KEY, true);
    }

    /****************************************
     *           ProjectComponent           *
     *****************************************/
    @Override
    public void projectOpened() {

    }

    @Override
    public void projectClosed() {

    }

    @Override
    public void initComponent() {

    }

    @Override
    public void disposeComponent() {

    }

    @NotNull
    @Override
    public String getComponentName() {
        return "DBNavigator.Project.Settings";
    }
}
