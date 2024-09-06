package com.dbn.oracleAI;

import com.dbn.DatabaseNavigator;
import com.dbn.common.component.PersistentState;
import com.dbn.common.component.ProjectComponentBase;
import com.dbn.common.thread.Progress;
import com.dbn.connection.ConnectionHandler;
import com.dbn.database.interfaces.DatabaseInterfaceInvoker;
import com.dbn.diagnostics.Diagnostics;
import com.dbn.oracleAI.config.ProviderConfiguration;
import com.dbn.oracleAI.types.ProviderType;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import lombok.extern.slf4j.Slf4j;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import static com.dbn.common.Priority.HIGH;
import static com.dbn.common.component.Components.projectService;
import static com.dbn.common.util.Messages.showErrorDialog;
import static com.dbn.common.util.Messages.showInfoDialog;

@Slf4j
@State(
        name = DatabaseAssistantPrerequisiteManager.COMPONENT_NAME,
        storages = @Storage(DatabaseNavigator.STORAGE_FILE))
public class DatabaseAssistantPrerequisiteManager extends ProjectComponentBase implements PersistentState {
    public static final String COMPONENT_NAME = "DBNavigator.Project.DatabaseAssistantPrerequisiteManager";

    private DatabaseAssistantPrerequisiteManager(Project project) {
        super(project, COMPONENT_NAME);
    }

    public static DatabaseAssistantPrerequisiteManager getInstance(@NotNull Project project) {
        return projectService(project, DatabaseAssistantPrerequisiteManager.class);
    }

    private String getAccessPoint(ProviderType providerType) {
        return providerType == null ? "" : ProviderConfiguration.getAccessPoint(providerType);
    }

    public void grantNetworkAccess(ConnectionHandler connection, ProviderType providerType, String command) {
        Project project = connection.getProject();

        String host = getAccessPoint(providerType);
        String user = connection.getUserName();
        String title = txt("prc.assistant.title.GrantingAccess");
        String message = txt("prc.assistant.message.GrantingNetworkAccess", host, user);

        Progress.modal(project, connection, false, title, message, progress -> {
            try {
                DatabaseInterfaceInvoker.execute(HIGH, title, message, project, connection.getConnectionId(),
                        c -> connection.getAssistantInterface().grantACLRights(c, command));

                showInfoDialog(project, txt("msg.assistant.title.AccessGranted"), txt("msg.assistant.info.NetworkAccessGranted", host, user));
            } catch (Throwable e) {
                Diagnostics.conditionallyLog(e);
                showErrorDialog(project, txt("msg.assistant.title.AccessGrantFailed"), txt("msg.assistant.error.NetworkAccessGrantFailed", host, user, e.getMessage()));
            }
        });
    }

    public void grantExecutionPrivileges(ConnectionHandler connection, String user) {
        String title = txt("prc.assistant.title.GrantingPrivileges");
        String message = txt("prc.assistant.message.GrantingExecutionPrivileges", user);

        Project project = getProject();
        Progress.modal(project, connection, false, title, message, progress -> {
            try {
                DatabaseInterfaceInvoker.execute(HIGH, title, message, project, connection.getConnectionId(),
                        c -> connection.getAssistantInterface().grantPrivilege(c, user));

                showInfoDialog(project, txt("msg.assistant.title.PrivilegesGranted"), txt("msg.assistant.info.ExecutionPrivilegesGranted", user));
            } catch (Throwable e) {
                Diagnostics.conditionallyLog(e);
                showErrorDialog(project, txt("msg.assistant.title.PrivilegesGrantFailed"), txt("msg.assistant.error.ExecutionPrivilegesGrantFailed", user, e.getMessage()));
            }
        });
    }

    /*********************************************
     *            PersistentStateComponent       *
     *********************************************/
    @Override
    public Element getComponentState() {
        return null;
    }

    @Override
    public void loadComponentState(@NotNull Element element) {

    }
}
