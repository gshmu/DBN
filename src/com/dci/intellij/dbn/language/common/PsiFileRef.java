package com.dci.intellij.dbn.language.common;


import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public class PsiFileRef<T extends PsiFile>{
    private WeakReference<T> psiFileRef;

    public PsiFileRef(T psiFile) {
        this.psiFileRef = new WeakReference<>(psiFile);
    }

    @Nullable
    public T get() {
        T psiFile = psiFileRef.get();
        if (psiFile != null && !psiFile.isValid()) {
            Project project = psiFile.getProject();
            VirtualFile virtualFile = psiFile.getVirtualFile();

            psiFile = (T) PsiUtil.getPsiFile(project, virtualFile);
            if (psiFile != null && psiFile.isValid()) {
                psiFileRef = new WeakReference<T>(psiFile);
            } else {
                psiFile = null;
            }
        }
        return psiFile;
    }

    @NotNull
    public T getnn() {
        return FailsafeUtil.get(get());
    }
}
