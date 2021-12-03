package com.dci.intellij.dbn.common.dispose;

import com.dci.intellij.dbn.common.routine.ParametricRunnable;
import com.intellij.mock.MockProject;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Failsafe {
    private static final VirtualFile DUMMY_VIRTUAL_FILE = new LightVirtualFile();
    public static final Project DUMMY_PROJECT = new MockProject(null, ApplicationManager.getApplication());

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
            
        } else if (object instanceof StatefulDisposable) {
            StatefulDisposable disposable = (StatefulDisposable) object;
            return !disposable.isDisposed();

        } else if (object instanceof Project) {
            Project project = (Project) object;
            return !project.isDisposed();

        }
        return true;
    }

    public static <T, E extends Throwable> void invoke(@Nullable T target, ParametricRunnable<T, E> invoker) throws E {
        if (check(target)) {
            invoker.run(target);
        }
    }

}
