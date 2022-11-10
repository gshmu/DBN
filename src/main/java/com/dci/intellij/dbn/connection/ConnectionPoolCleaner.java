package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.project.Projects;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.intellij.openapi.project.Project;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

class ConnectionPoolCleaner extends TimerTask {
    public static final ConnectionPoolCleaner INSTANCE = new ConnectionPoolCleaner();


    @Override
    public void run() {
        Project[] projects = Projects.getOpenProjects();
        for (Project project : projects) {
            ConnectionManager connectionManager = ConnectionManager.getInstance(project);
            List<ConnectionHandler> connections = connectionManager.getConnections();
            for (ConnectionHandler connection : connections) {
                ConnectionPool connectionPool = connection.getConnectionPool();
                connectionPool.clean();
            }

        }
    }

    void start() {
        Timer poolCleaner = new Timer("DBN - Idle Connection Pool Cleaner");
        poolCleaner.schedule(INSTANCE, TimeUtil.Millis.ONE_MINUTE, TimeUtil.Millis.ONE_MINUTE);
    }

}


