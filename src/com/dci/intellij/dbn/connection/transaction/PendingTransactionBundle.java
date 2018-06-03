package com.dci.intellij.dbn.connection.transaction;


import com.dci.intellij.dbn.vfs.file.DBConsoleVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBObjectVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class PendingTransactionBundle {
    private List<PendingTransaction> entries = new ArrayList<PendingTransaction>();

    public void notifyChange(VirtualFile file){
        Icon icon = file.getFileType().getIcon();
        if (file instanceof DBObjectVirtualFile) {
            DBObjectVirtualFile databaseObjectFile = (DBObjectVirtualFile) file;
            icon = databaseObjectFile.getIcon();
        }
        if (file instanceof DBConsoleVirtualFile) {
            DBConsoleVirtualFile sqlConsoleFile = (DBConsoleVirtualFile) file;
            icon = sqlConsoleFile.getIcon();
        }

        String url = file.getUrl();

        PendingTransaction change = getPendingTransaction(file);
        if (change == null) {
            change = new PendingTransaction(url, file.getPresentableUrl(), icon);
            entries.add(change);
        }
        change.incrementChangesCount();
    }

    public PendingTransaction getPendingTransaction(VirtualFile file) {
        for (PendingTransaction change : entries) {
            if (change.getFilePath().equals(file.getUrl())) {
                return change;
            }
        }
        return null;
    }

    public List<PendingTransaction> getEntries() {
        return entries;
    }

    public int size() {
        return entries.size();
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }
}
