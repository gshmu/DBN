package com.dci.intellij.dbn.language.common;


import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

public class PsiElementRef<T extends PsiElement>{
    private WeakReference<T> psiElementRef;

    public PsiElementRef(T psiElement) {
        this.psiElementRef = new WeakReference<>(psiElement);
    }

    public T get() {
        return psiElementRef.get();
    }

    @NotNull
    public T getnn() {
        return FailsafeUtil.get(get());
    }
}
