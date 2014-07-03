package com.dci.intellij.dbn.execution.compiler;

import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.execution.common.message.ConsoleMessage;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DatabaseContentFile;
import com.dci.intellij.dbn.vfs.DatabaseEditableObjectFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CompilerMessage extends ConsoleMessage {
    private CompilerResult compilerResult;
    private int line;
    private int position;
    private DBContentType contentType;
    private DatabaseEditableObjectFile databaseFile;
    private DatabaseContentFile contentFile;
    private boolean isEcho;
    private String subjectIdentifier;

    public CompilerMessage(CompilerResult compilerResult, String text, MessageType type) {
        super(type, text);
        this.compilerResult = compilerResult;

        Disposer.register(this, compilerResult);
    }

    public CompilerMessage(CompilerResult compilerResult, String text) {
        super(MessageType.INFO, text);
        this.compilerResult = compilerResult;

        Disposer.register(this, compilerResult);
    }

    public CompilerMessage(CompilerResult compilerResult, ResultSet resultSet) throws SQLException {
        super(MessageType.ERROR, resultSet.getString("TEXT"));
        line = resultSet.getInt("LINE");
        position = resultSet.getInt("POSITION");

        line = Math.max(line-1, 0);
        position = Math.max(position-1, 0);
        this.compilerResult = compilerResult;

        if (compilerResult.getObject().getContentType() == DBContentType.CODE_SPEC_AND_BODY) {
            String objectType = resultSet.getString("OBJECT_TYPE");
            contentType = objectType.contains("BODY") ?  DBContentType.CODE_BODY : DBContentType.CODE_SPEC;
        } else {
            contentType = compilerResult.getObject().getContentType();
        }
        isEcho = !text.startsWith("PLS");

        subjectIdentifier = extractIdentifier(text, '\'');
        if (subjectIdentifier == null) subjectIdentifier = extractIdentifier(text, '"');

        Disposer.register(this, compilerResult);
    }

    public String getSubjectIdentifier() {
        return subjectIdentifier;
    }

    private static String extractIdentifier(String message, char identifierQuoteChar) {
        int startIndex = message.indexOf(identifierQuoteChar);
        if (startIndex > -1) {
            startIndex = startIndex + 1;
            int endIndex = message.indexOf(identifierQuoteChar, startIndex);
            if (endIndex > -1) {
                return message.substring(startIndex, endIndex);
            }
        }
        return null;
    }

    public DatabaseEditableObjectFile getDatabaseFile() {
        if (databaseFile == null) {
            databaseFile = compilerResult.getObject().getVirtualFile();
        }
        return databaseFile;
    }

    public DatabaseContentFile getContentFile() {
        if (contentFile == null) {
            DatabaseEditableObjectFile databaseFile = getDatabaseFile();
            contentFile = databaseFile.getContentFile(contentType);
        }
        return contentFile;
    }

    public CompilerResult getCompilerResult() {
        return compilerResult;
    }

    public DBSchemaObject getObject() {
        return compilerResult.getObject();
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setSubjectIdentifier(String subjectIdentifier) {
        this.subjectIdentifier = subjectIdentifier;
    }

    public int getPosition() {
        return position;
    }

    public String getText() {
        return text;
    }

    public boolean isEcho() {
        return isEcho;
    }

    public Project getProject() {
        return compilerResult.getProject();
    }

    public void dispose() {
        compilerResult = null;
        databaseFile = null;
        contentFile = null;
    }
}
