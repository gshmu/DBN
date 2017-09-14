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
    public static final Project DUMMY_PROJECT = new MockProject(ApplicationManager.getApplication().getPicoContainer(), ApplicationManager.getApplication());

    public static @NotNull <T extends Disposable> T get(@Nullable T disposable) {
        if (disposable == null || disposable.isDisposed()) {
            throw AlreadyDisposedException.INSTANCE;
        }
        return disposable;
    }

    public static @NotNull <T> T get(@Nullable T object) {
        if (object == null) {
            throw AlreadyDisposedException.INSTANCE;
        }
        return object;
    }

    public static @NotNull Project get(@Nullable Project project) {
        if (project == null) {
            throw AlreadyDisposedException.INSTANCE;
        }
        return project;
    }

    public static <T> T getComponent(@Nullable Project project, @NotNull Class<T> interfaceClass) {
        project = get(project);
        T component = project.getComponent(interfaceClass);
        return get(component);
    }



    public static @NotNull VirtualFile nvl(@Nullable VirtualFile virtualFile) {
        return virtualFile == null ? DUMMY_VIRTUAL_FILE : virtualFile;
    }

    public static void check(Object object) {
        if (!softCheck(object)) {
            throw AlreadyDisposedException.INSTANCE;
        }
    }

    public static boolean softCheck(Object object) {
        if (object == null) {
            return false;
        } else if (object instanceof Disposable) {
            Disposable disposable = (Disposable) object;
            if (disposable.isDisposed()) {
                return false;
            }
        } else if (object instanceof Project) {
            Project project = (Project) object;
            if (project.isDisposed()) {
                return false;
            }
        }
        return true;
    }
}
