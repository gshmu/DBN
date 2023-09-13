package com.dci.intellij.dbn.plugin;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.component.ApplicationComponentBase;
import com.dci.intellij.dbn.common.component.PersistentState;
import com.dci.intellij.dbn.common.file.FileTypeService;
import com.dci.intellij.dbn.common.project.Projects;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.language.psql.PSQLFileType;
import com.dci.intellij.dbn.language.sql.SQLFileType;
import com.dci.intellij.dbn.plugin.ui.PluginConflictResolutionDialog;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.dci.intellij.dbn.common.component.Components.applicationService;
import static com.dci.intellij.dbn.common.options.setting.Settings.getBoolean;
import static com.dci.intellij.dbn.common.options.setting.Settings.setBoolean;
import static com.dci.intellij.dbn.diagnostics.Diagnostics.conditionallyLog;
import static com.dci.intellij.dbn.plugin.DBPluginStatus.*;
import static com.dci.intellij.dbn.plugin.PluginConflictManager.COMPONENT_NAME;

@Getter
@Setter
@State(
    name = COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class PluginConflictManager extends ApplicationComponentBase implements PersistentState {
    public static final String COMPONENT_NAME = "DBNavigator.Application.PluginConflictManager";
    private boolean fileTypesClaimed;
    private boolean conflictResolved;

    public PluginConflictManager() {
        super(COMPONENT_NAME);
        Projects.projectOpened(project -> assesPluginConflict());
    }

    public static PluginConflictManager getInstance() {
        return applicationService(PluginConflictManager.class);
    }

    public void assesPluginConflict() {
        if (conflictResolved) return;
        conflictResolved = true;

        DBPluginStatus sqlPluginStatus = getSqlPluginStatus();
        DBPluginStatus dbnPluginStatus = getDbnPluginStatus();

        if (dbnPluginStatus == ACTIVE)  {
            if (sqlPluginStatus == ACTIVE) {
                // happy mixed user, no change required
                conflictResolved = true;
            } else {
                // missing or passive SQL plugin - favor DBN
                claimFileAssociations();
            }
        } else if (dbnPluginStatus == PASSIVE) {
            if (sqlPluginStatus == ACTIVE) {
                // SQL plugin user - restore default file associations
                restoreFileAssociations();
            } else if (sqlPluginStatus == PASSIVE) {
                showConflictResolutionDialog();
            } else {
                // missing or passive SQL plugin - favor DBN
                claimFileAssociations();
            }
        }
    }

    private void showConflictResolutionDialog() {
        Dispatch.run(() -> PluginConflictResolutionDialog.open());
    }

    public void applyConflictResolution(PluginConflictResolution resolution) {
        switch (resolution) {
            case DISABLE_PLUGIN: disablePlugin(); return;
            case CONTINUE_FEATURED: claimFileAssociations(); return;
            case CONTINUE_LIMITED: restoreFileAssociations(); return;
            default:
        }
    }

    private void claimFileAssociations() {
        FileTypeService fileTypeService = FileTypeService.getInstance();

        try {
            fileTypeService.setSilentFileChangeContext(true);
            fileTypeService.claimFileAssociations(SQLFileType.INSTANCE);
            fileTypeService.claimFileAssociations(PSQLFileType.INSTANCE);
        } finally {
            fileTypeService.setSilentFileChangeContext(false);
            fileTypesClaimed = true;
        }
    }

    private void restoreFileAssociations() {
        FileTypeService fileTypeService = FileTypeService.getInstance();
        try {
            fileTypeService.setSilentFileChangeContext(true);
            fileTypeService.restoreFileAssociations();
        } finally {
            fileTypeService.setSilentFileChangeContext(false);
            fileTypesClaimed = false;
        }
    }

    private void disablePlugin() {
        String pluginId = DatabaseNavigator.DBN_PLUGIN_ID.getIdString();
        PluginManager.disablePlugin(pluginId);
        ApplicationManagerEx.getApplicationEx().restart(true);
    }

    @Override
    public Element getComponentState() {
        Element element = new Element("state");
        setBoolean(element, "plugin-conflict-resolved", conflictResolved);
        setBoolean(element, "file-types-claimed", fileTypesClaimed);
        return element;
    }

    @Override
    public void loadComponentState(@NotNull Element element) {
        fileTypesClaimed = getBoolean(element, "file-types-claimed", false);
        conflictResolved = getBoolean(element, "plugin-conflict-resolved", false);
    }

    public DBPluginStatus getDbnPluginStatus() {
        List<Project> projects = getRelevantProjects();
        for (Project project : projects) {
            ConnectionManager connectionManager = ConnectionManager.getInstance(project);
            List<ConnectionHandler> connections = connectionManager.getConnectionBundle().getAllConnections();
            if (!connections.isEmpty()) return ACTIVE;
        }

        return PASSIVE;
    }

    public DBPluginStatus getSqlPluginStatus() {
        IdeaPluginDescriptor pluginDescriptor = PluginManager.getPlugin(DatabaseNavigator.DB_PLUGIN_ID);
        if (pluginDescriptor == null) return MISSING; // not installed

        ClassLoader pluginClassLoader = pluginDescriptor.getPluginClassLoader();
        if (pluginClassLoader == null) return MISSING;

        try {
            Class<?> psiFacadeClass = pluginClassLoader.loadClass("com.intellij.database.psi.DbPsiFacade");
            Method getInstanceMethod = psiFacadeClass.getMethod("getInstance", Project.class);

            List<Project> projects = getRelevantProjects();
            for (Project project : projects) {
                Object psiFacade = getInstanceMethod.invoke(psiFacadeClass, project);
                Method getDataSourcesMethod = psiFacadeClass.getMethod("getDataSources");
                List configs = (List) getDataSourcesMethod.invoke(psiFacade);
                if (!configs.isEmpty()) return ACTIVE; // connection configs found
            }
            return PASSIVE;
        } catch (Throwable e) {
            conditionallyLog(e);
        }

        return PASSIVE;
    }

    @NotNull
    private List<Project> getRelevantProjects() {
        List<Project> projects = new ArrayList<>(Arrays.asList(Projects.getOpenProjects()));
        projects.add(ProjectManager.getInstance().getDefaultProject());
        return projects;
    }
}
