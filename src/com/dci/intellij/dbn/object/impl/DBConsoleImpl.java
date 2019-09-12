package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadata;
import com.dci.intellij.dbn.object.DBConsole;
import com.dci.intellij.dbn.object.common.DBObjectImpl;
import com.dci.intellij.dbn.object.common.DBObjectPsiFacade;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.dci.intellij.dbn.vfs.DBConsoleType;
import com.dci.intellij.dbn.vfs.file.DBConsoleVirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.sql.SQLException;

public class DBConsoleImpl extends DBObjectImpl<DBObjectMetadata> implements DBConsole {
    private DBConsoleVirtualFile virtualFile;
    private final DBConsoleType consoleType;
    private DBObjectPsiFacade psiFacade = new DBObjectPsiFacade();

    public DBConsoleImpl(@NotNull ConnectionHandler connectionHandler, String name, DBConsoleType consoleType) {
        super(connectionHandler, DBObjectType.CONSOLE, name);
        virtualFile = new DBConsoleVirtualFile(this);
        this.consoleType = consoleType;
    }

    @NotNull
    @Override
    public DBObjectType getObjectType() {
        return DBObjectType.CONSOLE;
    }

    @Override
    protected String initObject(DBObjectMetadata metadata) throws SQLException {
        return null;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        switch (consoleType) {
            case STANDARD: return Icons.DBO_CONSOLE;
            case DEBUG: return Icons.DBO_CONSOLE_DEBUG;
        }
        return super.getIcon();
    }

    @NotNull
    @Override
    public DBObjectPsiFacade getPsiFacade() {
        return super.getPsiFacade();
    }

    @NotNull
    @Override
    public DBConsoleVirtualFile getVirtualFile() {
        return virtualFile;
    }

    @Override
    public DBConsoleType getConsoleType() {
        return consoleType;
    }

    @Override
    public void setName(String newName) {
        getRef().setObjectName(newName);
    }
}
