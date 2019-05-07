package com.dci.intellij.dbn.execution.explain;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.routine.ParametricRunnable;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ResourceUtil;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseInterface;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.explain.result.ExplainPlanResult;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ExplainPlanManager extends AbstractProjectComponent {
    private ExplainPlanManager(Project project) {
        super(project);
        //EventManager.subscribe(project, PsiDocumentTransactionListener.TOPIC, psiDocumentTransactionListener);
    }

    public static ExplainPlanManager getInstance(@NotNull Project project) {
        return Failsafe.getComponent(project, ExplainPlanManager.class);
    }

    @Override
    public void disposeInner() {
        //EventManager.unsubscribe(psiDocumentTransactionListener);
        super.disposeInner();
    }

    /*********************************************************
     *                       Execution                       *
     *********************************************************/

    public void explainPlan(
            @NotNull ExecutablePsiElement executable,
            @Nullable ParametricRunnable.Basic<ExplainPlanResult> callback) {

        Project project = getProject();
        String elementDescription = executable.getSpecificElementType().getDescription();

        DBLanguagePsiFile psiFile = executable.getFile();
        FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
        connectionMappingManager.selectConnectionAndSchema(psiFile,
                ()-> ConnectionAction.invoke("generating the explain plan", false, executable.getFile(),
                        (action) -> Progress.prompt(getProject(), "Extracting explain plan for " + elementDescription, true,
                                (progress) -> {
                                    ConnectionHandler connectionHandler = action.getConnectionHandler();
                                    ExplainPlanResult explainPlanResult;
                                    try {
                                        explainPlanResult = DatabaseInterface.call(true,
                                                connectionHandler,
                                                (provider, connection) -> {
                                                    SchemaId currentSchema = executable.getFile().getSchemaId();
                                                    connectionHandler.setCurrentSchema(connection, currentSchema);
                                                    Statement statement = null;
                                                    ResultSet resultSet = null;
                                                    try {
                                                        DatabaseMetadataInterface metadataInterface = provider.getMetadataInterface();
                                                        metadataInterface.clearExplainPlanData(connection);

                                                        DatabaseCompatibilityInterface compatibilityInterface = provider.getCompatibilityInterface();
                                                        String explainPlanStatementPrefix = compatibilityInterface.getExplainPlanStatementPrefix();
                                                        String explainPlanQuery = explainPlanStatementPrefix + "\n" + executable.prepareStatementText();
                                                        statement = connection.createStatement();
                                                        statement.execute(explainPlanQuery);

                                                        resultSet = metadataInterface.loadExplainPlan(connection);
                                                        return new ExplainPlanResult(executable, resultSet);

                                                    } finally {
                                                        ResourceUtil.close(resultSet);
                                                        ResourceUtil.close(statement);
                                                        ResourceUtil.rollbackSilently(connection);
                                                    }
                                                });
                                    } catch (SQLException e) {
                                        explainPlanResult = new ExplainPlanResult(executable, e.getMessage());
                                    }


                                    if (callback == null) {
                                        ExecutionManager executionManager = ExecutionManager.getInstance(project);
                                        executionManager.addExplainPlanResult(explainPlanResult);
                                    } else {
                                        callback.run(explainPlanResult);
                                    }
                                })));
    }

    /*********************************************************
     *                    ProjectComponent                   *
     *********************************************************/
    @Override
    @NotNull
    @NonNls
    public String getComponentName() {
        return "DBNavigator.Project.ExplainPlanManager";
    }

}
