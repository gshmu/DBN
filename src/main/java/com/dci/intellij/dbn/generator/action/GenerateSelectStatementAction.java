package com.dci.intellij.dbn.generator.action;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.generator.StatementGenerationManager;
import com.dci.intellij.dbn.generator.StatementGeneratorResult;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GenerateSelectStatementAction extends GenerateStatementAction {
    private final List<DBObjectRef<DBObject>> selectedObjectRefs;

    GenerateSelectStatementAction(List<DBObject> selectedObjects) {
        super("SELECT Statement");
        this.selectedObjectRefs = DBObjectRef.from(selectedObjects);
    }

    @Override
    protected StatementGeneratorResult generateStatement(Project project) {
        StatementGenerationManager statementGenerationManager = StatementGenerationManager.getInstance(project);
        List<DBObject> selectedObjects = getSelectedObjects();
        return statementGenerationManager.generateSelectStatement(selectedObjects, true);
    }

    private List<DBObject> getSelectedObjects() {
        return DBObjectRef.ensure(selectedObjectRefs);
    }

    @Nullable
    @Override
    public ConnectionHandler getConnection() {
        List<DBObject> selectedObjects = getSelectedObjects();
        if (selectedObjects.size() > 0) {
            return selectedObjects.get(0).getConnection();
        }
        return null;
    }
}
