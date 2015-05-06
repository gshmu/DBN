package com.dci.intellij.dbn.execution.logging;

import java.util.Date;

import com.dci.intellij.dbn.common.locale.Formatter;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;

public class LogOutput {
    public static enum Type {
        SYS(ProcessOutputTypes.SYSTEM),
        STD(ProcessOutputTypes.STDOUT),
        ERR(ProcessOutputTypes.STDERR);
        private Key key;

        Type(Key key) {
            this.key = key;
        }

        public Key getKey() {
            return key;
        }
    }

    private String text;
    private Type type;
    private boolean scrollToEnd;

    private LogOutput(String text, Type type) {
        this(text, type, false);
    }
    private LogOutput(String text, Type type, boolean scrollToEnd) {
        this.text = text;
        this.type = type;
        this.scrollToEnd = scrollToEnd;
    }

    public String getText() {
        return text;
    }

    public Type getType() {
        return type;
    }

    public boolean isScrollToEnd() {
        return scrollToEnd;
    }

    public static LogOutput createErrOutput(String text) {
        return new LogOutput(text, Type.ERR);
    }

    public static LogOutput createStdOutput(String text) {
        return new LogOutput(text, Type.STD);
    }

    public static LogOutput createSysOutput(LogOutputContext context, String message) {
        ConnectionHandler connectionHandler = context.getConnectionHandler();
        Project project = connectionHandler.getProject();
        Formatter formatter = Formatter.getInstance(project);
        String date = formatter.formatDateTime(new Date());
        String text = date + ": " + connectionHandler.getName();
        VirtualFile sourceFile = context.getSourceFile();
        if (sourceFile != null) {
            text += " / " + sourceFile.getName();
        }
        text += message;

        return new LogOutput(text, Type.SYS, true);
    }

}
