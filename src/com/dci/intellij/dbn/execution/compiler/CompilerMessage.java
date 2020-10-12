package com.dci.intellij.dbn.execution.compiler;

import com.dci.intellij.dbn.common.dispose.DisposeUtil;
import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.execution.common.message.ConsoleMessage;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.file.DBContentVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBEditableObjectVirtualFile;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CompilerMessage extends ConsoleMessage implements Comparable<CompilerMessage> {
    private final CompilerResult compilerResult;
    private final DBContentType contentType;
    private String subjectIdentifier;
    private int line;
    private int position;
    private boolean echo;
    private DBEditableObjectVirtualFile databaseFile;
    private DBContentVirtualFile contentFile;

    public CompilerMessage(CompilerResult compilerResult, DBContentType contentType, String text, MessageType type) {
        super(type, text);
        this.compilerResult = compilerResult;
        this.contentType = contentType;
    }

    public CompilerMessage(CompilerResult compilerResult, DBContentType contentType, String text) {
        super(MessageType.INFO, text);
        this.compilerResult = compilerResult;
        this.contentType = contentType;
    }

    public CompilerMessage(CompilerResult compilerResult, ResultSet resultSet) throws SQLException {
        super(MessageType.ERROR, resultSet.getString("TEXT"));
        line = resultSet.getInt("LINE");
        position = resultSet.getInt("POSITION");

        line = Math.max(line-1, 0);
        position = Math.max(position-1, 0);
        this.compilerResult = compilerResult;

        DBContentType objectContentType = DBContentType.get(compilerResult.getObjectType());
        if (objectContentType == DBContentType.CODE_SPEC_AND_BODY) {
            String objectType = resultSet.getString("OBJECT_TYPE");
            contentType = objectType.contains("BODY") ?  DBContentType.CODE_BODY : DBContentType.CODE_SPEC;
        } else {
            contentType = objectContentType;
        }

        echo = !text.startsWith("PLS") && !text.contains("ORA");
        if (echo) {
            setType(MessageType.WARNING);
        }

        subjectIdentifier = extractIdentifier(text, '\'');
        if (subjectIdentifier == null) subjectIdentifier = extractIdentifier(text, '"');
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

    @Nullable
    public DBEditableObjectVirtualFile getDatabaseFile() {
        DBSchemaObject schemaObject = compilerResult.getObject();
        if (databaseFile == null && schemaObject != null) {
            databaseFile = schemaObject.getEditableVirtualFile();
        }
        return databaseFile;
    }

    @Nullable
    public DBContentVirtualFile getContentFile() {
        if (contentFile == null) {
            DBEditableObjectVirtualFile databaseFile = getDatabaseFile();
            if (databaseFile != null) {
                contentFile = databaseFile.getContentFile(contentType);
            }
        }
        return contentFile;
    }

    public DBContentType getContentType() {
        return contentType;
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

    @Override
    public String getText() {
        return text;
    }

    public boolean isEcho() {
        return echo;
    }

    public Project getProject() {
        return compilerResult.getProject();
    }

    public String getObjectName() {
        return compilerResult.getObjectRef().objectName;
    }

    @Override
    public int compareTo(CompilerMessage that) {
        if (this.getType() == that.getType()) {
            return line - that.line;
        }
        return that.getType().compareTo(this.getType());
    }

    @Override
    public void disposeInner() {
        DisposeUtil.dispose(compilerResult);
        super.disposeInner();
    }
}
