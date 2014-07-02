package com.dci.intellij.dbn.database.oracle;

import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseOption;
import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseSettings;
import com.dci.intellij.dbn.code.psql.style.options.PSQLCodeStyleSettings;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.DatabaseObjectTypeId;
import com.dci.intellij.dbn.database.common.DatabaseDDLInterfaceImpl;
import com.dci.intellij.dbn.editor.code.SourceCodeContent;
import com.dci.intellij.dbn.object.factory.ArgumentFactoryInput;
import com.dci.intellij.dbn.object.factory.MethodFactoryInput;
import com.intellij.openapi.util.text.StringUtil;

import java.sql.Connection;
import java.sql.SQLException;

public class OracleDDLInterface extends DatabaseDDLInterfaceImpl {
    public OracleDDLInterface(DatabaseInterfaceProvider provider) {
        super("oracle_ddl_interface.xml", provider);
    }

    public int getEditorHeaderEndOffset(DatabaseObjectTypeId objectTypeId, String objectName, String content) {
        if (objectTypeId == DatabaseObjectTypeId.TRIGGER) {
            if (content.length() > 0) {
                int startIndex = StringUtil.indexOfIgnoreCase(content, objectName, 0) + objectName.length();
                int offset = StringUtil.indexOfIgnoreCase(content, "declare", startIndex);
                if (offset == -1) offset = StringUtil.indexOfIgnoreCase(content, "begin", startIndex);
                if (offset == -1) offset = StringUtil.indexOfIgnoreCase(content, "call", startIndex);
                if (offset == -1) offset = 0;                
                return offset;
            }
        }
        return 0;
    }

    @Override
    public void computeSourceCodeOffsets(SourceCodeContent content, DatabaseObjectTypeId objectTypeId, String objectName) {
        String sourceCode = content.getSourceCode();
        if (objectTypeId == DatabaseObjectTypeId.TRIGGER) {
            if (sourceCode.length() > 0) {
                int startIndex = StringUtil.indexOfIgnoreCase(sourceCode, objectName, 0) + objectName.length();
                int headerEndOffset = StringUtil.indexOfIgnoreCase(sourceCode, "declare", startIndex);
                if (headerEndOffset == -1) headerEndOffset = StringUtil.indexOfIgnoreCase(sourceCode, "begin", startIndex);
                if (headerEndOffset == -1) headerEndOffset = StringUtil.indexOfIgnoreCase(sourceCode, "call", startIndex);
                if (headerEndOffset == -1) headerEndOffset = 0;
                content.getOffsets().setHeaderEndOffset(headerEndOffset);
            }
        }

        if (objectTypeId != DatabaseObjectTypeId.VIEW) {
            int nameIndex = StringUtil.indexOfIgnoreCase(sourceCode, objectName, 0);
            if (nameIndex > -1) {
                int guardedBlockEndOffset = nameIndex + objectName.length();
                if (sourceCode.charAt(guardedBlockEndOffset) == '"'){
                    guardedBlockEndOffset++;
                }
                content.getOffsets().setGuardedBlockEndOffset(guardedBlockEndOffset);
            }
        }
    }

    /*********************************************************
     *                   CHANGE statements                   *
     *********************************************************/
    public void updateView(String viewName, String oldCode, String newCode, Connection connection) throws SQLException {
        executeUpdate(connection, "change-view", viewName, newCode);
    }

    @Override
    public void updateTrigger(String tableOwner, String tableName, String triggerName, String oldCode, String newCode, Connection connection) throws SQLException {
        updateObject(triggerName, "trigger", oldCode, newCode, connection);
    }

    public void updateObject(String objectName, String objectType, String oldCode, String newCode, Connection connection) throws SQLException {
        // code contains object type and name
        executeUpdate(connection, "change-object", newCode);
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
                    argument.isInput() && argument.isOutput() ? 6 :
                    argument.isInput() ? 2 :
                    argument.isOutput() ? 3 : 0);
        }


        for (ArgumentFactoryInput argument : method.getArguments()) {
            buffer.append("\n    ");
            buffer.append(objectCaseOption.changeCase(argument.getObjectName()));
            buffer.append(StringUtil.repeatSymbol(' ', maxArgNameLength - argument.getObjectName().length() + 1));
            String direction =
                    argument.isInput() && argument.isOutput() ? keywordCaseOption.changeCase("in out") :
                    argument.isInput() ? keywordCaseOption.changeCase("in") :
                    argument.isOutput() ? keywordCaseOption.changeCase("out") : "";
            buffer.append(direction);
            buffer.append(StringUtil.repeatSymbol(' ', maxArgDirectionLength - direction.length() + 1));
            buffer.append(dataTypeCaseOption.changeCase(argument.getDataType()));
            if (argument != method.getArguments().get(method.getArguments().size() -1)) {
                buffer.append(",");
            }
        }

        buffer.append(")\n");
        if (method.isFunction()) {
            buffer.append(keywordCaseOption.changeCase("return "));
            buffer.append(dataTypeCaseOption.changeCase(method.getReturnArgument().getDataType()));
            buffer.append("\n");
        }
        buffer.append(keywordCaseOption.changeCase("is\nbegin\n\n"));
        if (method.isFunction()) buffer.append(keywordCaseOption.changeCase("    return null;\n\n"));
        buffer.append("end;");
        createObject(buffer.toString(), connection);
    }
}