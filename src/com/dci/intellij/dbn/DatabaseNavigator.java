package com.dci.intellij.dbn;

import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.ide.plugins.RepositoryHelper;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DatabaseNavigator implements ApplicationComponent, JDOMExternalizable {
    private static final String SQL_PLUGIN_ID = "com.intellij.sql";
    public static final String DBN_PLUGIN_ID = "DBN";
    /*static {
        Extensions.getRootArea().
                getExtensionPoint(CodeStyleSettingsProvider.EXTENSION_POINT_NAME).
                registerExtension(new SQLCodeStyleSettingsProvider());
    }*/

    @NotNull
    public String getComponentName() {
        return "DBNavigator";
    }

    private boolean debugModeEnabled;
    private boolean developerModeEnabled;
    private boolean slowDatabaseModeEnabled;
    private boolean showPluginConflictDialog;
    private String repositoryPluginVersion;

    public void initComponent() {
        //ModuleTypeManager.getInstance().registerModuleType(DBModuleType.MODULE_TYPE);

        //FileTypeManager.getInstance().registerFileType(SQLFileType.INSTANCE, "sql");
        //FileTypeManager.getInstance().registerFileType(PSQLFileType.INSTANCE, "psql");
        //resolvePluginConflict();

        FileTemplateManager templateManager = FileTemplateManager.getInstance();
        if (templateManager.getTemplate("SQL Script") == null) {
            templateManager.addTemplate("SQL Script", "sql");
        }

        NotificationGroup notificationGroup = new NotificationGroup("Database Navigator", NotificationDisplayType.TOOL_WINDOW, true, ExecutionManager.TOOL_WINDOW_ID);

        Timer updateChecker = new Timer("Plugin Update check task");
        updateChecker.schedule(new PluginUpdateChecker(), TimeUtil.ONE_MINUTE, TimeUtil.ONE_HOUR);
    }

    private boolean sqlPluginActive() {
        for (IdeaPluginDescriptor pluginDescriptor : PluginManager.getPlugins()) {
            if (pluginDescriptor.getPluginId().getIdString().equals(SQL_PLUGIN_ID)) {
                return !PluginManager.getDisabledPlugins().contains(SQL_PLUGIN_ID);
            }
        }
        return false;
    }

    private void resolvePluginConflict() {
        if (showPluginConflictDialog && sqlPluginActive()) {
            showPluginConflictDialog = false;
            new SimpleLaterInvocator() {
                public void execute() {
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
                }
            }.start();
        }
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

    public void disposeComponent() {
    }

    public void readExternal(Element element) throws InvalidDataException {
        debugModeEnabled = SettingsUtil.getBoolean(element, "enable-debug-mode", false);
        developerModeEnabled = SettingsUtil.getBoolean(element, "enable-developer-mode", false);
        showPluginConflictDialog = SettingsUtil.getBoolean(element, "show-plugin-conflict-dialog", true);
        SettingsUtil.isDebugEnabled = debugModeEnabled;
    }

    public void writeExternal(Element element) throws WriteExternalException {
        SettingsUtil.setBoolean(element, "enable-debug-mode", debugModeEnabled);
        SettingsUtil.setBoolean(element, "enable-developer-mode", developerModeEnabled);
        SettingsUtil.setBoolean(element, "show-plugin-conflict-dialog", showPluginConflictDialog);
    }

    public String getName() {
        return null;
    }

    public ConfigurationEditorForm createSettingsEditor() {
        return null;
    }

    public String getRepositoryPluginVersion() {
        return repositoryPluginVersion;
    }

    private class PluginUpdateChecker extends TimerTask {
        public void run() {
            try {
                List<IdeaPluginDescriptor> descriptors = RepositoryHelper.loadPluginsFromRepository(null);
                for (IdeaPluginDescriptor descriptor : descriptors) {
                    if (descriptor.getPluginId().toString().equals(DatabaseNavigator.DBN_PLUGIN_ID)) {
                        repositoryPluginVersion = descriptor.getVersion();
                        break;
                    }
                }
            } catch (Exception e) {
            }
        }
    }
}

