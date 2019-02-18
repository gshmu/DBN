package com.dci.intellij.dbn.code.common.style.presets;

import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CodeStylePresetImpl implements CodeStylePreset {
    private String id;
    private String name;

    protected CodeStylePresetImpl(String id, String name) {
        this.id = id;
        this.name = name;
        //CodeStylePresetsRegister.registerWrapPreset(this);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    @NotNull
    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }

    @Nullable
    protected static BasePsiElement getParentPsiElement(@NotNull PsiElement psiElement) {
        PsiElement parentPsiElement = psiElement.getParent();
        if (parentPsiElement instanceof BasePsiElement) {
            return (BasePsiElement) parentPsiElement;
        }
        return null;
    }

    protected static ElementType getParentElementType(PsiElement psiElement) {
        BasePsiElement parentPsiElement = getParentPsiElement(psiElement);
        if (parentPsiElement != null) {
            return parentPsiElement.getElementType();
        }
        return null;
    }
}
