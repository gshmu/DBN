package com.dci.intellij.dbn.language.common.element.lookup;

import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.SharedTokenTypeBundle;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.*;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeRef;
import com.dci.intellij.dbn.language.common.element.impl.WrappingDefinition;
import gnu.trove.THashSet;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public abstract class ElementTypeLookupCacheBase<T extends ElementType> implements ElementTypeLookupCache<T> {
    private Set<TokenType> nextPossibleTokens;
    protected T elementType;

    ElementTypeLookupCacheBase(T elementType) {
        this.elementType = elementType;
    }

    @Override
    public T getElementType() {
        return elementType;
    }

    @Override
    public void init() {

    }

    /**
     * This method returns all possible tokens (optional or not) which may follow current element.
     *
     * NOTE: to be used only for limited scope, since the tree walk-up
     * is done only until first named element is hit.
     * (named elements do not have parents)
     */
    public Set<TokenType> getNextPossibleTokens() {
        if (nextPossibleTokens == null) {
            synchronized (this) {
                if (nextPossibleTokens == null) {
                    THashSet<TokenType> nextPossibleTokens = new THashSet<TokenType>();
                    ElementType elementType = this.elementType;
                    ElementType parentElementType = elementType.getParent();
                    while (parentElementType != null) {
                        if (parentElementType instanceof SequenceElementType) {
                            SequenceElementType sequenceElementType = (SequenceElementType) parentElementType;
                            int elementsCount = sequenceElementType.getChildCount();
                            int index = sequenceElementType.indexOf(elementType, 0) + 1;

                            if (index < elementsCount) {
                                ElementTypeRef child = sequenceElementType.getChild(index);
                                while (child != null) {
                                    child.getLookupCache().collectFirstPossibleTokens(nextPossibleTokens);
                                    if (!child.isOptional()) {
                                        parentElementType = null;
                                        break;
                                    }
                                    child = child.getNext();
                                }
                            }
                        } else if (parentElementType instanceof IterationElementType) {
                            IterationElementType iteration = (IterationElementType) parentElementType;
                            TokenElementType[] separatorTokens = iteration.getSeparatorTokens();
                            if (separatorTokens != null) {
                                for (TokenElementType separatorToken : separatorTokens) {
                                    nextPossibleTokens.add(separatorToken.getTokenType());
                                }
                            }
                        }
                        if (parentElementType != null) {
                            elementType = parentElementType;
                            parentElementType = elementType.getParent();
                        }
                    }
                    this.nextPossibleTokens = nextPossibleTokens;
                }
            }
        }
        return nextPossibleTokens;
    }

    protected DBLanguage getLanguage() {
        return getElementType().getLanguage();
    }

    protected SharedTokenTypeBundle getSharedTokenTypes() {
        return getLanguage().getSharedTokenTypes();
    }

    @Override
    public void collectFirstPossibleTokens(Set<TokenType> bucket) {
        bucket.addAll(getFirstPossibleTokens());
    }

    @Override
    public Set<LeafElementType> collectFirstPossibleLeafs(ElementLookupContext context) {
        return collectFirstPossibleLeafs(context.reset(), null);
    }

    @Override
    public Set<TokenType> collectFirstPossibleTokens(ElementLookupContext context) {
        return collectFirstPossibleTokens(context.reset(), null);
    }

    @Override
    public Set<LeafElementType> collectFirstPossibleLeafs(ElementLookupContext context, @Nullable Set<LeafElementType> bucket) {
        WrappingDefinition wrapping = elementType.getWrapping();
        if (wrapping != null) {
            bucket = initBucket(bucket);
            bucket.add(wrapping.getBeginElementType());
        }
        return bucket;
    }

    @Override
    public Set<TokenType> collectFirstPossibleTokens(ElementLookupContext context, @Nullable Set<TokenType> bucket) {
        WrappingDefinition wrapping = elementType.getWrapping();
        if (wrapping != null) {
            bucket = initBucket(bucket);
            bucket.add(wrapping.getBeginElementType().getTokenType());
        }
        return bucket;
    }

    @Override
    public void registerLeaf(LeafElementType leaf, ElementType source) {
        ElementType parent = elementType.getParent();
        if (parent != null) {
            parent.getLookupCache().registerLeaf(leaf, elementType);
        }
    }

    protected <E> Set<E> initBucket(Set<E> bucket) {
        if (bucket == null) bucket = new java.util.HashSet<E>();
        return bucket;
    }
}
