package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.project.Projects;
import com.dci.intellij.dbn.common.ref.WeakRef;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.dci.intellij.dbn.common.dispose.Checks.isNotValid;
import static com.dci.intellij.dbn.common.dispose.Checks.isValid;

final class ConnectionCache {
    private static Wrapper[] data = new Wrapper[50];
    private static final Lock lock = new ReentrantLock();

    private ConnectionCache() {}

    @Nullable
    public static ConnectionHandler resolve(@Nullable ConnectionId connectionId) {
        if (connectionId == null) return null;
        ensure(connectionId);

        Wrapper wrapper = data[connectionId.index()];
        return wrapper == null ? null : wrapper.get();
    }

    private static void ensure(ConnectionId connectionId) {
        int index = connectionId.index();
        if (found(index)) return;

        try {
            lock.lock();
            if (found(index)) return;

            // ensure capacity
            if (data.length <= index) {
                Wrapper[] oldData = data;
                Wrapper[] newData = new Wrapper[oldData.length * 2];
                System.arraycopy(oldData, 0, newData, 0, oldData.length);
                data = newData;
            }

            // ensure entry
            for (Project project : Projects.getOpenProjects()) {
                ConnectionManager connectionManager = ConnectionManager.getInstance(project);
                ConnectionHandler connection = connectionManager.getConnection(connectionId);

                // cache as null if disposed
                if (isNotValid(connection)) connection = null;

                data[index] = new Wrapper(connection);
            }
        } finally {
            lock.unlock();
        }
    }

    private static boolean found(int index) {
        return data.length > index &&
                data[index] != null &&
                isValid(data[index].get());
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
