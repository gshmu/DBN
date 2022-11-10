package com.dci.intellij.dbn.ddl;

import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;

public class DDLFileNameProvider {
    private DBObjectRef objectRef;
    private DDLFileType ddlFileType;
    private String extension;

    public DDLFileNameProvider(DBObjectRef objectRef, DDLFileType ddlFileType, String extension) {
        this.objectRef = objectRef;
        this.ddlFileType = ddlFileType;
        this.extension = extension;
    }

    public DBObject getObject() {
        return objectRef.get();
    }

    public DDLFileType getDdlFileType() {
        return ddlFileType;
    }

    public String getExtension() {
        return extension;
    }

    public String getFileName() {
        return objectRef.getFileName().toLowerCase() + '.' + extension;
    }
}
