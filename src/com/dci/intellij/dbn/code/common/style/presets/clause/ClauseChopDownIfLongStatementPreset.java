package com.dci.intellij.dbn.code.common.style.presets.clause;

import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.NamedPsiElement;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.Wrap;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleSettings;

public class ClauseChopDownIfLongStatementPreset extends ClauseAbstractPreset {
    public ClauseChopDownIfLongStatementPreset() {
        super("chop_down_if_statement_long", "Chop down if statement long");
    }

    @Nullable
    public Wrap getWrap(BasePsiElement psiElement, CodeStyleSettings settings) {
        NamedPsiElement namedPsiElement = getEnclosingStatementElement(psiElement);
        boolean shouldWrap = namedPsiElement != null && namedPsiElement.approximateLength() > settings.getRightMargin(psiElement.getLanguage());
        return shouldWrap ? WRAP_ALWAYS : WRAP_NONE;

    }

    @Nullable
    public Spacing getSpacing(BasePsiElement psiElement, CodeStyleSettings settings) {
        NamedPsiElement namedPsiElement = getEnclosingStatementElement(psiElement);
        boolean shouldWrap = namedPsiElement!= null && namedPsiElement.approximateLength() > settings.getRightMargin(psiElement.getLanguage());
        return getSpacing(psiElement, shouldWrap);
    }

    @Nullable
    private NamedPsiElement getEnclosingStatementElement(BasePsiElement psiElement) {
        BasePsiElement parentPsiElement = getParentPsiElement(psiElement);
        if (parentPsiElement != null) {
            NamedPsiElement namedPsiElement = (NamedPsiElement) parentPsiElement.findEnclosingPsiElement(ElementTypeAttribute.STATEMENT);
            if (namedPsiElement == null) {
                PsiElement childPsiElement = parentPsiElement.getFile().getFirstChild();
                while (childPsiElement != null) {
                    if (childPsiElement instanceof NamedPsiElement) {
                        return (NamedPsiElement) childPsiElement;
                    }
                    childPsiElement = childPsiElement.getNextSibling();
                }
            } else {
                return namedPsiElement;
            }
        }
        return null;
    }

}