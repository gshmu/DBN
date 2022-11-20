package com.dci.intellij.dbn.common.dispose;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public final class Checks {
    private Checks() {}


    public static boolean allValid(Object ... objects) {
        for (Object object : objects) {
            if (isNotValid(object)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotValid(Object object) {
        return !isValid(object);
    }

    public static boolean isValid(Object object) {
        if (object == null) {
            return false;
        }

        if (object instanceof StatefulDisposable) {
            StatefulDisposable disposable = (StatefulDisposable) object;
            return !disposable.isDisposed();
        }

        if (object instanceof Project) {
            Project project = (Project) object;
            return project != Failsafe.DUMMY_PROJECT && !project.isDisposed();
        }

        if (object instanceof Editor) {
            Editor editor = (Editor) object;
            return !editor.isDisposed();
        }

        if (object instanceof VirtualFile) {
            VirtualFile virtualFile = (VirtualFile) object;
            return virtualFile.isValid();
        }

        return true;
    }
}
