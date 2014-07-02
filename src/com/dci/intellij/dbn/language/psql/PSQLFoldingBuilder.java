package com.dci.intellij.dbn.language.psql;

import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.ChameleonPsiElement;
import com.dci.intellij.dbn.language.common.psi.TokenPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PSQLFoldingBuilder implements FoldingBuilder {

    @NotNull
    public FoldingDescriptor[] buildFoldRegions(@NotNull ASTNode node, @NotNull Document document) {
        List<FoldingDescriptor> foldingDescriptors = new ArrayList<FoldingDescriptor>();
        createFoldingDescriptors(node.getPsi(), document, foldingDescriptors, 0);
        return foldingDescriptors.toArray(new FoldingDescriptor[foldingDescriptors.size()]);
    }

    private void createFoldingDescriptors(PsiElement psiElement, Document document, List<FoldingDescriptor> foldingDescriptors, int nestingIndex) {
        PsiElement child = psiElement.getFirstChild();
        while (child != null) {
            if (child instanceof PsiComment) {
                String text = child.getText();
                if (text.startsWith("/*") && text.indexOf('\n') > -1) {
                    FoldingDescriptor foldingDescriptor = new FoldingDescriptor(
                            child.getNode(),
                            child.getTextRange());

                    foldingDescriptors.add(foldingDescriptor);
                }
            }
            if (child instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) child;
                ElementType elementType = basePsiElement.getElementType();
                int blockEndOffset = basePsiElement.getTextOffset() + basePsiElement.getTextLength();

                boolean folded = false;

                if (elementType.is(ElementTypeAttribute.FOLDABLE_BLOCK)) {
                    BasePsiElement subjectPsiElement = basePsiElement.lookupFirstPsiElement(ElementTypeAttribute.SUBJECT);
                    if (subjectPsiElement == null) {
                        PsiElement firstChild = basePsiElement.getFirstChild();
                        if (firstChild instanceof TokenPsiElement) {
                            subjectPsiElement = (BasePsiElement) firstChild;
                        }
                    }
                    if (subjectPsiElement != null && subjectPsiElement.getParent() == basePsiElement) {

                        int subjectEndOffset = subjectPsiElement.getTextOffset() + subjectPsiElement.getTextLength();

                        int subjectLineNumber = document.getLineNumber(subjectEndOffset);
                        int blockEndOffsetLineNumber = document.getLineNumber(blockEndOffset);

                        if (subjectLineNumber < blockEndOffsetLineNumber) {
                            TextRange textRange = new TextRange(subjectEndOffset, blockEndOffset);

                            FoldingDescriptor foldingDescriptor = new FoldingDescriptor(basePsiElement.getNode(), textRange);

                            foldingDescriptors.add(foldingDescriptor);
                            nestingIndex++;
                            folded = true;
                        }
                    }
                } 

                if (!folded && elementType.is(ElementTypeAttribute.STATEMENT)) {
                    if (basePsiElement.containsLineBreaks()) {
                        TextRange textRange = null;

                        BasePsiElement firstPsiElement = basePsiElement.lookupFirstLeafPsiElement();
                        int firstElementEndOffset = firstPsiElement.getTextOffset() + firstPsiElement.getTextLength();
                        int firstElementLineNumber = document.getLineNumber(firstElementEndOffset);


                        BasePsiElement subjectPsiElement = basePsiElement.lookupFirstPsiElement(ElementTypeAttribute.SUBJECT);
                        if (subjectPsiElement != null && subjectPsiElement.getParent() == basePsiElement) {
                            int subjectEndOffset = subjectPsiElement.getTextOffset() + subjectPsiElement.getTextLength();
                            int subjectLineNumber = document.getLineNumber(subjectEndOffset);

                            if (subjectLineNumber == firstElementLineNumber) {
                                textRange = new TextRange(subjectEndOffset, blockEndOffset);
                            }
                        }

                        if (textRange == null) {
                            textRange = new TextRange(firstElementEndOffset, blockEndOffset);
                        }

                        FoldingDescriptor foldingDescriptor = new FoldingDescriptor(basePsiElement.getNode(), textRange);
                        foldingDescriptors.add(foldingDescriptor);
                        nestingIndex++;
                    }
                }

                if (nestingIndex < 9) {
                    createFoldingDescriptors(child, document, foldingDescriptors, nestingIndex);
                }
            }
            child = child.getNextSibling();
        }
    }

    public String getPlaceholderText(@NotNull ASTNode node) {
        PsiElement psiElement = node.getPsi();
        if (psiElement instanceof BasePsiElement) {
            /*BasePsiElement basePsiElement = (BasePsiElement) psiElement;
            PsiElement subject = basePsiElement.lookupFirstSubjectPsiElement();
            StringBuilder buffer = new StringBuilder(basePsiElement.getElementType().getDescription());
            if (subject != null) {
                buffer.append(" (");
                buffer.append(subject.getText());
                buffer.append(")");
            }
            return buffer.toString();*/
            return "(...)";
        }

        if (psiElement instanceof PsiComment) {
            return "/*...*/";
        }

        if (psiElement instanceof ChameleonPsiElement) {
            ChameleonPsiElement chameleonPsiElement = (ChameleonPsiElement) psiElement;
            return chameleonPsiElement.getLanguage().getDisplayName() + " block";
        }
        return "";
    }

    public boolean isCollapsedByDefault(@NotNull ASTNode node) {
        return false;
    }

}
