package com.dci.intellij.dbn.code.common.style.formatting;

import com.dci.intellij.dbn.code.common.style.presets.CodeStylePreset;
import com.intellij.formatting.Alignment;
import com.intellij.formatting.Block;
import com.intellij.formatting.ChildAttributes;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.Wrap;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PassiveFormattingBlock implements Block {
    private static final  List<Block> EMPTY_LIST = new ArrayList<Block>(0);
    private PsiElement psiElement;

    public PassiveFormattingBlock(PsiElement psiElement) {
        this.psiElement = psiElement;
    }

    @Override
    @NotNull
    public TextRange getTextRange() {
        return psiElement.getTextRange();
    }

    @Override
    @NotNull
    public List<Block> getSubBlocks() {
        return EMPTY_LIST;
    }

    @Override
    public Wrap getWrap() {
        return CodeStylePreset.WRAP_NONE;
    }

    @Override
    public Indent getIndent() {
        return Indent.getNoneIndent();
    }

    @Override
    public Alignment getAlignment() {
        return null;
    }

    @Override
    public Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
        return null;
    }

    @Override
    @NotNull
    public ChildAttributes getChildAttributes(int newChildIndex) {
        return new ChildAttributes(Indent.getNoneIndent(), Alignment.createAlignment());
    }

    @Override
    public boolean isIncomplete() {
        return false;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }
}
