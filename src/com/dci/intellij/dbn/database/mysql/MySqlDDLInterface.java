package com.dci.intellij.dbn.database.mysql;

import java.sql.Connection;
import java.sql.SQLException;

import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseOption;
import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseSettings;
import com.dci.intellij.dbn.code.psql.style.options.PSQLCodeStyleSettings;
import com.dci.intellij.dbn.code.sql.style.options.SQLCodeStyleSettings;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.DatabaseObjectTypeId;
import com.dci.intellij.dbn.database.common.DatabaseDDLInterfaceImpl;
import com.dci.intellij.dbn.ddl.options.DDLFileSettings;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.code.SourceCodeContent;
import com.dci.intellij.dbn.object.factory.ArgumentFactoryInput;
import com.dci.intellij.dbn.object.factory.MethodFactoryInput;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;

public class MySqlDDLInterface extends DatabaseDDLInterfaceImpl {
    public MySqlDDLInterface(DatabaseInterfaceProvider provider) {
        super("mysql_ddl_interface.xml", provider);
    }


    public String createDDLStatement(Project project, DatabaseObjectTypeId objectTypeId, String userName, String schemaName, String objectName, DBContentType contentType, String code) {
        DDLFileSettings ddlFileSettings = DDLFileSettings.getInstance(project);
        boolean useQualified = ddlFileSettings.getGeneralSettings().isUseQualifiedObjectNames();
        boolean makeRerunnable = ddlFileSettings.getGeneralSettings().isMakeScriptsRerunnable();

        CodeStyleCaseSettings caseSettings = SQLCodeStyleSettings.getInstance(project).getCaseSettings();
        CodeStyleCaseOption kco = caseSettings.getKeywordCaseOption();
        CodeStyleCaseOption oco = caseSettings.getObjectCaseOption();


        if (objectTypeId == DatabaseObjectTypeId.VIEW) {
            return kco.changeCase("create" + (makeRerunnable ? " or replace" : "") + " view ") +
                    oco.changeCase((useQualified ? schemaName + "." : "") + objectName) +
                    kco.changeCase(" as\n") +
                    code;
        }

        if (objectTypeId == DatabaseObjectTypeId.PROCEDURE || objectTypeId == DatabaseObjectTypeId.FUNCTION) {
            String objectType = objectTypeId.toString().toLowerCase();
            code = updateNameQualification(code, useQualified, objectType, schemaName, objectName, caseSettings);
            String delimiterChange = kco.changeCase("delimiter $$\n");
            String dropStatement =
                    kco.changeCase("drop " + objectType + " if exists ") +
                    oco.changeCase((useQualified ? schemaName + "." : "") + objectName) + "$$\n";
            String createStatement = kco.changeCase("create definer=current_user\n") + code + "$$\n";
            String delimiterReset = kco.changeCase("delimiter ;");
            return delimiterChange + (makeRerunnable ? dropStatement : "") + createStatement + delimiterReset;
        }
        return code;
    }

    @Override
    public void computeSourceCodeOffsets(SourceCodeContent content, DatabaseObjectTypeId objectTypeId, String objectName) {
        super.computeSourceCodeOffsets(content, objectTypeId, objectName);
    }

    public String getSessionSqlMode(Connection connection) throws SQLException {
        return getSingleValue(connection, "get-session-sql-mode");
    }

    public void setSessionSqlMode(String sqlMode, Connection connection) throws SQLException {
        if (sqlMode != null) {
            executeCall(connection, null, "set-session-sql-mode", sqlMode);
        }
    }

    /*********************************************************
     *                   CHANGE statements                   *
     *********************************************************/
    public void updateView(String viewName, String oldCode, String newCode, Connection connection) throws SQLException {
        String sqlMode = getSessionSqlMode(connection);
        setSessionSqlMode("TRADITIONAL", connection);
        dropObjectIfExists("view", viewName, connection);
        try {
            createView(viewName, newCode, connection);
        } catch (SQLException e) {
            createView(viewName, oldCode, connection);
            throw e;
        } finally {
            setSessionSqlMode(sqlMode, connection);
        }
    }

