package com.dci.intellij.dbn.common.dispose;

import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;
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

    public static @NotNull <T> T nn(@Nullable T object) {
        if (object == null) {
            throw AlreadyDisposedException.INSTANCE;
        }
        return object;
    }

    public static <T> T getComponent(@NotNull Project project, @NotNull Class<T> interfaceClass) {
        project = nd(project);
        T component = project.getComponent(interfaceClass);
        return nn(component);
    }



    public static @NotNull VirtualFile nvl(@Nullable VirtualFile virtualFile) {
        return virtualFile == null ? DUMMY_VIRTUAL_FILE : virtualFile;
    }

    @NotNull
    public static <T> T nd(@Nullable T object) {
        if (!check(object)) {
            throw AlreadyDisposedException.INSTANCE;
        }
        return object;
    }

    public static boolean check(Object ... objects) {
        for (Object object : objects) {
            if (!check(object)) {
                return false;
            }
        }
        return true;
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

    public static <T, E extends Throwable> T guarded(T defaultValue, ThrowableCallable<T, E> callable) throws E{
        try {
            return callable.call();
        } catch (ProcessCanceledException e) {
            return defaultValue;
        }
    }

    public static <E extends Throwable> void guarded(ThrowableRunnable<E> runnable) throws E {
        try {
            runnable.run();
        } catch (ProcessCanceledException ignore) {}
    }

    public static void guarded(Runnable runnable, Runnable cancel){
        try {
            runnable.run();
        } catch (ProcessCanceledException ignore) {
            cancel.run();
        }
    }
}
