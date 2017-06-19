package com.dci.intellij.dbn.language.common;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.TokenPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;

public abstract class DBLanguageFoldingBuilder implements FoldingBuilder, DumbAware {

    @NotNull
    public final FoldingDescriptor[] buildFoldRegions(@NotNull ASTNode node, @NotNull Document document) {
        if (node.getTextLength() == 0) {
            return FoldingDescriptor.EMPTY;
        } else  {
            List<FoldingDescriptor> descriptors = new ArrayList<FoldingDescriptor>();
            createFoldingDescriptors(node.getPsi(), document, descriptors, 0);
            return descriptors.toArray(new FoldingDescriptor[descriptors.size()]);
        }
    }

    protected abstract void createFoldingDescriptors(PsiElement psiElement, Document document, List<FoldingDescriptor> descriptors, int nestingIndex);

    protected static void createLiteralFolding(FoldingContext context, TokenPsiElement tokenPsiElement) {
        if (tokenPsiElement.getTokenType().isLiteral()) {
            TextRange textRange = tokenPsiElement.getTextRange();
            if (textRange.getLength() > 200 && tokenPsiElement.containsLineBreaks()) {
                FoldingDescriptor foldingDescriptor = new FoldingDescriptor(
                        tokenPsiElement.getNode(),
                        textRange);

                context.addDescriptor(foldingDescriptor);
            }
        }
    }

    protected static void createCommentFolding(FoldingContext context, PsiComment psiComment) {
        CharSequence chars = psiComment.getNode().getChars();
        if (StringUtil.startsWith(chars, "/*") && StringUtil.containsLineBreak(chars)) {
            FoldingDescriptor foldingDescriptor = new FoldingDescriptor(
                    psiComment.getNode(),
                    psiComment.getTextRange());

            context.addDescriptor(foldingDescriptor);
        }
    }

    protected static void createAttributeFolding(FoldingContext context, BasePsiElement basePsiElement) {
        if (basePsiElement.is(ElementTypeAttribute.FOLDABLE_BLOCK)) {
            BasePsiElement subjectPsiElement = basePsiElement.findFirstPsiElement(ElementTypeAttribute.SUBJECT);
            if (subjectPsiElement == null) {
                PsiElement firstChild = basePsiElement.getFirstChild();
                if (firstChild instanceof TokenPsiElement) {
                    subjectPsiElement = (BasePsiElement) firstChild;
                }
            }
            if (subjectPsiElement != null && subjectPsiElement.getParent() == basePsiElement) {
                int subjectEndOffset = subjectPsiElement.getTextOffset() + subjectPsiElement.getTextLength();
                int subjectLineNumber = context.document.getLineNumber(subjectEndOffset);
                int blockEndOffset = basePsiElement.getTextOffset() + basePsiElement.getTextLength();
                int blockEndOffsetLineNumber = context.document.getLineNumber(blockEndOffset);

                if (subjectLineNumber < blockEndOffsetLineNumber) {
                    TextRange textRange = new TextRange(subjectEndOffset, blockEndOffset);

                    FoldingDescriptor descriptor = new FoldingDescriptor(basePsiElement.getNode(), textRange);
                    context.addDescriptor(descriptor);
                }
            }
        }
    }

    protected static class FoldingContext {
        private Document document;
        private List<FoldingDescriptor> descriptors;
        boolean folded;
        int nestingIndex;

        public FoldingContext(List<FoldingDescriptor> descriptors, Document document, int nestingIndex) {
            this.document = document;
            this.descriptors = descriptors;
            this.nestingIndex = nestingIndex;
        }

        public void addDescriptor(FoldingDescriptor descriptor) {
            descriptors.add(descriptor);
            nestingIndex++;
            folded = true;
        }

        public boolean isFolded() {
            return folded;
        }

        public int getNestingIndex() {
            return nestingIndex;
        }
    }
}