    @Override
    public void updateTrigger(String tableOwner, String tableName, String triggerName, String oldCode, String newCode, Connection connection) throws SQLException {
        updateObject(triggerName, "trigger", oldCode, newCode, connection);
    }

    public void updateObject(String objectName, String objectType, String oldCode, String newCode, Connection connection) throws SQLException {
        String sqlMode = getSessionSqlMode(connection);
        setSessionSqlMode("TRADITIONAL", connection);
        dropObjectIfExists(objectType, objectName, connection);
        try {
            createObject(newCode, connection);
        } catch (SQLException e) {
            createObject(oldCode, connection);
            throw e;
        } finally {
            setSessionSqlMode(sqlMode, connection);
        }
    }

    /*********************************************************
     *                     DROP statements                   *
     *********************************************************/
    private void dropObjectIfExists(String objectType, String objectName, Connection connection) throws SQLException {
        executeUpdate(connection, "drop-object-if-exists", objectType, objectName);
    }

    /*********************************************************
     *                   CREATE statements                   *
     *********************************************************/
    public void createMethod(MethodFactoryInput method, Connection connection) throws SQLException {
        CodeStyleCaseSettings styleCaseSettings = PSQLCodeStyleSettings.getInstance(method.getSchema().getProject()).getCaseSettings();
        CodeStyleCaseOption keywordCaseOption = styleCaseSettings.getKeywordCaseOption();
        CodeStyleCaseOption objectCaseOption = styleCaseSettings.getObjectCaseOption();
        CodeStyleCaseOption dataTypeCaseOption = styleCaseSettings.getDatatypeCaseOption();

        StringBuilder buffer = new StringBuilder();
        String methodType = method.isFunction() ? "function " : "procedure ";
        buffer.append(keywordCaseOption.changeCase(methodType));
        buffer.append(objectCaseOption.changeCase(method.getObjectName()));
        buffer.append("(");

        int maxArgNameLength = 0;
        int maxArgDirectionLength = 0;
        for (ArgumentFactoryInput argument : method.getArguments()) {
            maxArgNameLength = Math.max(maxArgNameLength, argument.getObjectName().length());
            maxArgDirectionLength = Math.max(maxArgDirectionLength,
                    argument.isInput() && argument.isOutput() ? 5 :
                    argument.isInput() ? 2 :
                    argument.isOutput() ? 3 : 0);
        }


        for (ArgumentFactoryInput argument : method.getArguments()) {
            buffer.append("\n    ");
            
            if (!method.isFunction()) {
                String direction =
                        argument.isInput() && argument.isOutput() ? keywordCaseOption.changeCase("inout") :
                                argument.isInput() ? keywordCaseOption.changeCase("in") :
                                        argument.isOutput() ? keywordCaseOption.changeCase("out") : "";
                buffer.append(direction);
                buffer.append(StringUtil.repeatSymbol(' ', maxArgDirectionLength - direction.length() + 1));
            }

            buffer.append(objectCaseOption.changeCase(argument.getObjectName()));
            buffer.append(StringUtil.repeatSymbol(' ', maxArgNameLength - argument.getObjectName().length() + 1));

            buffer.append(dataTypeCaseOption.changeCase(argument.getDataType()));
            if (argument != method.getArguments().get(method.getArguments().size() -1)) {
                buffer.append(",");
            }
        }

        buffer.append(")\n");
        if (method.isFunction()) {
            buffer.append(keywordCaseOption.changeCase("returns "));
            buffer.append(dataTypeCaseOption.changeCase(method.getReturnArgument().getDataType()));
            buffer.append("\n");
        }
        buffer.append(keywordCaseOption.changeCase("begin\n\n"));
        if (method.isFunction()) buffer.append(keywordCaseOption.changeCase("    return null;\n\n"));
        buffer.append("end");
        
        String sqlMode = getSessionSqlMode(connection);
        try {
            setSessionSqlMode("TRADITIONAL", connection);
            createObject(buffer.toString(), connection);
        } finally {
            setSessionSqlMode(sqlMode, connection);
        }

    }

}
