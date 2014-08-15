package com.dci.intellij.dbn.ddl;

import java.util.List;

import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.ddl.options.DDLFileSettings;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.vfs.DatabaseEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.SourceCodeVirtualFile;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.vfs.VirtualFile;

public class ObjectToDDLContentSynchronizer implements Runnable {
    DatabaseEditableObjectVirtualFile databaseFile;
    private DBContentType sourceContentType;

    public ObjectToDDLContentSynchronizer(DBContentType sourceContentType, DatabaseEditableObjectVirtualFile databaseFile) {
        this.sourceContentType = sourceContentType;
        this.databaseFile = databaseFile;
    }

    public void run() {
        assert !sourceContentType.isBundle();
        DDLFileManager ddlFileManager = DDLFileManager.getInstance(databaseFile.getProject());
        List<VirtualFile> ddlFiles = databaseFile.getAttachedDDLFiles();
        DDLFileSettings ddlFileSettings = DDLFileSettings.getInstance(databaseFile.getProject());
        String postfix = ddlFileSettings.getGeneralSettings().getStatementPostfix().value();

        if (ddlFiles != null && !ddlFiles.isEmpty()) {
            for (VirtualFile ddlFile : ddlFiles) {
                DDLFileType ddlFileType = ddlFileManager.getDDLFileTypeForExtension(ddlFile.getExtension());
                DBContentType fileContentType = ddlFileType.getContentType();

                StringBuilder buffer = new StringBuilder();
                if (fileContentType.isBundle()) {
                    DBContentType[] contentTypes = fileContentType.getSubContentTypes();
                    for (DBContentType contentType : contentTypes) {
                        SourceCodeVirtualFile virtualFile = (SourceCodeVirtualFile) databaseFile.getContentFile(contentType);
                        String statement = virtualFile.createDDLStatement();
                        if (statement.trim().length() > 0) {
                            buffer.append(statement);
                            if (postfix.length() > 0) {
                                buffer.append("\n");
                                buffer.append(postfix);
                            }
                            buffer.append("\n");
                        }
                        if (contentType != contentTypes[contentTypes.length - 1]) buffer.append("\n");
                    }
                } else {
                    SourceCodeVirtualFile virtualFile = (SourceCodeVirtualFile) databaseFile.getContentFile(fileContentType);
                    buffer.append(virtualFile.createDDLStatement());
                    if (postfix.length() > 0) {
                        buffer.append("\n");
                        buffer.append(postfix);
                    }
                    buffer.append("\n");
                }
                Document document = DocumentUtil.getDocument(ddlFile);
                document.setText(buffer.toString());
            }
        }
    }
}

