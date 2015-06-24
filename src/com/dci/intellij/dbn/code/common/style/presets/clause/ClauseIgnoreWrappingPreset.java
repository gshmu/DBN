package com.dci.intellij.dbn.code.common.style.presets.clause;

import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.Wrap;
import com.intellij.psi.codeStyle.CodeStyleSettings;

public class ClauseIgnoreWrappingPreset extends ClauseAbstractPreset {
    public ClauseIgnoreWrappingPreset() {
        super("ignore_wrapping", "Ignore");
    }

    @Nullable
    public Wrap getWrap(BasePsiElement psiElement, CodeStyleSettings settings) {
        return null;
    }

    @Nullable
    public Spacing getSpacing(BasePsiElement psiElement, CodeStyleSettings settings) {
        return null;
    }
}