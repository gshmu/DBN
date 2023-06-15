package com.dci.intellij.dbn.generator.action;

import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.ddl.DDLManager;
import com.dci.intellij.dbn.generator.StatementGeneratorResult;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

import static com.dci.intellij.dbn.common.dispose.Failsafe.conditionallyLog;

public class GenerateDDLStatementAction extends GenerateStatementAction {
    private final DBObjectRef object;

    GenerateDDLStatementAction(DBObject object) {
        super("DDL Statement");
        this.object = DBObjectRef.of(object);

    }

    @Nullable
    @Override
    public ConnectionHandler getConnection() {
        return getObject().getConnection();
    }



    @NotNull
    public DBObject getObject() {
        return DBObjectRef.ensure(object);
    }

    @Override
    protected StatementGeneratorResult generateStatement(Project project) {
        StatementGeneratorResult result = new StatementGeneratorResult();
        DBObject object = getObject();
        try {
            DDLManager ddlManager = DDLManager.getInstance(project);
            String statement = ddlManager.extractDDL(object);
            if (Strings.isEmptyOrSpaces(statement)) {
                String message =
                        "Could not extract DDL statement for " + object.getQualifiedNameWithType() + ".\n" +
                                "You may not have enough rights to perform this action. Please contact your database administrator for more details.";
                result.getMessages().addErrorMessage(message);
            }

            result.setStatement(statement);
        } catch (SQLException e) {
            conditionallyLog(e);
            result.getMessages().addErrorMessage(
                    "Could not extract DDL statement for " + object.getQualifiedNameWithType() + ".\n" +
                            "Cause: " + e.getMessage());
        }
        return result;
    }
}
