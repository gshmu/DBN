package com.dci.intellij.dbn.language.common.element.impl;

import com.dci.intellij.dbn.common.index.Indexable;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ChameleonElementType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.ElementTypeBundle;
import com.dci.intellij.dbn.language.common.element.cache.ElementLookupContext;
import com.dci.intellij.dbn.language.common.element.cache.ElementTypeLookupCache;
import com.dci.intellij.dbn.language.common.element.parser.ParserContext;
import com.dci.intellij.dbn.language.common.element.path.BasicPathNode;
import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;
import com.dci.intellij.dbn.language.common.element.path.PathNode;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeDefinitionException;
import gnu.trove.THashSet;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

@Getter
@Setter
public abstract class LeafElementType extends ElementTypeBase implements Indexable {
    private TokenType tokenType;
    private boolean optional;
    private int idx;

    LeafElementType(ElementTypeBundle bundle, ElementTypeBase parent, String id, Element def) throws ElementTypeDefinitionException {
        super(bundle, parent, id, def);
        idx = bundle.nextIndex();
        bundle.registerElement(this);
    }

    LeafElementType(ElementTypeBundle bundle, ElementTypeBase parent, String id, String description) {
        super(bundle, parent, id, description);
        idx = bundle.nextIndex();
        bundle.registerElement(this);
    }

    @Override
    public int index() {
        return idx;
    }

    public void registerLeaf() {
        getParent().getLookupCache().registerLeaf(this, this);
    }

    public abstract boolean isSameAs(LeafElementType elementType);

    public abstract boolean isIdentifier();

    @Override
    public boolean isLeaf() {
        return true;
    }

    public static ElementType getPreviousElement(BasicPathNode pathNode) {
        int position = 0;
        while (pathNode != null) {
            ElementType elementType = pathNode.getElementType();
            if (elementType instanceof SequenceElementType) {
                SequenceElementType sequenceElementType = (SequenceElementType) elementType;
                if (position > 0 ) {
                    return sequenceElementType.getChild(position-1).getElementType();
                }
            }
            position = pathNode.getIndexInParent();
            pathNode = pathNode.getParent();
        }
        return null;
    }

    public Set<LeafElementType> getNextPossibleLeafs(PathNode pathNode, @NotNull ElementLookupContext context) {
        Set<LeafElementType> possibleLeafs = new THashSet<LeafElementType>();
        int position = 1;
        while (pathNode != null) {
            ElementType elementType = pathNode.getElementType();

            if (elementType instanceof SequenceElementType) {
                SequenceElementType sequenceElementType = (SequenceElementType) elementType;

                int elementsCount = sequenceElementType.getChildCount();

                if (position < elementsCount) {
                    ElementTypeRef child = sequenceElementType.getChild(position);
                    while (child != null) {
                        if (context.check(child)) {
                            child.getLookupCache().captureFirstPossibleLeafs(context.reset(), possibleLeafs);
                            if (!child.isOptional()) {
                                pathNode = null;
                                break;
                            }
                        }
                        child = child.getNext();
                    }
                } else if (elementType instanceof NamedElementType){
                    context.removeBranchMarkers((NamedElementType) elementType);
                }
            } else if (elementType instanceof IterationElementType) {
                IterationElementType iterationElementType = (IterationElementType) elementType;
                TokenElementType[] separatorTokens = iterationElementType.getSeparatorTokens();
                if (separatorTokens == null) {
                    ElementTypeLookupCache<?> lookupCache = iterationElementType.getIteratedElementType().getLookupCache();
                    lookupCache.captureFirstPossibleLeafs(context.reset(), possibleLeafs);
                } else {
                    possibleLeafs.addAll(Arrays.asList(separatorTokens));
                }
            } else if (elementType instanceof QualifiedIdentifierElementType) {
                QualifiedIdentifierElementType qualifiedIdentifierElementType = (QualifiedIdentifierElementType) elementType;
                if (this == qualifiedIdentifierElementType.getSeparatorToken()) {
                    break;
                }
            } else if (elementType instanceof ChameleonElementType) {
                ChameleonElementType chameleonElementType = (ChameleonElementType) elementType;
                ElementTypeBundle elementTypeBundle = chameleonElementType.getParentLanguage().getParserDefinition().getParser().getElementTypes();;
                ElementTypeLookupCache<?> lookupCache = elementTypeBundle.getRootElementType().getLookupCache();
                possibleLeafs.addAll(lookupCache.getFirstPossibleLeafs());
            }
            if (pathNode != null) {
                ElementType pathElementType = pathNode.getElementType();
                if (pathElementType != null && pathElementType.is(ElementTypeAttribute.STATEMENT) && context.isBreakOnAttribute(ElementTypeAttribute.STATEMENT)){
                    break;
                }
                position = pathNode.getIndexInParent() + 1;
                pathNode = pathNode.getParent();
            }
        }
        return possibleLeafs;
    }

