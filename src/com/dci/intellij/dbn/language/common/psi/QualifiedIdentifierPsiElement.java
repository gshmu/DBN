package com.dci.intellij.dbn.language.common.psi;

import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.latent.MutableLatent;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.IdentifierElementType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.QualifiedIdentifierElementType;
import com.dci.intellij.dbn.language.common.element.impl.QualifiedIdentifierVariant;
import com.dci.intellij.dbn.language.common.element.parser.ParseResultType;
import com.dci.intellij.dbn.object.common.DBObject;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QualifiedIdentifierPsiElement extends SequencePsiElement {
    private Latent<ParseVariants> parseVariants = MutableLatent.create(
            () -> this.getElementsCount(),
            () -> this.buildParseVariants());

    public QualifiedIdentifierPsiElement(ASTNode astNode, ElementType elementType) {
        super(astNode, elementType);
    }

    public QualifiedIdentifierElementType getElementType() {
        return (QualifiedIdentifierElementType) super.getElementType();
    }

    public List<QualifiedIdentifierVariant> getParseVariants() {
        return parseVariants.get().getElements();
    }

    public int getIndexOf(LeafPsiElement leafPsiElement) {
        int index = 0;
        PsiElement child = getFirstChild();
        while (child != null) {
            if (child == leafPsiElement){
                return index;
            }
            if (child instanceof IdentifierPsiElement) {
                index++;
            }
            child = child.getNextSibling();
        }
        return -1;        
    }

    public int getIndexOf(IdentifierElementType identifierElementType) {
        int index = 0;
        PsiElement child = getFirstChild();
        while (child != null) {
            if (child instanceof IdentifierPsiElement) {
                IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) child;
                if (identifierPsiElement.getElementType() == identifierElementType) {
                    return index;
                } else {
                    index++;
                }
            }
            child = child.getNextSibling();
        }
        return -1;
    }

    public IdentifierPsiElement getLeafAtIndex(int index) {
        if (index >= 0) {
            int idx = 0;
            PsiElement child = getFirstChild();
            while (child != null) {
                if (child instanceof IdentifierPsiElement) {
                    if (idx == index) {
                        return (IdentifierPsiElement) child;
                    } else {
                        idx++;
                    }
                }
                child = child.getNextSibling();
            }
        }
        return null;
    }

    private ParseVariants buildParseVariants() {
        List<QualifiedIdentifierVariant> parseVariants = new ArrayList<>();
        for (LeafElementType[] elementTypes : getElementType().getVariants()) {

            ParseResultType resultType = ParseResultType.FULL_MATCH;
            for (int i=0; i< elementTypes.length; i++) {

                BasePsiElement leaf = getLeafAtIndex(i);
                // if no mach -> consider as partial if not first element
                if (leaf == null) {
                    resultType = i==0 ? ParseResultType.NO_MATCH : ParseResultType.PARTIAL_MATCH;
                    break;
                }

                LeafElementType leafElementType = (LeafElementType) leaf.getElementType();
                if (!(leafElementType.isIdentifier() && elementTypes[i].isIdentifier() || leafElementType.isSameAs(elementTypes[i]))) {
                    resultType = i==0 ? ParseResultType.NO_MATCH : ParseResultType.PARTIAL_MATCH;
                    break;
                }

                BasePsiElement separator = leaf.getNextElement();
                if (separator == null) {
                    // if is NOT the last element and no separator found -> consider as partial mach
                    if (i < elementTypes.length -1) {
                        resultType = ParseResultType.PARTIAL_MATCH;
                        break;
                    }
                } else {
                    // if is the last element and still separator found -> not match;
                    if (i == elementTypes.length -1){
                        resultType = ParseResultType.NO_MATCH;
                        break;
                    }
                }
            }
            if (resultType != ParseResultType.NO_MATCH) {
                parseVariants.add(new QualifiedIdentifierVariant(elementTypes, resultType == ParseResultType.PARTIAL_MATCH));
            }
        }
        Collections.sort(parseVariants);
        return new ParseVariants(parseVariants);
    }

    private LeafPsiElement lookupParentElementFor(LeafPsiElement element) {
        int index = getIndexOf(element);
        if (index > 0) {
            return getLeafAtIndex(index - 1);
        }
        return null;
    }

    public DBObject lookupParentObjectFor(LeafPsiElement leafPsiElement) {
        LeafPsiElement parent = lookupParentElementFor(leafPsiElement);
        return parent == null ? null : parent.resolveUnderlyingObject();
    }

    public DBObject lookupParentObjectFor(LeafElementType leafElementType) {
       for (QualifiedIdentifierVariant parseVariant : getParseVariants()) {
            if (parseVariant.getLeafs().length == getElementsCount()) {
                int index = parseVariant.getIndexOf(leafElementType);
                if (index > 0) {
                    IdentifierPsiElement previousPsiElement = getLeafAtIndex(index-1);
                    if (previousPsiElement != null) {
                        DBObject parentObject = previousPsiElement.resolveUnderlyingObject();
                        if (parentObject != null) {
                            return parentObject;
                        }
                    }
                }
            }
        }
        return null;
    }

    int getElementsCount() {
        int count = 0;
        PsiElement child = getFirstChild();
        while (child != null) {
            if (child instanceof LeafPsiElement) {
                LeafPsiElement leafPsiElement = (LeafPsiElement) child;
                if (leafPsiElement.getElementType() != getElementType().getSeparatorToken() ) {
                    count++;
                }
            }
            child = child.getNextSibling();
        }
        return count;
    }

    private static class ParseVariants {
        private List<QualifiedIdentifierVariant> elements;

        ParseVariants(List<QualifiedIdentifierVariant> elements) {
            this.elements = elements;
        }

        public List<QualifiedIdentifierVariant> getElements() {
            return elements;
        }
    }
}
