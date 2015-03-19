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

    public static @NotNull <T extends Disposable> T get(@Nullable T disposable) {
        if (disposable == null || disposable.isDisposed()) {
            throw AlreadyDisposedException.INSTANCE;
        }
        return disposable;
    }

    public static @NotNull Project get(@Nullable Project project) {
        if (project == null || project.isDisposed()) {
            throw AlreadyDisposedException.INSTANCE;
        }
        return project;
    }

    public static @NotNull VirtualFile nvl(@Nullable VirtualFile virtualFile) {
        return virtualFile == null ? DUMMY_VIRTUAL_FILE : virtualFile;
    }

}
