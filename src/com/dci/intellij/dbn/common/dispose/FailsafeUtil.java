package com.dci.intellij.dbn.common.dispose;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.mock.MockProject;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;

public class FailsafeUtil {
    private static final VirtualFile DUMMY_VIRTUAL_FILE = new LightVirtualFile();
    private static final Project DUMMY_PROJECT = new MockProject(ApplicationManager.getApplication().getPicoContainer(), ApplicationManager.getApplication());



    public static @NotNull Project get(@Nullable Project project) {
        return project == null ? DUMMY_PROJECT : project;
    }

    public static @NotNull VirtualFile get(@Nullable VirtualFile virtualFile) {
        return virtualFile == null ? DUMMY_VIRTUAL_FILE : virtualFile;
    }

}
