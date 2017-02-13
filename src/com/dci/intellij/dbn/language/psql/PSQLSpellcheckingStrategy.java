package com.dci.intellij.dbn.language.psql;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.Tokenizer;

public class PSQLSpellcheckingStrategy extends SpellcheckingStrategy {
    @NotNull
    @Override
    public Tokenizer getTokenizer(PsiElement element) {
        if (element instanceof PsiWhiteSpace) {
            return EMPTY_TOKENIZER;
        }

        if (element instanceof PsiComment) {
            return TEXT_TOKENIZER;
        }

        if (element instanceof LeafPsiElement) {
            LeafPsiElement leafPsiElement = (LeafPsiElement) element;
            PsiElement parent = leafPsiElement.getParent();
            if (parent instanceof IdentifierPsiElement) {
                IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) parent;
                if (identifierPsiElement.isDefinition()) {
                    return TEXT_TOKENIZER;
                }
            }
        }

        return EMPTY_TOKENIZER;
    }
}
