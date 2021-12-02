package com.dci.intellij.dbn.language.common;


import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PsiElementRef<T extends PsiElement> extends WeakRef<T>{
    private PsiElementRef(T psiElement) {
        super(psiElement);
    }

    @Contract("null -> null;!null -> !null;")
    public static <T extends PsiElement> PsiElementRef<T> from(@Nullable T psiElement) {
        return psiElement == null ? null : new PsiElementRef<T>(psiElement);
    }

    @Nullable
    @Override
    public T get() {
        return super.get();
    }

    @Override
    @NotNull
    public T ensure() {
        return Failsafe.nn(get());
    }
}
