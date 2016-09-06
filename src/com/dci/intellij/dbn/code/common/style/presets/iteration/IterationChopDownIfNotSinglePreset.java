package com.dci.intellij.dbn.code.common.style.presets.iteration;

import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.IterationElementType;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.Wrap;
import com.intellij.psi.codeStyle.CodeStyleSettings;

public class IterationChopDownIfNotSinglePreset extends IterationAbstractPreset {
    public IterationChopDownIfNotSinglePreset() {
        super("chop_down_if_not_single", "Chop down unless single element");
    }

    @Nullable
    public Wrap getWrap(BasePsiElement psiElement, CodeStyleSettings settings) {
        BasePsiElement parentPsiElement = getParentPsiElement(psiElement);
        if (parentPsiElement != null) {
            IterationElementType iterationElementType = (IterationElementType) parentPsiElement.getElementType();
            ElementType elementType = psiElement.getElementType();

            boolean shouldWrap = PsiUtil.getChildCount(parentPsiElement) > 1;
            return getWrap(elementType, iterationElementType, shouldWrap);
        }

        return null;
    }

    @Nullable
    public Spacing getSpacing(BasePsiElement psiElement, CodeStyleSettings settings) {
        BasePsiElement parentPsiElement = getParentPsiElement(psiElement);
        if (parentPsiElement != null) {
            IterationElementType iterationElementType = (IterationElementType) parentPsiElement.getElementType();
            ElementType elementType = psiElement.getElementType();

            boolean shouldWrap = PsiUtil.getChildCount(parentPsiElement) > 1;
            return getSpacing(iterationElementType, elementType, shouldWrap);
        }
        return null;
    }
}