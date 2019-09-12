package com.dci.intellij.dbn.object;

import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.vfs.DBConsoleType;
import com.dci.intellij.dbn.vfs.file.DBConsoleVirtualFile;
import org.jetbrains.annotations.NotNull;

public interface DBConsole extends DBObject {
    void setName(String newName);

    @NotNull
    @Override
    DBConsoleVirtualFile getVirtualFile();

    DBConsoleType getConsoleType();
}
