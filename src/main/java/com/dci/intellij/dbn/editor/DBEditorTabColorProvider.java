package com.dci.intellij.dbn.editor;

import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.environment.options.EnvironmentSettings;
import com.dci.intellij.dbn.common.environment.options.EnvironmentVisibilitySettings;
import com.dci.intellij.dbn.common.util.Guarded;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionContextManager;
import com.dci.intellij.dbn.options.general.GeneralProjectSettings;
import com.dci.intellij.dbn.vfs.DBVirtualFileImpl;
import com.dci.intellij.dbn.vfs.file.DBConsoleVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBObjectVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBSessionBrowserVirtualFile;
import com.intellij.openapi.fileEditor.impl.EditorTabColorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

import static com.dci.intellij.dbn.common.dispose.Checks.isNotValid;
import static com.dci.intellij.dbn.common.util.Files.isDbLanguageFile;

public class DBEditorTabColorProvider implements EditorTabColorProvider, DumbAware {

    @Override
    public Color getEditorTabColor(@NotNull Project project, @NotNull VirtualFile file) {
        if (isNotValid(project)) return null;
        if (isNotValid(file)) return null;
        if (!isDbLanguageFile(file)) return null;

        return Guarded.call(null, () -> {
            ConnectionHandler connection = getConnection(file, project);
            if (isNotValid(connection)) return null;

            GeneralProjectSettings instance = GeneralProjectSettings.getInstance(project);
            EnvironmentSettings environmentSettings = instance.getEnvironmentSettings();
            EnvironmentVisibilitySettings visibilitySettings = environmentSettings.getVisibilitySettings();
            EnvironmentType environmentType = connection.getEnvironmentType();
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
        });
    }
    
    @Nullable
    public static ConnectionHandler getConnection(VirtualFile file, Project project) {
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

    private static Color getColor(ConnectionHandler connection) {
        EnvironmentType environmentType = connection.getEnvironmentType();
        return environmentType.getColor();
    }
}
