package com.dci.intellij.dbn.common.dispose;

import com.dci.intellij.dbn.common.routine.ParametricRunnable;
import com.intellij.mock.MockProject;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dci.intellij.dbn.common.dispose.Checks.isNotValid;
import static com.dci.intellij.dbn.common.dispose.Checks.isValid;

public class Failsafe {
    private static final VirtualFile DUMMY_VIRTUAL_FILE = new LightVirtualFile();
    public static final Project DUMMY_PROJECT = new MockProject(null, ApplicationManager.getApplication());

    public static @NotNull <T> T nn(@Nullable T object) {
        if (object == null) {
            throw AlreadyDisposedException.INSTANCE;
        }
        return object;
    }

    @NotNull
    public static <T> T nd(@Nullable T object) {
        if (isNotValid(object)) {
            throw AlreadyDisposedException.INSTANCE;
        }
        return object;
    }

    public static <T, E extends Throwable> void invoke(@Nullable T target, ParametricRunnable<T, E> invoker) throws E {
        if (isValid(target)) {
            invoker.run(target);
        }
    }

}
