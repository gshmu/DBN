package com.dci.intellij.dbn.execution.explain;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.thread.RunnableTask;
import com.dci.intellij.dbn.common.thread.TaskInstructions;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.explain.result.ExplainPlanResult;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.dci.intellij.dbn.object.DBSchema;
import com.intellij.openapi.project.Project;

public class ExplainPlanManager extends AbstractProjectComponent {
    private ExplainPlanManager(Project project) {
        super(project);
        //EventManager.subscribe(project, PsiDocumentTransactionListener.TOPIC, psiDocumentTransactionListener);
    }

    public static ExplainPlanManager getInstance(@NotNull Project project) {
        return project.getComponent(ExplainPlanManager.class);
    }

    @Override
    public void dispose() {
        super.dispose();
        //EventManager.unsubscribe(psiDocumentTransactionListener);
    }

    /*********************************************************
     *                       Execution                       *
     *********************************************************/

    public void explainPlan(final ExecutablePsiElement executable, final @Nullable RunnableTask<ExplainPlanResult> callback) {
        TaskInstructions taskInstructions = new TaskInstructions("Extracting explain plan for " + executable.getElementType().getDescription(), false, true);
        ConnectionAction explainAction = new ConnectionAction("generating the explain plan", executable.getFile(), taskInstructions) {
            @Override
            protected void execute() {
                ConnectionHandler connectionHandler = getConnectionHandler();
                DBSchema currentSchema = executable.getFile().getCurrentSchema();
                ExplainPlanResult explainPlanResult = null;
                Connection connection = null;
                Statement statement = null;
                ResultSet resultSet = null;
                try {
                    DatabaseInterfaceProvider interfaceProvider = connectionHandler.getInterfaceProvider();
                    DatabaseMetadataInterface metadataInterface = interfaceProvider.getMetadataInterface();
                    connection = connectionHandler.getPoolConnection(currentSchema);
                    metadataInterface.clearExplainPlanData(connection);

                    DatabaseCompatibilityInterface compatibilityInterface = interfaceProvider.getCompatibilityInterface();
                    String explainPlanStatementPrefix = compatibilityInterface.getExplainPlanStatementPrefix();
                    String explainPlanQuery = explainPlanStatementPrefix + "\n" + executable.prepareStatementText();
                    statement = connection.createStatement();
                    statement.execute(explainPlanQuery);

                    resultSet = metadataInterface.loadExplainPlan(connection);
                    explainPlanResult = new ExplainPlanResult(executable, resultSet);

                } catch (SQLException e) {
                    explainPlanResult = new ExplainPlanResult(executable, e.getMessage());
                } finally {
                    ConnectionUtil.rollback(connection);
                    ConnectionUtil.closeResultSet(resultSet);
                    ConnectionUtil.closeStatement(statement);
                    connectionHandler.freePoolConnection(connection);
                }

                if (callback == null) {
                    ExecutionManager executionManager = ExecutionManager.getInstance(getProject());
                    executionManager.addExplainPlanResult(explainPlanResult);
                } else {
                    callback.setOption(explainPlanResult);
                    callback.start();
                }
            }
        };

        FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(getProject());
        connectionMappingManager.selectConnectionAndSchema(executable.getFile(), explainAction);
    }

    /*********************************************************
     *                    ProjectComponent                   *
     *********************************************************/
    @NotNull
    @NonNls
    public String getComponentName() {
        return "DBNavigator.Project.ExplainPlanManager";
    }

}
