package com.dci.intellij.dbn.generator.action;

import com.dci.intellij.dbn.generator.StatementGenerationManager;
import com.dci.intellij.dbn.generator.StatementGeneratorResult;
import com.dci.intellij.dbn.object.common.DBObject;
import com.intellij.openapi.project.Project;

import java.util.List;

public class GenerateSelectStatementAction extends GenerateStatementAction {
    private List<DBObject> selectedObjects;

    public GenerateSelectStatementAction(List<DBObject> selectedObjects) {
        super("SELECT Statement");
        this.selectedObjects = selectedObjects;
    }

    @Override
    protected StatementGeneratorResult generateStatement(Project project) {
        StatementGenerationManager statementGenerationManager = StatementGenerationManager.getInstance(project);
        return statementGenerationManager.generateSelectStatement(selectedObjects, true);
    }
}
