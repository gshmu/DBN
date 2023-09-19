package com.dci.intellij.dbn.plugin;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.component.ApplicationComponentBase;
import com.dci.intellij.dbn.common.component.PersistentState;
import com.dci.intellij.dbn.common.file.FileTypeService;
import com.dci.intellij.dbn.common.project.Projects;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.config.ConnectionBundleSettings;
import com.dci.intellij.dbn.language.common.DBLanguageFileType;
import com.dci.intellij.dbn.language.psql.PSQLFileType;
import com.dci.intellij.dbn.language.sql.SQLFileType;
import com.dci.intellij.dbn.plugin.ui.PluginConflictResolutionDialog;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.List;

import static com.dci.intellij.dbn.common.component.Components.applicationService;
import static com.dci.intellij.dbn.common.options.setting.Settings.*;
import static com.dci.intellij.dbn.diagnostics.Diagnostics.conditionallyLog;
import static com.dci.intellij.dbn.plugin.DBPluginStatus.*;
import static com.dci.intellij.dbn.plugin.PluginConflictManager.COMPONENT_NAME;
import static java.lang.System.arraycopy;

@Getter
@Setter
@State(
    name = COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class PluginConflictManager extends ApplicationComponentBase implements PersistentState {
    public static final String COMPONENT_NAME = "DBNavigator.Application.PluginConflictManager";

    private boolean fileTypesClaimed;
    private boolean conflictPrompted;
    private DBPluginStatus dbnPluginStatus = UNKNOWN;
    private DBPluginStatus sqlPluginStatus = UNKNOWN;

    public PluginConflictManager() {
        super(COMPONENT_NAME);
    }

    public static PluginConflictManager getInstance() {
        return applicationService(PluginConflictManager.class);
    }

    public void assesPluginConflict(Project project) {
        if (conflictPrompted) return;

        // make sure connections are loaded latest here
        ConnectionBundleSettings.getInstance(project);

        sqlPluginStatus = evaluateSqlPluginStatus();
        dbnPluginStatus = evaluateDbnPluginStatus();

        if (dbnPluginStatus == ACTIVE)  {
            if (sqlPluginStatus == ACTIVE) {
                showConflictResolutionDialog();
            } else {
                // missing or passive SQL plugin - favor DBN
                claimFileAssociations(false);
            }
        } else if (dbnPluginStatus == PASSIVE) {
            if (sqlPluginStatus == ACTIVE) {
                // SQL plugin user - restore default file associations
                restoreFileAssociations();
                
            } else if (sqlPluginStatus == PASSIVE) {
                showConflictResolutionDialog();
                
            } else if (sqlPluginStatus == MISSING) {
                // missing SQL plugin - favor DBN
                claimFileAssociations(false);
            }
        }
    }

    private void showConflictResolutionDialog() {
        conflictPrompted = true;
        Dispatch.run(() -> PluginConflictResolutionDialog.open());
    }

    public void applyConflictResolution(PluginConflictResolution resolution) {
        switch (resolution) {
            case DISABLE_PLUGIN: disablePlugin(); return;
            case CONTINUE_FEATURED: claimFileAssociations(true); return;
            case CONTINUE_LIMITED: restoreFileAssociations(); return;
            case DECIDE_LATER: conflictPrompted = false; return;
            default:
        }
    }

    private void claimFileAssociations(boolean force) {
        // do not claim again if already claimed once (even if no longer associated with DBN)
        if (fileTypesClaimed && !force) return;

        FileTypeService fileTypeService = FileTypeService.getInstance();

        try {
            fileTypeService.setSilentFileChangeContext(true);
            fileTypeService.claimFileAssociations(SQLFileType.INSTANCE);
            fileTypeService.claimFileAssociations(PSQLFileType.INSTANCE);
        } finally {
            fileTypeService.setSilentFileChangeContext(false);
            if (force) fileTypesClaimed = true;
        }
    }

    private boolean areFileTypesClaimed() {
        FileTypeService fileTypeService = FileTypeService.getInstance();
        FileType fileType = fileTypeService.getCurrentFileType("sql");
        return fileType instanceof DBLanguageFileType;
    }

    private void restoreFileAssociations() {
        FileTypeService fileTypeService = FileTypeService.getInstance();
        try {
            fileTypeService.setSilentFileChangeContext(true);
            fileTypeService.restoreFileAssociations();
        } finally {
            fileTypeService.setSilentFileChangeContext(false);
        }
    }


    private void disablePlugin() {
        String pluginId = DatabaseNavigator.DBN_PLUGIN_ID.getIdString();
        PluginManager.disablePlugin(pluginId);
        ApplicationManagerEx.getApplicationEx().restart(true);

        // prompt again if needed on reinstall
        conflictPrompted = false;
    }

    public DBPluginStatus evaluateDbnPluginStatus() {
        // do not change if already recorded as ACTIVE
        if (dbnPluginStatus == ACTIVE) return ACTIVE;

        for (Project project : getRelevantProjects()) {
            ConnectionManager connectionManager = ConnectionManager.getInstance(project);
            List<ConnectionHandler> connections = connectionManager.getConnectionBundle().getAllConnections();
            if (!connections.isEmpty()) return ACTIVE;
        }

        return PASSIVE;
    }

    public DBPluginStatus evaluateSqlPluginStatus() {
        // do not change if already recorded as ACTIVE
        if (sqlPluginStatus == ACTIVE) return ACTIVE;

        IdeaPluginDescriptor pluginDescriptor = PluginManager.getPlugin(DatabaseNavigator.DB_PLUGIN_ID);
        if (pluginDescriptor == null) return MISSING; // not installed

        ClassLoader pluginClassLoader = pluginDescriptor.getPluginClassLoader();
        if (pluginClassLoader == null) return MISSING;

        try {
            Class<?> psiFacadeClass = pluginClassLoader.loadClass("com.intellij.database.psi.DbPsiFacade");
            Method getInstanceMethod = psiFacadeClass.getMethod("getInstance", Project.class);

            for (Project project : getRelevantProjects()) {
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
    private Project[] getRelevantProjects() {

        Project[] openProjects = Projects.getOpenProjects();
        Project[] projects = new Project[openProjects.length + 1];
        arraycopy(openProjects, 0, projects, 0, openProjects.length);

        Project defaultProject = ProjectManager.getInstance().getDefaultProject();
        projects[projects.length - 1] = defaultProject;
        return projects;
    }

    /**************************************************************************
     *                       PersistentStateComponent                         *
     **************************************************************************/

    @Override
    public Element getComponentState() {
        Element element = new Element("state");
        setBoolean(element, "plugin-conflict-prompted", conflictPrompted);
        setBoolean(element, "file-types-claimed", fileTypesClaimed);
        setEnum(element, "sql-plugin-status", sqlPluginStatus);
        setEnum(element, "sbn-plugin-status", dbnPluginStatus);

        return element;
    }

    @Override
    public void loadComponentState(@NotNull Element element) {
        conflictPrompted = getBoolean(element, "plugin-conflict-prompted", conflictPrompted);
        fileTypesClaimed = getBoolean(element, "file-types-claimed", fileTypesClaimed);
        sqlPluginStatus = getEnum(element, "sql-plugin-status", sqlPluginStatus);
        dbnPluginStatus = getEnum(element, "sbn-plugin-status", dbnPluginStatus);
    }


}
