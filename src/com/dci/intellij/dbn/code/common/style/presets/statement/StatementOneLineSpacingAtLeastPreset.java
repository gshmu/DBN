package com.dci.intellij.dbn.code.common.style.presets.statement;

import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.Wrap;
import com.intellij.psi.codeStyle.CodeStyleSettings;

public class StatementOneLineSpacingAtLeastPreset extends StatementAbstractPreset {
    public StatementOneLineSpacingAtLeastPreset() {
        super("one_line_at_least", "One line at least");
    }

    @Nullable
    public Wrap getWrap(BasePsiElement psiElement, CodeStyleSettings settings) {
        return null;
    }

    @Nullable
    public Spacing getSpacing(BasePsiElement psiElement, CodeStyleSettings settings) {
        return SPACING_MIN_ONE_LINE;
    }
}