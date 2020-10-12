package com.dci.intellij.dbn.generator.action;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.generator.StatementGeneratorResult;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

public class GenerateDDLStatementAction extends GenerateStatementAction {
    private DBObjectRef objectRef;

    GenerateDDLStatementAction(DBObject object) {
        super("DDL Statement");
        objectRef = DBObjectRef.of(object);

    }

    @Nullable
    @Override
    public ConnectionHandler getConnectionHandler() {
        return getObject().getConnectionHandler();
    }

    @NotNull
    public DBObject getObject() {
        return DBObjectRef.ensure(objectRef);
    }

    @Override
    protected StatementGeneratorResult generateStatement(Project project) {
        StatementGeneratorResult result = new StatementGeneratorResult();
        DBObject object = getObject();
        try {
            String statement = object.extractDDL();
            if (StringUtil.isEmptyOrSpaces(statement)) {
                String message =
                        "Could not extract DDL statement for " + object.getQualifiedNameWithType() + ".\n" +
                                "You may not have enough rights to perform this action. Please contact your database administrator for more details.";
                result.getMessages().addErrorMessage(message);
            }

            result.setStatement(statement);
        } catch (SQLException e) {
            result.getMessages().addErrorMessage(
                    "Could not extract DDL statement for " + object.getQualifiedNameWithType() + ".\n" +
                            "Cause: " + e.getMessage());
        }
        return result;
    }
}
