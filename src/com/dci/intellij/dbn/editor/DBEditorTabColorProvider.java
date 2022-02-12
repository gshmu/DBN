package com.dci.intellij.dbn.editor;

import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.environment.options.EnvironmentSettings;
import com.dci.intellij.dbn.common.environment.options.EnvironmentVisibilitySettings;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionContextManager;
import com.dci.intellij.dbn.language.common.DBLanguageFileType;
import com.dci.intellij.dbn.options.general.GeneralProjectSettings;
import com.dci.intellij.dbn.vfs.DBVirtualFileImpl;
import com.dci.intellij.dbn.vfs.file.DBConsoleVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBObjectVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBSessionBrowserVirtualFile;
import com.intellij.openapi.fileEditor.impl.EditorTabColorProvider;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class DBEditorTabColorProvider implements EditorTabColorProvider, DumbAware {

    @Override
    public Color getEditorTabColor(@NotNull Project project, @NotNull VirtualFile file) {
        if (file.getFileType() instanceof DBLanguageFileType) {
            try {
                ConnectionHandler connectionHandler = getConnectionHandler(file, project);
                if (connectionHandler == null) {
                    return null;
                } else {
                    GeneralProjectSettings instance = GeneralProjectSettings.getInstance(project);
                    EnvironmentSettings environmentSettings = instance.getEnvironmentSettings();
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
            } catch (ProcessCanceledException ignore) {}
        }
        return null;
    }
    
    @Nullable
    public static ConnectionHandler getConnectionHandler(VirtualFile file, Project project) {
        if (file instanceof DBConsoleVirtualFile) {
            DBConsoleVirtualFile consoleFile = (DBConsoleVirtualFile) file;
            return consoleFile.getConnection();
        }

        if (file instanceof DBSessionBrowserVirtualFile) {
            DBSessionBrowserVirtualFile sessionBrowserFile = (DBSessionBrowserVirtualFile) file;
            return sessionBrowserFile.getConnection();
        }
        
        if (file instanceof DBObjectVirtualFile) {
            DBObjectVirtualFile objectFile = (DBObjectVirtualFile) file;
            return objectFile.getConnection();
        }

        return FileConnectionContextManager.getInstance(project).getConnection(file);
    }

    private static Color getColor(ConnectionHandler connectionHandler) {
        EnvironmentType environmentType = connectionHandler.getEnvironmentType();
        return environmentType.getColor();
    }
}
