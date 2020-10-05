package com.dci.intellij.dbn.init;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.proxy.CommonProxy;

import java.net.ProxySelector;
import java.util.Timer;
import java.util.TimerTask;

public class DatabaseNavigatorInitializer /*implements com.intellij.ide.ApplicationInitializedListener*/ {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    //@Override
    public static void componentsInitialized() {
        new NotificationGroup("Database Navigator", NotificationDisplayType.TOOL_WINDOW, true, ExecutionManager.TOOL_WINDOW_ID);

        Timer updateChecker = new Timer("DBN - Plugin Update (check timer)");
        updateChecker.schedule(new PluginUpdateChecker(), TimeUtil.Millis.ONE_SECOND, TimeUtil.Millis.ONE_HOUR);
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

                IdeaPluginDescriptor pluginDescriptor = DatabaseNavigator.getPluginDescriptor();
                if (pluginDescriptor != null) {
                    DatabaseNavigator databaseNavigator = DatabaseNavigator.getInstance();
                    databaseNavigator.setRepositoryPluginVersion(pluginDescriptor.getVersion());
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to load DBN plugin descriptor", e);
            } finally {
                if (changeProxy) {
                    ProxySelector.setDefault(initialProxySelector);
                }
            }
        }
    }
}
