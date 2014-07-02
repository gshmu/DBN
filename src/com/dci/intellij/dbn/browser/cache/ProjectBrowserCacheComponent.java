package com.dci.intellij.dbn.browser.cache;

import com.dci.intellij.dbn.connection.ProjectConnectionBundle;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ProjectBrowserCacheComponent extends BrowserCacheComponent implements ProjectComponent {

    private ProjectBrowserCacheComponent(Project project) {
        super( ProjectBrowserCacheComponent.createProjectConfigFile(project),
               project.getComponent(ProjectConnectionBundle.class));
    }


    public static ProjectBrowserCacheComponent getInstance(Project project) {
        return project.getComponent(ProjectBrowserCacheComponent.class);
    }

    private static File createProjectConfigFile(Project project) {
        /*String projectFilePath = project.getProjectFilePath();
        if (projectFilePath != null) {
            int index = projectFilePath.lastIndexOf('.');
            if (index > -1) {
                String cacheFilePath = projectFilePath.substring(0, index) + FILE_EXTENSION;
                return new File(cacheFilePath);
            }
        }*/
        return null;
    }

    /***************************************
    *            ProjectComponent          *
    ****************************************/
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.BrowserCacheComponent";
    }
    public void projectOpened() {}
    public void projectClosed() {}
    public void initComponent() {}
    public void disposeComponent() {}
}
