package com.dci.intellij.dbn.language.common;

import java.util.List;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.language.common.psi.TokenPsiElement;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;

public abstract class DBLanguageFoldingBuilder implements FoldingBuilder, DumbAware {

    protected static void createLiteralFolding(List<FoldingDescriptor> descriptors, TokenPsiElement tokenPsiElement) {
        if (tokenPsiElement.getTokenType().isLiteral()) {
            TextRange textRange = tokenPsiElement.getTextRange();
            if (textRange.getLength() > 200 && StringUtil.containsLineBreak(tokenPsiElement.getChars())) {
                FoldingDescriptor foldingDescriptor = new FoldingDescriptor(
                        tokenPsiElement.getNode(),
                        textRange);
                descriptors.add(foldingDescriptor);

            }
        }
    }

    protected static void createCommentFolding(List<FoldingDescriptor> descriptors, PsiComment psiComment) {
        CharSequence chars = psiComment.getNode().getChars();
        if (StringUtil.startsWith(chars, "/*") && StringUtil.containsLineBreak(chars)) {
            FoldingDescriptor foldingDescriptor = new FoldingDescriptor(
                    psiComment.getNode(),
                    psiComment.getTextRange());

            descriptors.add(foldingDescriptor);
        }
    }
}
