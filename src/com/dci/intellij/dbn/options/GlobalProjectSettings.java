package com.dci.intellij.dbn.options;

import com.dci.intellij.dbn.browser.options.DatabaseBrowserSettings;
import com.dci.intellij.dbn.code.common.completion.options.CodeCompletionSettings;
import com.dci.intellij.dbn.code.common.style.options.ProjectCodeStyleSettings;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.options.CompositeProjectConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.connection.GlobalConnectionSettings;
import com.dci.intellij.dbn.ddl.options.DDLFileSettings;
import com.dci.intellij.dbn.editor.data.options.DataEditorSettings;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.navigation.options.NavigationSettings;
import com.dci.intellij.dbn.options.general.GeneralProjectSettings;
import com.dci.intellij.dbn.options.ui.GlobalProjectSettingsEditorForm;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import javax.swing.JComponent;

public class GlobalProjectSettings
        extends CompositeProjectConfiguration<GlobalProjectSettingsEditorForm>
        implements ProjectComponent, JDOMExternalizable, SearchableConfigurable.Parent {

    private GeneralProjectSettings generalSettings;
    private DatabaseBrowserSettings browserSettings;
    private NavigationSettings navigationSettings;
    private DataEditorSettings dataEditorSettings;
    private CodeCompletionSettings codeCompletionSettings;
    private ProjectCodeStyleSettings codeStyleSettings;
    private ExecutionEngineSettings executionEngineSettings;
    private DDLFileSettings ddlFileSettings;
    private GlobalConnectionSettings connectionSettings;



    public static GlobalProjectSettings getInstance(Project project) {
        return project.getComponent(GlobalProjectSettings.class);
    }

    private GlobalProjectSettings(Project project) {
        super(project);
        generalSettings = new GeneralProjectSettings(project);
        browserSettings = new DatabaseBrowserSettings(project);
        navigationSettings = new NavigationSettings(project);
        codeStyleSettings = new ProjectCodeStyleSettings(project);
        dataEditorSettings = new DataEditorSettings(project);
        codeCompletionSettings = new CodeCompletionSettings(project);
        executionEngineSettings = new ExecutionEngineSettings(project);
        ddlFileSettings = new DDLFileSettings(project);
        connectionSettings = new GlobalConnectionSettings(project);
    }

    public String getHelpTopic() {
        GlobalProjectSettingsEditorForm settingsEditor = getSettingsEditor();
        if (settingsEditor == null) {
            return super.getHelpTopic();
        } else {
            Configuration selectedConfiguration = settingsEditor.getActiveSettings();
            return selectedConfiguration.getHelpTopic();
        }
    }

    @Override
    public JComponent createComponent() {
        return null;//super.createComponent();
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

    public GlobalConnectionSettings getConnectionSettings() {
        return connectionSettings;
    }

    public DataEditorSettings getDataEditorSettings() {
        return dataEditorSettings;
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

    @Override
    protected void onApply() {
        ProjectSettingsChangeListener listener = EventManager.notify(getProject(), ProjectSettingsChangeListener.TOPIC);
        listener.projectSettingsChanged(getProject());
    }

    /*********************************************************
     *                    Configuration                      *
     *********************************************************/
    public GlobalProjectSettingsEditorForm createConfigurationEditor() {
        return new GlobalProjectSettingsEditorForm(this);
    }

    protected Configuration[] createConfigurations() {
        return new Configuration[] {
                generalSettings,
                browserSettings,
                navigationSettings,
                codeStyleSettings,
                dataEditorSettings,
                codeCompletionSettings,
                executionEngineSettings,
                ddlFileSettings,
                connectionSettings};
    }

/*    public void readConfiguration(Element element) throws InvalidDataException {
        readConfiguration(element, browserSettings);
        readConfiguration(element, navigationSettings);
        readConfiguration(element, dataEditorSettings);
        readConfiguration(element, codeCompletionSettings);
        readConfiguration(element, executionEngineSettings);
        readConfiguration(element, ddlFileSettings);
        readConfiguration(element, generalSettings);
    }

    public void writeConfiguration(Element element) throws WriteExternalException {
        writeConfiguration(element, browserSettings);
        writeConfiguration(element, navigationSettings);
        writeConfiguration(element, dataEditorSettings);
        writeConfiguration(element, codeCompletionSettings);
        writeConfiguration(element, executionEngineSettings);
        writeConfiguration(element, ddlFileSettings);
        writeConfiguration(element, generalSettings);
    }*/

    /*********************************************************
     *                  JDOMExternalizable                   *
     *********************************************************/
    public void readExternal(Element element) throws InvalidDataException {
        readConfiguration(element);
    }

    public void writeExternal(Element element) throws WriteExternalException {
        writeConfiguration(element);
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
    *             ProjectComponent          *
    *****************************************/
    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.Settings";
    }

    public void projectOpened() {}
    public void projectClosed() {}
    public void initComponent() {}
    public void disposeComponent() {}
}
