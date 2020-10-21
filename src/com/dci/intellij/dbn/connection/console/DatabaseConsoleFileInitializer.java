package com.dci.intellij.dbn.connection.console;

import com.dci.intellij.dbn.common.thread.Write;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.editor.code.content.GuardedBlockMarkers;
import com.dci.intellij.dbn.editor.code.content.GuardedBlockType;
import com.dci.intellij.dbn.vfs.file.DBConsoleVirtualFile;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class DatabaseConsoleFileInitializer implements FileDocumentManagerListener {
    @Override
    public void fileContentLoaded(@NotNull VirtualFile file, @NotNull Document document) {
        if (file instanceof DBConsoleVirtualFile) {
            // restore guarded blocks after console file loaded
            DBConsoleVirtualFile consoleFile = (DBConsoleVirtualFile) file;
            Write.run(() -> {
                GuardedBlockMarkers guardedBlocks = consoleFile.getContent().getOffsets().getGuardedBlocks();
                if (!guardedBlocks.isEmpty()) {
                    DocumentUtil.removeGuardedBlocks(document, GuardedBlockType.READONLY_DOCUMENT_SECTION);
                    DocumentUtil.createGuardedBlocks(document, GuardedBlockType.READONLY_DOCUMENT_SECTION, guardedBlocks, null);
                }
            });
        }
    }
}
