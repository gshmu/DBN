package com.dci.intellij.dbn.generator.action;

import com.dci.intellij.dbn.generator.StatementGenerationManager;
import com.dci.intellij.dbn.generator.StatementGeneratorResult;
import com.dci.intellij.dbn.object.DBTable;
import com.intellij.openapi.project.Project;

public class GenerateInsertStatementAction extends GenerateStatementAction {
    private DBTable table;

    public GenerateInsertStatementAction(DBTable table) {
        super("INSERT Statement");
        this.table = table;
    }

    @Override
    protected StatementGeneratorResult generateStatement(Project project) {
        StatementGenerationManager statementGenerationManager = StatementGenerationManager.getInstance(project);
        return statementGenerationManager.generateInsert(table);
    }
}
