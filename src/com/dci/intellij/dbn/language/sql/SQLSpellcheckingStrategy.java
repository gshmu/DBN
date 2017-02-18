package com.dci.intellij.dbn.language.sql;

import org.jetbrains.annotations.NotNull;

import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.Tokenizer;

public class SQLSpellcheckingStrategy extends SpellcheckingStrategy {
    @NotNull
    @Override
    public Tokenizer getTokenizer(PsiElement element) {
        if (element instanceof PsiComment) {
            return TEXT_TOKENIZER;
        }

        return EMPTY_TOKENIZER;
    }
}
