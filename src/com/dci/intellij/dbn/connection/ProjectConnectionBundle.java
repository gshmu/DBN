package com.dci.intellij.dbn.connection;

import javax.swing.Icon;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.project.Project;

@State(
    name = "DBNavigator.Project.ConnectionManager",
    storages = {
        @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/dbnavigator.xml", scheme = StorageScheme.DIRECTORY_BASED),
        @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/misc.xml", scheme = StorageScheme.DIRECTORY_BASED),
        @Storage(file = StoragePathMacros.PROJECT_FILE)}
)
public class ProjectConnectionBundle extends ConnectionBundle implements ProjectComponent, PersistentStateComponent<Element> {
    private List<ConnectionHandler> virtualConnections = new ArrayList<ConnectionHandler>();
    private ProjectConnectionBundle(Project project) {
        super(project);

        virtualConnections.add(new VirtualConnectionHandler(
                "virtual-oracle-connection",
                "Virtual - Oracle 10.1",
                DatabaseType.ORACLE,
                10.1,
                project));

        virtualConnections.add(new VirtualConnectionHandler(
                "virtual-mysql-connection", 
                "Virtual - MySQL 5.0",
                DatabaseType.MYSQL,
                5.0,
                project));

        virtualConnections.add(new VirtualConnectionHandler(
                "virtual-postgres-connection",
                "Virtual - PostgreSQL 9.3.4",
                DatabaseType.POSTGRES,
                9.3,
                project));

        virtualConnections.add(new VirtualConnectionHandler(
                "virtual-iso92-sql-connection",
                "Virtual - ISO-92 SQL",
                DatabaseType.UNKNOWN,
                92,
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
