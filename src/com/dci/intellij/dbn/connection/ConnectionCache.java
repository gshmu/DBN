package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.component.ApplicationComponent;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.project.Projects;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionCache implements ApplicationComponent {
    private static final Map<ConnectionId, ConnectionHandler> CACHE = new ConcurrentHashMap<>();

    public ConnectionCache() {
        Projects.projectOpened(project -> initializeCache(project));
        Projects.projectClosed(project -> releaseCache(project));
    }

    @Nullable
    public static ConnectionHandler findConnectionHandler(ConnectionId connectionId) {
        if (connectionId != null) {
            ConnectionHandler connectionHandler = CACHE.get(connectionId);
            if (connectionHandler == null) {
                synchronized (ConnectionCache.class) {
                    connectionHandler = CACHE.get(connectionId);
                    if (connectionHandler == null) {
                        for (Project project : Projects.getOpenProjects()) {
                            ConnectionManager connectionManager = ConnectionManager.getInstance(project);
                            connectionHandler = connectionManager.getConnectionHandler(connectionId);
                            if (Failsafe.check(connectionHandler)) {
                                CACHE.put(connectionId, connectionHandler);
                                return connectionHandler;
                            }
                        }
                    }
                }
            }
            return Failsafe.check(connectionHandler) ? connectionHandler : null;
        } else{
            return null;
        }
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "DBNavigator.ConnectionCache";
    }


    private static void initializeCache(@NotNull Project project) {
        if (!project.isDefault()) {
            ConnectionManager connectionManager = ConnectionManager.getInstance(project);
            List<ConnectionHandler> connectionHandlers = connectionManager.getConnectionHandlers();
            for (ConnectionHandler connectionHandler : connectionHandlers) {
                CACHE.put(connectionHandler.getConnectionId(), connectionHandler);
            }
        }
    }

    private static void releaseCache(@NotNull Project project) {
        if (!project.isDefault()) {
            Iterator<ConnectionId> connectionIds = CACHE.keySet().iterator();
            while (connectionIds.hasNext()) {
                ConnectionId connectionId = connectionIds.next();
                ConnectionHandler connectionHandler = CACHE.get(connectionId);
                if (connectionHandler.isDisposed() || connectionHandler.getProject() == project) {
                    connectionIds.remove();
                }
            }
        }
    }
}
