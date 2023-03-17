package com.dci.intellij.dbn.language.common.psi;

import com.dci.intellij.dbn.common.ref.WeakRef;
import com.intellij.psi.PsiElement;
import lombok.experimental.Delegate;

public class WeakPsiDelegate implements PsiElement{
    private final WeakRef<PsiElement> delegate;

    private WeakPsiDelegate(PsiElement delegate) {
        this.delegate = WeakRef.of(delegate);
    }

    @Delegate
    public PsiElement getDelegate() {
        PsiElement psiElement = WeakRef.get(delegate);
        return psiElement;
    }

    public static PsiElement wrap(PsiElement psiElement) {
        return new WeakPsiDelegate(psiElement);
    }
}
