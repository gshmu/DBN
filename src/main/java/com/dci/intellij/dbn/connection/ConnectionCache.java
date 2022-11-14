package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.project.Projects;
import com.dci.intellij.dbn.language.common.WeakRef;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dci.intellij.dbn.common.dispose.Failsafe.invalid;

final class ConnectionCache {
    private static Wrapper[] data = new Wrapper[50];

    private ConnectionCache() {}

    @Nullable
    public static ConnectionHandler resolve(@Nullable ConnectionId connectionId) {
        if (connectionId == null) return null;

        int index = connectionId.index();

        if (data.length <= index || data[index] == null) {
            synchronized (ConnectionCache.class) {
                if (data.length <= index || data[index] == null) {

                    // ensure capacity
                    if (data.length <= index) {
                        Wrapper[] oldCache = data;
                        data = new Wrapper[data.length * 2];
                        System.arraycopy(oldCache, 0, data, 0, oldCache.length);
                    }

                    // ensure entry
                    for (Project project : Projects.getOpenProjects()) {
                        ConnectionManager connectionManager = ConnectionManager.getInstance(project);
                        ConnectionHandler connection = connectionManager.getConnection(connectionId);

                        // cache as null if disposed
                        if (invalid(connection)) connection = null;

                        data[index] = new Wrapper(connection);
                    }
                }
            }
        }

        Wrapper wrapper = data[index];
        return wrapper == null ? null : wrapper.get();
    }

    public static void releaseCache(@NotNull Project project) {
        if (project.isDefault()) return;

        for (int i = 0; i < data.length; i++) {
            Wrapper wrapper = data[i];
            if (wrapper == null) continue;

            ConnectionHandler connectionHandler = wrapper.get();
            if (connectionHandler == null || connectionHandler.isDisposed() || connectionHandler.getProject() == project) {
                data[i] = null;
            }
        }
    }

    private static void refreshConnections(@NotNull Project project) {

    }


    private static class Wrapper extends WeakRef<ConnectionHandler> {
        protected Wrapper(ConnectionHandler referent) {
            super(referent);
        }

        @Override
        public String toString() {
            ConnectionHandler connection = get();
            return connection == null ? "null" : connection.getName();
        }
    }
}
