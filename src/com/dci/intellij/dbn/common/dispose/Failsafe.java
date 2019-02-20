package com.dci.intellij.dbn.common.dispose;

import com.dci.intellij.dbn.common.thread.BasicCallable;
import com.dci.intellij.dbn.common.thread.BasicRunnable;
import com.intellij.mock.MockProject;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Failsafe {
    private static final VirtualFile DUMMY_VIRTUAL_FILE = new LightVirtualFile();
    public static final Project DUMMY_PROJECT = new MockProject(ApplicationManager.getApplication().getPicoContainer(), ApplicationManager.getApplication());

    public static @NotNull <T extends Disposable> T get(@Nullable T disposable) {
        if (disposable == null) {
            throw AlreadyDisposedException.INSTANCE;
        } else if (disposable.isDisposed()) {
            if (ApplicationManager.getApplication().isDispatchThread()) {
                return disposable;
            }
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

    public static <T> T getComponent(@NotNull Project project, @NotNull Class<T> interfaceClass) {
        project = ensure(project);
        T component = project.getComponent(interfaceClass);
        return get(component);
    }



    public static @NotNull VirtualFile nvl(@Nullable VirtualFile virtualFile) {
        return virtualFile == null ? DUMMY_VIRTUAL_FILE : virtualFile;
    }

    @NotNull
    public static <T> T ensure(T object) {
        if (!check(object)) {
            throw AlreadyDisposedException.INSTANCE;
        }
        return object;
    }

    public static boolean check(Object object) {
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

    public static <T> T lenient(T defaultValue, BasicCallable<T, RuntimeException> callable) {
        try {
            return callable.call();
        } catch (ProcessCanceledException e) {
            return defaultValue;
        }
    }

    public static void lenient(BasicRunnable<RuntimeException> runnable) {
        try {
            runnable.run();
        } catch (ProcessCanceledException ignore) {}
    }

    public static void lenient(BasicRunnable<RuntimeException> runnable, BasicRunnable<RuntimeException> cancel) {
        try {
            runnable.run();
        } catch (ProcessCanceledException ignore) {
            cancel.run();
        }
    }

    public static void run (@Nullable Runnable runnable) {
        try {
            if (runnable != null) runnable.run();
        } catch (ProcessCanceledException ignore) {}
    }
}
