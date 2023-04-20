package com.dci.intellij.dbn.ddl;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.component.PersistentState;
import com.dci.intellij.dbn.common.component.ProjectComponentBase;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.PooledConnection;
import com.dci.intellij.dbn.connection.Resources;
import com.dci.intellij.dbn.connection.jdbc.DBNCallableStatement;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.sql.Types;

import static com.dci.intellij.dbn.common.component.Components.projectService;

@State(
        name = DDLManager.COMPONENT_NAME,
        storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class DDLManager extends ProjectComponentBase implements PersistentState {

    public static final String COMPONENT_NAME = "DBNavigator.Project.DDLManager";

    private DDLManager(@NotNull Project project) {
        super(project, COMPONENT_NAME);
    }

    public static DDLManager getInstance(@NotNull Project project) {
        return projectService(project, DDLManager.class);
    }

    public String extractDDL(DBObject object) throws SQLException {
        // TODO move to database interface (ORACLE)
        ConnectionHandler connection = object.getConnection();
        return PooledConnection.call(connection.createConnectionContext(), conn -> {
            DBNCallableStatement statement = null;
            try {
                DBObjectType objectType = object.getObjectType();
                DBObjectType genericType = objectType.getGenericType();
                objectType = genericType == DBObjectType.TRIGGER ? genericType : objectType;
                String objectTypeName = objectType.getName().toUpperCase();

                statement = conn.prepareCall("{? = call DBMS_METADATA.GET_DDL(?, ?, ?)}");
                statement.registerOutParameter(1, Types.CLOB);
                statement.setString(2, objectTypeName);
                statement.setString(3, object.getName());
                statement.setString(4, object.getSchema().getName());

                statement.execute();
                String ddl = statement.getString(1);
                return ddl == null ? null : ddl.trim();
            } finally {
                Resources.close(statement);
            }
        });
    }

    @Override
    public Element getComponentState() {
        return null;
    }

    @Override
    public void loadComponentState(@NotNull Element state) {

    }
}
