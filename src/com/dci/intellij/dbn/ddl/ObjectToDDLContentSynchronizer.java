package com.dci.intellij.dbn.ddl;

import java.util.List;

import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class ObjectToDDLContentSynchronizer implements Runnable {
    DBEditableObjectVirtualFile databaseFile;

    public ObjectToDDLContentSynchronizer(DBEditableObjectVirtualFile databaseFile) {
        this.databaseFile = databaseFile;
    }

    public void run() {

        Project project = databaseFile.getProject();
        if (project != null) {
            DDLFileManager ddlFileManager = DDLFileManager.getInstance(project);
            List<VirtualFile> ddlFiles = databaseFile.getAttachedDDLFiles();

            if (ddlFiles != null && !ddlFiles.isEmpty()) {
                for (VirtualFile ddlFile : ddlFiles) {
                    DDLFileType ddlFileType = ddlFileManager.getDDLFileTypeForExtension(ddlFile.getExtension());
                    DBContentType fileContentType = ddlFileType.getContentType();

                    StringBuilder buffer = new StringBuilder();
                    if (fileContentType.isBundle()) {
                        DBContentType[] contentTypes = fileContentType.getSubContentTypes();
                        for (DBContentType contentType : contentTypes) {
                            DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) databaseFile.getContentFile(contentType);
                            if (sourceCodeFile != null) {
                                String statement = ddlFileManager.createDDLStatement(sourceCodeFile, contentType);
                                if (statement.trim().length() > 0) {
                                    buffer.append(statement);
                                    buffer.append('\n');
                                }
                                if (contentType != contentTypes[contentTypes.length - 1]) buffer.append('\n');
                            }
                        }
                    } else {
                        DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) databaseFile.getContentFile(fileContentType);
                        if (sourceCodeFile != null) {
                            buffer.append(ddlFileManager.createDDLStatement(sourceCodeFile, fileContentType));
                            buffer.append('\n');
                        }
                    }
                    Document document = DocumentUtil.getDocument(ddlFile);
                    if (document != null) {
                        document.setText(buffer.toString());
                    }
                }
            }
        }
    }
}

