package com.dci.intellij.dbn.execution.explain;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;

public class ExplainPlanManager extends AbstractProjectComponent {
    private ExplainPlanManager(Project project) {
        super(project);
        //EventManager.subscribe(project, PsiDocumentTransactionListener.TOPIC, psiDocumentTransactionListener);
    }

    public static ExplainPlanManager getInstance(Project project) {
        return project.getComponent(ExplainPlanManager.class);
    }

    @Override
    public void disposeComponent() {
        //EventManager.unsubscribe(psiDocumentTransactionListener);
        super.disposeComponent();
    }

    /*********************************************************
     *                       Execution                       *
     *********************************************************/

    public void explainPlan(FileEditor fileEditor, ExecutablePsiElement executable) {
        BackgroundTask explainTask = new BackgroundTask(getProject(), "Extracting explain plan for " + executable.getElementType().getDescription(), false, true) {
            public void execute(@NotNull ProgressIndicator progressIndicator) {
                initProgressIndicator(progressIndicator, true);

            }
        };

        FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(getProject());
        connectionMappingManager.selectConnectionAndSchema(executable.getFile(), explainTask);
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
