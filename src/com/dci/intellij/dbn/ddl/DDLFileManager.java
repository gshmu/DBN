package com.dci.intellij.dbn.ddl;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.thread.WriteActionRunner;
import com.dci.intellij.dbn.ddl.options.DDLFileExtensionSettings;
import com.dci.intellij.dbn.ddl.options.DDLFileSettings;
import com.dci.intellij.dbn.language.common.DBLanguageFileType;
import com.intellij.openapi.fileTypes.ExtensionFileNameMatcher;
import com.intellij.openapi.fileTypes.FileNameMatcher;
import com.intellij.openapi.fileTypes.FileTypeEvent;
import com.intellij.openapi.fileTypes.FileTypeListener;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DDLFileManager extends AbstractProjectComponent implements JDOMExternalizable, FileTypeListener {
    private DDLFileManager(Project project) {
        super(project);
    }

   public void registerExtensions() {
       new WriteActionRunner() {
           public void run() {
               if (!isDisposed()) {
                   FileTypeManager.getInstance().removeFileTypeListener(DDLFileManager.this);
                   FileTypeManager fileTypeManager = FileTypeManager.getInstance();
                   List<DDLFileType> ddlFileTypeList = getExtensionSettings().getDDLFileTypes();
                   for (DDLFileType ddlFileType : ddlFileTypeList) {
                       for (String extension : ddlFileType.getExtensions()) {
                           fileTypeManager.associateExtension(ddlFileType.getLanguageFileType(), extension);
                       }
                   }
                   FileTypeManager.getInstance().addFileTypeListener(DDLFileManager.this);
               }
           }
       }.start();
    }

    public static DDLFileManager getInstance(Project project) {
        return project.getComponent(DDLFileManager.class);
    }

    public DDLFileExtensionSettings getExtensionSettings() {
        return DDLFileSettings.getInstance(getProject()).getExtensionSettings();
    }

    public DDLFileType getDDLFileType(String ddlFileTypeId) {
        return getExtensionSettings().getDDLFileType(ddlFileTypeId);
    }

    public DDLFileType getDDLFileTypeForExtension(String extension) {
        return getExtensionSettings().getDDLFileTypeForExtension(extension);
    }

    /***************************************
     *            FileTypeListener         *
     ***************************************/

    public void beforeFileTypesChanged(FileTypeEvent event) {

    }

    public void fileTypesChanged(FileTypeEvent event) {
        StringBuilder restoredAssociations = null;
        FileTypeManager fileTypeManager = FileTypeManager.getInstance();
        List<DDLFileType> ddlFileTypeList = getExtensionSettings().getDDLFileTypes();
        for (DDLFileType ddlFileType : ddlFileTypeList) {
            DBLanguageFileType fileType = ddlFileType.getLanguageFileType();
            List<FileNameMatcher> associations = fileTypeManager.getAssociations(fileType);
            List<String> registeredExtension = new ArrayList<String>();
            for (FileNameMatcher association : associations) {
                if (association instanceof ExtensionFileNameMatcher) {
                    ExtensionFileNameMatcher extensionMatcher = (ExtensionFileNameMatcher) association;
                    registeredExtension.add(extensionMatcher.getExtension());
                }
            }

            for (String extension : ddlFileType.getExtensions()) {
                if (!registeredExtension.contains(extension)) {
                    fileTypeManager.associateExtension(fileType, extension);
                    if (restoredAssociations == null) {
                        restoredAssociations = new StringBuilder();
                    } else {
                        restoredAssociations.append(", ");
                    }
                    restoredAssociations.append(extension);

                }
            }
        }
        if (restoredAssociations != null) {
            String message =
                    "Following file associations have been restored: \"" + restoredAssociations.toString() + "\". " +
                            "They are registered as DDL file types in project \"" + getProject().getName() + "\".\n" +
                            "Please remove them from project DDL configuration first (Project Settings > DB Navigator > DDL File Settings).";
            Messages.showWarningDialog(getProject(), message, Constants.DBN_TITLE_PREFIX + "Restored file extensions");
        }
    }

    /***************************************
     *            ProjectComponent         *
     ***************************************/
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.DDLFileManager";
    }

    public void projectOpened() {
        new SimpleLaterInvocator() {
            public void execute() {
                registerExtensions();
            }
        }.start();
    }

    public void projectClosed() {
        FileTypeManager.getInstance().removeFileTypeListener(this);
    }

    /****************************************
    *            JDOMExternalizable         *
    *****************************************/
    public void readExternal(Element element) throws InvalidDataException {
    }

    public void writeExternal(Element element) throws WriteExternalException {

    }

}
