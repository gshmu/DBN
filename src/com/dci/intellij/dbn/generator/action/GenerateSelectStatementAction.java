package com.dci.intellij.dbn.generator.action;

import java.util.List;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.generator.StatementGenerationManager;
import com.dci.intellij.dbn.generator.StatementGeneratorResult;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.project.Project;

public class GenerateSelectStatementAction extends GenerateStatementAction {
    private List<DBObjectRef> selectedObjectRefs;

    public GenerateSelectStatementAction(List<DBObject> selectedObjects) {
        super("SELECT Statement");
        this.selectedObjectRefs = DBObjectRef.from(selectedObjects);
    }

    @Override
    protected StatementGeneratorResult generateStatement(Project project) {
        StatementGenerationManager statementGenerationManager = StatementGenerationManager.getInstance(project);
        List<DBObject> selectedObjects = getSelectedObjects();
        return statementGenerationManager.generateSelectStatement(selectedObjects, true);
    }

    public List<DBObject> getSelectedObjects() {
        return DBObjectRef.getnn(selectedObjectRefs);
    }

    @Nullable
    @Override
    public ConnectionHandler getConnectionHandler() {
        List<DBObject> selectedObjects = getSelectedObjects();
        if (selectedObjects.size() > 0) {
            return selectedObjects.get(0).getConnectionHandler();
        }
        return null;
    }
}
