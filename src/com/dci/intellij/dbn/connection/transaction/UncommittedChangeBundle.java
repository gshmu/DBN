package com.dci.intellij.dbn.connection.transaction;


import javax.swing.Icon;
import java.util.ArrayList;
import java.util.List;

import com.dci.intellij.dbn.vfs.DatabaseObjectVirtualFile;
import com.dci.intellij.dbn.vfs.SQLConsoleVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;

public class UncommittedChangeBundle {
    private List<UncommittedChange> changes = new ArrayList<UncommittedChange>();

    public void notifyChange(VirtualFile file){
        Icon icon = file.getFileType().getIcon();
        if (file instanceof DatabaseObjectVirtualFile) {
            DatabaseObjectVirtualFile databaseObjectFile = (DatabaseObjectVirtualFile) file;
            icon = databaseObjectFile.getIcon();
        }
        if (file instanceof SQLConsoleVirtualFile) {
            SQLConsoleVirtualFile sqlConsoleFile = (SQLConsoleVirtualFile) file;
            icon = sqlConsoleFile.getIcon();
        }

        String url = file.getUrl();

        UncommittedChange change = getUncommittedChange(file);
        if (change == null) {
            change = new UncommittedChange(url, file.getPresentableUrl(), icon);
            changes.add(change);
        }
        change.incrementChangesCount();
    }

    public UncommittedChange getUncommittedChange(VirtualFile file) {
        for (UncommittedChange change : changes) {
            if (change.getFilePath().equals(file.getUrl())) {
                return change;
            }
        }
        return null;
    }

    public List<UncommittedChange> getChanges() {
        return changes;
    }

    public int size() {
        return changes.size();
    }

    public boolean isEmpty() {
        return changes.isEmpty();
    }
}
