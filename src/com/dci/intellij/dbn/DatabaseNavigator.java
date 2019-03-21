package com.dci.intellij.dbn;

import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.init.DatabaseNavigatorInitializer;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.ui.Messages;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.util.List;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.getBoolean;
import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.setBoolean;

@State(
    name = DatabaseNavigator.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class DatabaseNavigator implements ApplicationComponent, PersistentStateComponent<Element> {
    public static final String COMPONENT_NAME = "DBNavigator.Application.Settings";
    public static final String STORAGE_FILE = "dbnavigator.xml";

    private static final String SQL_PLUGIN_ID = "com.intellij.sql";
    public static final String DBN_PLUGIN_ID = "DBN";
    /*static {
        Extensions.getRootArea().
                getExtensionPoint(CodeStyleSettingsProvider.EXTENSION_POINT_NAME).
                registerExtension(new SQLCodeStyleSettingsProvider());
    }*/

    @Override
    @NotNull
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    public static boolean debugModeEnabled = false;
    public static boolean developerModeEnabled = false;
    public static boolean slowDatabaseModeEnabled = false;

    private boolean showPluginConflictDialog;
    private String repositoryPluginVersion;

    @Override
    public void initComponent() {
        //ModuleTypeManager.getInstance().registerModuleType(DBModuleType.MODULE_TYPE);

        //FileTypeManager.getInstance().registerFileType(SQLFileType.INSTANCE, "sql");
        //FileTypeManager.getInstance().registerFileType(PSQLFileType.INSTANCE, "psql");
        //resolvePluginConflict();
        DatabaseNavigatorInitializer.componentsInitialized();
    }

    private static boolean sqlPluginActive() {
        for (IdeaPluginDescriptor pluginDescriptor : PluginManager.getPlugins()) {
            if (pluginDescriptor.getPluginId().getIdString().equals(SQL_PLUGIN_ID)) {
                return !PluginManager.getDisabledPlugins().contains(SQL_PLUGIN_ID);
            }
        }
        return false;
    }

    public static DatabaseNavigator getInstance() {
        return ApplicationManager.getApplication().getComponent(DatabaseNavigator.class);
    }

    public boolean isSlowDatabaseModeEnabled() {
        return developerModeEnabled && slowDatabaseModeEnabled;
    }

    public void setSlowDatabaseModeEnabled(boolean slowDatabaseModeEnabled) {
        this.slowDatabaseModeEnabled = slowDatabaseModeEnabled;
    }

    @Override
    public void disposeComponent() {
    }

    public String getName() {
        return null;
    }

    public String getPluginVersion() {
        IdeaPluginDescriptor pluginDescriptor = PluginManager.getPlugin(PluginId.getId(DatabaseNavigator.DBN_PLUGIN_ID));
        return pluginDescriptor != null ? pluginDescriptor.getVersion() : null;
    }

    public String getRepositoryPluginVersion() {
        return repositoryPluginVersion;
    }

    public void setRepositoryPluginVersion(String repositoryPluginVersion) {
        this.repositoryPluginVersion = repositoryPluginVersion;
    }

    /*********************************************
     *            PersistentStateComponent       *
     *********************************************/
    @Nullable
    @Override
    public Element getState() {
        Element element = new Element("state");
        setBoolean(element, "enable-debug-mode", debugModeEnabled);
        setBoolean(element, "enable-developer-mode", developerModeEnabled);
        setBoolean(element, "show-plugin-conflict-dialog", showPluginConflictDialog);
        return element;
    }

    @Override
    public void loadState(@NotNull Element element) {
        debugModeEnabled = getBoolean(element, "enable-debug-mode", false);
        developerModeEnabled = getBoolean(element, "enable-developer-mode", false);
        showPluginConflictDialog = getBoolean(element, "show-plugin-conflict-dialog", true);
    }

    private void resolvePluginConflict() {
        if (showPluginConflictDialog && sqlPluginActive()) {
            showPluginConflictDialog = false;
            Dispatch.invokeNonModal(() -> {
                List<String> disabledList = PluginManager.getDisabledPlugins();
                String message =
                        "Database Navigator plugin (DBN) is not compatible with the IntelliJ IDEA built-in SQL functionality. " +
                                "They both provide similar features but present quite different use-cases.\n" +
                                "In order to have access to the full functionality of Database Navigator plugin and avoid usage confusions, we strongly advise you disable the IDEA SQL plugin. \n" +
                                "You can enable it at any time from your plugin manager.\n\n" +
                                "For more details about the plugin conflict, please visit the Database Navigator support page.\n" +
                                "Note: IDEA will need to restart if you choose to make changes in the plugin configuration.\n\n" +
                                "Please pick an option to proceed.";
                String title = Constants.DBN_TITLE_PREFIX + "Plugin Conflict";
                String[] options = {
                        "Disable IDEA SQL plugin (restart)",
                        "Disable DBN plugin (restart)",
                        "Ignore and continue (not recommended)"};
                Icon icon = Messages.getWarningIcon();
                int exitCode = Messages.showDialog(message, title, options, 0, icon);
                if (exitCode == 0 || exitCode == 1) {
                    try {
                        disabledList.add(exitCode == 1 ? DBN_PLUGIN_ID : SQL_PLUGIN_ID);
                        PluginManager.saveDisabledPlugins(disabledList, false);
                        ApplicationManager.getApplication().restart();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}

