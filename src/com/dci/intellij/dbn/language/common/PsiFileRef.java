package com.dci.intellij.dbn.language.common;


import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.thread.Read;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PsiFileRef<T extends PsiFile>{
    private WeakRef<T> psiFileRef;

    private PsiFileRef(T psiFile) {
        this.psiFileRef = WeakRef.of(psiFile);
    }

    @Nullable
    public T get() {
        return Read.call(() -> {
            T psiFile = psiFileRef.get();
            if (psiFile != null && !psiFile.isValid()) {
                Project project = psiFile.getProject();
                VirtualFile virtualFile = psiFile.getVirtualFile();

                PsiFile newPsiFile = PsiUtil.getPsiFile(project, virtualFile);
                if (newPsiFile != null &&
                        newPsiFile != psiFile &&
                        newPsiFile.isValid() &&
                        newPsiFile.getClass() == psiFile.getClass()) {

                    psiFile = (T) newPsiFile;
                    psiFileRef = WeakRef.of(psiFile);
                } else {
                    psiFile = null;
                }
            }
            return psiFile;
        });
    }

    public static <T extends PsiFile> PsiFileRef<T> from(@NotNull T psiFile) {
        return new PsiFileRef<>(psiFile);
    }

    @NotNull
    public T ensure() {
        return Failsafe.nn(get());
    }
}
