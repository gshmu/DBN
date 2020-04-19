package com.dci.intellij.dbn.init;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.RepositoryHelper;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.util.proxy.CommonProxy;

import java.net.ProxySelector;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DatabaseNavigatorInitializer /*implements com.intellij.ide.ApplicationInitializedListener*/ {
    //@Override
    public static void componentsInitialized() {
        new NotificationGroup("Database Navigator", NotificationDisplayType.TOOL_WINDOW, true, ExecutionManager.TOOL_WINDOW_ID);

        Timer updateChecker = new Timer("DBN - Plugin Update (check timer)");
        updateChecker.schedule(new PluginUpdateChecker(), TimeUtil.ONE_SECOND, TimeUtil.ONE_HOUR);
    }

    private static class PluginUpdateChecker extends TimerTask {
        @Override
        public void run() {
            ProxySelector initialProxySelector = ProxySelector.getDefault();
            CommonProxy defaultProxy = CommonProxy.getInstance();
            boolean changeProxy = defaultProxy != initialProxySelector;
            try {
                if (changeProxy) {
                    ProxySelector.setDefault(defaultProxy);
                }

                List<IdeaPluginDescriptor> descriptors = RepositoryHelper.loadCachedPlugins();
                if (descriptors != null) {
                    for (IdeaPluginDescriptor descriptor : descriptors) {
                        if (descriptor.getPluginId().equals(DatabaseNavigator.DBN_PLUGIN_ID)) {
                            DatabaseNavigator databaseNavigator = DatabaseNavigator.getInstance();
                            databaseNavigator.setRepositoryPluginVersion(descriptor.getVersion());
                            break;
                        }
                    }
                }
            } catch (Exception e) {
            } finally {
                if (changeProxy) {
                    ProxySelector.setDefault(initialProxySelector);
                }
            }
        }
    }
}
