package com.dci.intellij.dbn.language.common.element.impl;

import com.dci.intellij.dbn.code.common.completion.options.filter.CodeCompletionFilterSettings;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.ElementTypeBundle;
import com.dci.intellij.dbn.language.common.element.IterationElementType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.QualifiedIdentifierElementType;
import com.dci.intellij.dbn.language.common.element.SequenceElementType;
import com.dci.intellij.dbn.language.common.element.TokenElementType;
import com.dci.intellij.dbn.language.common.element.path.PathNode;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeDefinitionException;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiWhiteSpace;
import gnu.trove.THashSet;
import org.jdom.Element;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

public abstract class LeafElementTypeImpl extends AbstractElementType implements LeafElementType {
    private TokenType tokenType;

    private boolean optional;

    public LeafElementTypeImpl(ElementTypeBundle bundle, ElementType parent, String id, Element def) throws ElementTypeDefinitionException {
        super(bundle, parent, id, def);
    }

    public LeafElementTypeImpl(ElementTypeBundle bundle, ElementType parent, String id, String description) throws ElementTypeDefinitionException {
        super(bundle, parent, id, description);
    }

    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public void registerLeaf() {
        getLookupCache().init();
        getParent().getLookupCache().registerLeaf(this, this);
    }

    public abstract boolean isSameAs(LeafElementType elementType);
    public abstract boolean isIdentifier();

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public boolean isOptional() {
        return optional;
    }

    public boolean isLeaf() {
        return true;
    }

    public ElementType getPreviousElement(PathNode pathNode) {
        int position = 0;
        while (pathNode != null) {
            ElementType elementType = pathNode.getElementType();
            if (elementType instanceof SequenceElementType) {
                SequenceElementType sequenceElementType = (SequenceElementType) elementType;
                if (position > 0 ) {
                    return sequenceElementType.getElementTypes()[position-1];
                }
            }
            position = pathNode.getCurrentSiblingIndex();
            pathNode = pathNode.getParent();
        }
        return null;
    }

    public Set<LeafElementType> getAlternativeLeafs(PathNode pathNode) {
        ElementType previousElementType = getPreviousElement(pathNode);
        // FIXME ----------- implement this
        return previousElementType.getLookupCache().getFirstPossibleLeafs();
    }

    public Set<LeafElementType> getNextPossibleLeafs(PathNode pathNode, CodeCompletionFilterSettings filterSettings) {
        Set<LeafElementType> possibleLeafs = new THashSet<LeafElementType>();
        int position = 0;
        while (pathNode != null) {
            ElementType elementType = pathNode.getElementType();

            if (elementType instanceof SequenceElementType) {
                SequenceElementType sequenceElementType = (SequenceElementType) elementType;

                int elementsCount = sequenceElementType.getElementTypes().length;

                for (int i=position+1; i<elementsCount; i++) {
                    ElementType next = sequenceElementType.getElementTypes()[i];
                    possibleLeafs.addAll(next.getLookupCache().getFirstPossibleLeafs());
                    if (!sequenceElementType.isOptional(i)) {
                        pathNode = null;
                        break;
                    }
                }
            } else if (elementType instanceof IterationElementType) {
                IterationElementType iterationElementType = (IterationElementType) elementType;
                TokenElementType[] separatorTokens = iterationElementType.getSeparatorTokens();
                if (separatorTokens == null) {
                    possibleLeafs.addAll(iterationElementType.getIteratedElementType().getLookupCache().getFirstPossibleLeafs());
                } else {
                    possibleLeafs.addAll(Arrays.asList(separatorTokens));
                }
            } else if (elementType instanceof QualifiedIdentifierElementType) {
                QualifiedIdentifierElementType qualifiedIdentifierElementType = (QualifiedIdentifierElementType) elementType;
                if (this == qualifiedIdentifierElementType.getSeparatorToken()) {
                    break;
                }
            }
            if (pathNode != null) {
                position = pathNode.getCurrentSiblingIndex();
                pathNode = pathNode.getParent();
            }
        }
        return possibleLeafs;
    }

    public Set<LeafElementType> getNextRequiredLeafs(PathNode pathNode) {
        Set<LeafElementType> requiredLeafs = new THashSet<LeafElementType>();
        int index = 0;
        while (pathNode != null) {
            ElementType elementType = pathNode.getElementType();

            if (elementType instanceof SequenceElementType) {
                SequenceElementType sequenceElementType = (SequenceElementType) elementType;
                int elementsCount = sequenceElementType.getElementTypes().length;

                for (int i=index+1; i<elementsCount; i++) {
                    if (!sequenceElementType.isOptional(i)) {
                        ElementType next = sequenceElementType.getElementTypes()[i];
                        requiredLeafs.addAll(next.getLookupCache().getFirstRequiredLeafs());
                        pathNode = null;
                        break;
                    }
                }
            } else if (elementType instanceof IterationElementType) {
                IterationElementType iteration = (IterationElementType) elementType;
                TokenElementType[] separatorTokens = iteration.getSeparatorTokens();
                Collections.addAll(requiredLeafs, separatorTokens);
            }
            if (pathNode != null) {
                index = pathNode.getCurrentSiblingIndex();
                pathNode = pathNode.getParent();
            }
        }
        return requiredLeafs;
    }


    /**
     * Returns the index of the corresponding ElementType in it's parent
     * Only applicable if the given astNode is corresponding to an ElementType within a SequenceElementType
     * For all the other cases it returns 0.
     */
    private int getElementTypeIndex(ASTNode astNode){
        ASTNode parentAstNode = astNode.getTreeParent();
        if (parentAstNode.getElementType() instanceof SequenceElementType) {
            SequenceElementType sequenceElementType = (SequenceElementType) parentAstNode.getElementType();
            int index = 0;
            ASTNode child = parentAstNode.getFirstChildNode();
            while (child != null) {
                if (astNode == child) {
                    break;
                }
                index++;
                child = child.getTreeNext();
                if (child instanceof PsiWhiteSpace){
                    child = child.getTreeNext();
                }
            }
            return sequenceElementType.indexOf((ElementType) astNode.getElementType(), index);
        }
        return 0;
    }
}
