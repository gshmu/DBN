package com.dci.intellij.dbn.editor;

import java.awt.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.environment.options.EnvironmentSettings;
import com.dci.intellij.dbn.common.environment.options.EnvironmentVisibilitySettings;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.language.common.DBLanguageFileType;
import com.dci.intellij.dbn.options.general.GeneralProjectSettings;
import com.dci.intellij.dbn.vfs.DBConsoleVirtualFile;
import com.dci.intellij.dbn.vfs.DBObjectVirtualFile;
import com.dci.intellij.dbn.vfs.DBSessionBrowserVirtualFile;
import com.dci.intellij.dbn.vfs.DBVirtualFileImpl;
import com.intellij.openapi.fileEditor.impl.EditorTabColorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class DBEditorTabColorProvider implements EditorTabColorProvider, DumbAware {

    @Override
    public Color getEditorTabColor(@NotNull Project project, @NotNull VirtualFile file) {
        if (file.getFileType() instanceof DBLanguageFileType) {
            ConnectionHandler connectionHandler = getConnectionHandler(file, project);
            if (connectionHandler == null) {
                return null;
            } else {
                EnvironmentSettings environmentSettings = GeneralProjectSettings.getInstance(connectionHandler.getProject()).getEnvironmentSettings();
                EnvironmentVisibilitySettings visibilitySettings = environmentSettings.getVisibilitySettings();
                EnvironmentType environmentType = connectionHandler.getEnvironmentType();
                if (file instanceof DBVirtualFileImpl) {
                    if (visibilitySettings.getObjectEditorTabs().value()) {
                        return environmentType.getColor();
                    }
                } else {
                    if (visibilitySettings.getScriptEditorTabs().value()) {
                        return environmentType.getColor();
                    }
                }
                return null;
            }
        }
        return null;
    }
    
    @Nullable
    public static ConnectionHandler getConnectionHandler(VirtualFile file, Project project) {
        if (file instanceof DBConsoleVirtualFile) {
            DBConsoleVirtualFile consoleFile = (DBConsoleVirtualFile) file;
            return consoleFile.getConnectionHandler();
        }

        if (file instanceof DBSessionBrowserVirtualFile) {
            DBSessionBrowserVirtualFile sessionBrowserFile = (DBSessionBrowserVirtualFile) file;
            return sessionBrowserFile.getConnectionHandler();
        }
        
        if (file instanceof DBObjectVirtualFile) {
            DBObjectVirtualFile objectFile = (DBObjectVirtualFile) file;
            return objectFile.getConnectionHandler();
        }

        return FileConnectionMappingManager.getInstance(project).getConnectionHandler(file);
    }

    private static Color getColor(ConnectionHandler connectionHandler) {
        EnvironmentType environmentType = connectionHandler.getEnvironmentType();
        return environmentType.getColor();
    }
}
