package com.dci.intellij.dbn.code.common.style.presets.iteration;

import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.IterationElementType;
import com.dci.intellij.dbn.language.common.element.TokenElementType;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.Wrap;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import org.jetbrains.annotations.Nullable;

public class IterationNoWrappingPreset extends IterationAbstractPreset {
    public IterationNoWrappingPreset() {
        super("no_wrapping", "No wrapping");
    }

    @Override
    @Nullable
    public Wrap getWrap(BasePsiElement psiElement, CodeStyleSettings settings) {
        return WRAP_NONE;
    }

    @Override
    @Nullable
    public Spacing getSpacing(BasePsiElement psiElement, CodeStyleSettings settings) {
        BasePsiElement parentPsiElement = getParentPsiElement(psiElement);
        if (parentPsiElement != null) {
            IterationElementType iterationElementType = (IterationElementType) parentPsiElement.elementType;
            ElementType elementType = psiElement.elementType;

            if (elementType instanceof TokenElementType) {
                TokenElementType tokenElementType = (TokenElementType) elementType;
                if (iterationElementType.isSeparator(tokenElementType)) {
                    return tokenElementType.isCharacter() ?
                            SPACING_NO_SPACE :
                            SPACING_ONE_SPACE;
                }
            }
        }
        return SPACING_ONE_SPACE;
    }
}
