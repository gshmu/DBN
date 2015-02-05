package com.dci.intellij.dbn.options;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.action.DBNDataKeys;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.thread.SimpleTask;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.impl.ProjectLifecycleListener;

@State(
        name = "DBNavigator.DefaultProject.Settings",
        storages = {@Storage(file = StoragePathMacros.APP_CONFIG + "/dbnavigator.xml")}
)
public class DefaultProjectSettingsManager implements ApplicationComponent, PersistentStateComponent<Element> {
    private ProjectSettings defaultProjectSettings;

    private DefaultProjectSettingsManager() {
        defaultProjectSettings = new ProjectSettings(ProjectManager.getInstance().getDefaultProject());
    }

    public static DefaultProjectSettingsManager getInstance() {
        return ApplicationManager.getApplication().getComponent(DefaultProjectSettingsManager.class);
    }

    public ProjectSettings getDefaultProjectSettings() {
        return defaultProjectSettings;
    }

        @Override
    public void initComponent() {
        EventManager.subscribe(ProjectLifecycleListener.TOPIC, projectLifecycleListener);
    }

    @Override
    public void disposeComponent() {
        EventManager.unsubscribe(projectLifecycleListener);
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "DBNavigator.Application.TemplateProjectSettings";
    }

    /****************************************
     *       PersistentStateComponent       *
     *****************************************/
    @Nullable
    @Override
    public Element getState() {
        Element element = new Element("state");
        defaultProjectSettings.writeConfiguration(element);
        return element;
    }

    @Override
    public void loadState(Element element) {
        defaultProjectSettings.readConfiguration(element);
    }

    /*********************************************************
     *              ProjectLifecycleListener                 *
     *********************************************************/
    private ProjectLifecycleListener projectLifecycleListener = new ProjectLifecycleListener.Adapter() {

        @Override
        public void projectComponentsInitialized(final Project project) {
            // not working. this event is notified in the project message bus
            //loadDefaultProjectSettings(project);
        }
    };

    public void saveDefaultProjectSettings(final Project project) {
        MessageUtil.showQuestionDialog(
                project, "Default Project Settings",
                "This will overwrite your default settings with the current project settings, including database connections configuration. \nAre you sure you want to continue?",
                new String[]{"Yes", "No"}, 0,
                new SimpleTask() {
                    @Override
                    public void execute() {
                        if (getOption() == 0) {
                            ProjectSettings projectSettings = ProjectSettingsManager.getSettings(project);
                            Element element = new Element("state");
                            projectSettings.writeConfiguration(element);
                            defaultProjectSettings.readConfiguration(element);
                        }

                    }
                });
    }

    public void loadDefaultProjectSettings(final Project project, boolean isNew) {
        Boolean settingsLoaded = project.getUserData(DBNDataKeys.PROJECT_SETTINGS_LOADED_KEY);
        if (settingsLoaded == null || !settingsLoaded || !isNew) {
            String message = isNew ?
                    "Do you want to import the default project settings into project \"" + project.getName() + "\"?":
                    "Your current settings will be overwritten with the default project settings, including database connections configuration. \nAre you sure you want to import the default project settings into project \"" + project.getName() + "\"?";
            MessageUtil.showQuestionDialog(
                    project, "Default Project Settings",
                    message,
                    new String[]{"Yes", "No"}, 0,
                    new SimpleTask() {
                        @Override
                        public void execute() {
                            if (getOption() == 0) {
                                ProjectSettings projectSettings = ProjectSettingsManager.getSettings(project);
                                Element element = new Element("state");
                                defaultProjectSettings.writeConfiguration(element);
                                projectSettings.readConfiguration(element);
                            }

                        }
                    });
        }
    }

}
