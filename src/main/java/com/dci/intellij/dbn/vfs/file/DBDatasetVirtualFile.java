package com.dci.intellij.dbn.vfs.file;

import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.DBDataset;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class DBDatasetVirtualFile extends DBContentVirtualFile {
    DBDatasetVirtualFile(DBEditableObjectVirtualFile databaseFile, DBContentType contentType) {
        super(databaseFile, contentType);
    }

    @Override
    @NotNull
    public DBDataset getObject() {
        return (DBDataset) super.getObject();
    }

    /*********************************************************
     *                     VirtualFile                       *
     *********************************************************/

    @Override
    @NotNull
    public byte[] contentsToByteArray() throws IOException {
        return new byte[0];
    }

    @Override
    public long getLength() {
        return 0;
    }

    @Override
    public DatabaseSession getSession() {
        return this.getConnection().getSessionBundle().getMainSession();
    }
}
