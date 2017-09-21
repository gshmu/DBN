package com.dci.intellij.dbn.language.common.psi;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.object.common.DBObjectPsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;

public class PsiElementRenameProcessor extends RenamePsiElementProcessor {

    @Override
    public boolean canProcessElement(@NotNull PsiElement element) {
        return element instanceof BasePsiElement || element instanceof DBObjectPsiElement;
    }


}