    public boolean isNextPossibleToken(TokenType tokenType, ParsePathNode pathNode, ParserContext context) {
        return isNextToken(tokenType, pathNode, context, false);
    }

    public boolean isNextRequiredToken(TokenType tokenType, ParsePathNode pathNode, ParserContext context) {
        return isNextToken(tokenType, pathNode, context, true);
    }

    private boolean isNextToken(TokenType tokenType, ParsePathNode pathNode, ParserContext context, boolean required) {
        int position = -1;
        while (pathNode != null) {
            ElementType elementType = pathNode.getElementType();

            if (elementType instanceof SequenceElementType) {
                SequenceElementType sequenceElementType = (SequenceElementType) elementType;

                int elementsCount = sequenceElementType.getChildCount();
                if (position == -1) {
                    position = pathNode.getCursorPosition() + 1;
                }

                //int position = sequenceElementType.indexOf(this) + 1;
/*
                int position = pathNode.getCursorPosition();
                if (pathNode.getCurrentOffset() < context.getBuilder().getCurrentOffset()) {
                    position++;
                }
*/
                if (position < elementsCount) {
                    ElementTypeRef child = sequenceElementType.getChild(position);
                    while (child != null) {
                        ElementTypeLookupCache lookupCache = child.getLookupCache();
                        if (required) {
                            if (lookupCache.isFirstRequiredToken(tokenType) && !child.isOptional()) {
                                return true;
                            }
                        } else {
                            if (lookupCache.isFirstPossibleToken(tokenType)) {
                                return true;
                            }
                        }

                        if (!child.isOptional()/* && !child.isOptionalFromHere()*/) {
                            return false;
                        }
                        child = child.getNext();
                    }
                }
            } else if (elementType instanceof IterationElementType) {
                IterationElementType iterationElementType = (IterationElementType) elementType;
                TokenElementType[] separatorTokens = iterationElementType.getSeparatorTokens();
                if (separatorTokens == null) {
                    ElementTypeLookupCache<?> lookupCache = iterationElementType.getIteratedElementType().getLookupCache();
                    if (required ?
                            lookupCache.isFirstRequiredToken(tokenType) :
                            lookupCache.isFirstPossibleToken(tokenType)) {
                        return true;
                    }
                }
            } else if (elementType instanceof QualifiedIdentifierElementType) {
                QualifiedIdentifierElementType qualifiedIdentifierElementType = (QualifiedIdentifierElementType) elementType;
                if (this == qualifiedIdentifierElementType.getSeparatorToken()) {
                    break;
                }
            } else if (elementType instanceof WrapperElementType) {
                WrapperElementType wrapperElementType = (WrapperElementType) elementType;
                return wrapperElementType.getEndTokenElement().getTokenType() == tokenType;
            }

            position = pathNode.getIndexInParent() + 1;
            pathNode = pathNode.getParent();
        }
        return false;
    }

    public Set<LeafElementType> getNextRequiredLeafs(PathNode pathNode, ParserContext context) {
        Set<LeafElementType> requiredLeafs = new THashSet<>();
        int position = 0;
        while (pathNode != null) {
            ElementType elementType = pathNode.getElementType();

            if (elementType instanceof SequenceElementType) {
                SequenceElementType sequenceElementType = (SequenceElementType) elementType;

                ElementTypeRef child = sequenceElementType.getChild(position + 1);
                while (child != null) {
                    if (!child.isOptional()) {
                        ElementTypeLookupCache<?> lookupCache = child.getLookupCache();
                        requiredLeafs.addAll(lookupCache.getFirstRequiredLeafs());
                        pathNode = null;
                        break;
                    }
                    child = child.getNext();
                }
            } else if (elementType instanceof IterationElementType) {
                IterationElementType iteration = (IterationElementType) elementType;
                TokenElementType[] separatorTokens = iteration.getSeparatorTokens();
                Collections.addAll(requiredLeafs, separatorTokens);
            }
            if (pathNode != null) {
                position = pathNode.getIndexInParent();
                pathNode = pathNode.getParent();
            }
        }
        return requiredLeafs;
    }

    @Override
    public void collectLeafElements(Set<LeafElementType> bucket) {
        super.collectLeafElements(bucket);
        bucket.add(this);
    }
}
