package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.Icons;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import java.util.ArrayList;
import java.util.List;

public class ProjectConnectionBundle extends ConnectionBundle implements ProjectComponent {
    private List<ConnectionHandler> virtualConnections = new ArrayList<ConnectionHandler>();
    private ProjectConnectionBundle(Project project) {
        super(project);

        virtualConnections.add(new VirtualConnectionHandler(
                "virtual-oracle-connection",
                "Virtual - Oracle 10.1",
                DatabaseType.ORACLE,
                project));

        virtualConnections.add(new VirtualConnectionHandler(
                "virtual-mysql-connection", 
                "Virtual - MySQL 5.0",
                DatabaseType.MYSQL,
                project));

        virtualConnections.add(new VirtualConnectionHandler(
                "virtual-postgres-connection",
                "Virtual - PostgreSQL 9.3.4",
                DatabaseType.POSTGRES,
                project));

        virtualConnections.add(new VirtualConnectionHandler(
                "virtual-iso92-sql-connection",
                "Virtual - ISO-92 SQL",
                DatabaseType.UNKNOWN,
                project));
    }

    @Override
    public String getDisplayName() {
        return "DB Connections";
    }

    @NotNull
    @Override
    public String getId() {
        return "DBNavigator.Project.ConnectionBundle";
    }

    public static ProjectConnectionBundle getInstance(Project project) {
        return project.getComponent(ProjectConnectionBundle.class);
    }

    public List<ConnectionHandler> getVirtualConnections() {
        return virtualConnections;
    }

    public ConnectionHandler getVirtualConnection(String id) {
        for (ConnectionHandler virtualConnection : virtualConnections) {
            if (virtualConnection.getId().equals(id)) {
                return virtualConnection;
            }
        }
        return null;
    }

    public Icon getIcon(int flags) {
        return Icons.PROJECT;
    }

    /***************************************
    *            ProjectComponent           *
    ****************************************/
    @NotNull
    @NonNls
    public String getComponentName() {
        return "DBNavigator.Project.ConnectionManager";
    }
    public void projectOpened() {}
    public void projectClosed() {}
    public void initComponent() {}
    public void disposeComponent() {
        dispose();
    }

    @Override
    public String toString() {
        return "ProjectConnectionBundle";
    }

    public int compareTo(@NotNull Object o) {
        return -1;
    }
}
