package com.dci.intellij.dbn;

import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.init.DatabaseNavigatorInitializer;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.extensions.PluginId;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    private boolean debugModeEnabled;
    private boolean developerModeEnabled;
    private boolean slowDatabaseModeEnabled;
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

    public boolean isDebugModeEnabled() {
        return debugModeEnabled;
    }

    public void setDebugModeEnabled(boolean debugModeEnabled) {
        this.debugModeEnabled = debugModeEnabled;
        SettingsUtil.isDebugEnabled = debugModeEnabled;
    }

    public boolean isDeveloperModeEnabled() {
        return developerModeEnabled;
    }

    public void setDeveloperModeEnabled(boolean developerModeEnabled) {
        this.developerModeEnabled = developerModeEnabled;
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
        SettingsUtil.setBoolean(element, "enable-debug-mode", debugModeEnabled);
        SettingsUtil.setBoolean(element, "enable-developer-mode", developerModeEnabled);
        SettingsUtil.setBoolean(element, "show-plugin-conflict-dialog", showPluginConflictDialog);
        return element;
    }

    @Override
    public void loadState(Element element) {
        debugModeEnabled = SettingsUtil.getBoolean(element, "enable-debug-mode", false);
        developerModeEnabled = SettingsUtil.getBoolean(element, "enable-developer-mode", false);
        showPluginConflictDialog = SettingsUtil.getBoolean(element, "show-plugin-conflict-dialog", true);
        SettingsUtil.isDebugEnabled = debugModeEnabled;
    }
}

