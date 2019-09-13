package com.dci.intellij.dbn.language.common.element.lookup;

import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.latent.RuntimeLatent;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.SharedTokenTypeBundle;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeBase;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeRef;
import com.dci.intellij.dbn.language.common.element.impl.IterationElementType;
import com.dci.intellij.dbn.language.common.element.impl.LeafElementType;
import com.dci.intellij.dbn.language.common.element.impl.SequenceElementType;
import com.dci.intellij.dbn.language.common.element.impl.TokenElementType;
import com.dci.intellij.dbn.language.common.element.impl.WrappingDefinition;
import gnu.trove.THashSet;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public abstract class ElementTypeLookupCache<T extends ElementTypeBase>/* implements ElementTypeLookupCache<T>*/ {
    private RuntimeLatent<Set<TokenType>> nextPossibleTokens = Latent.runtime(() -> computeNextPossibleTokens());
    public final T elementType;

    ElementTypeLookupCache(T elementType) {
        this.elementType = elementType;
    }

    public void init() {

    }

    public void cleanup() {
        CollectionUtil.compact(nextPossibleTokens.get());
    }

    /**
     * This method returns all possible tokens (optional or not) which may follow current element.
     *
     * NOTE: to be used only for limited scope, since the tree walk-up
     * is done only until first named element is hit.
     * (named elements do not have parents)
     */
    public Set<TokenType> getNextPossibleTokens() {
        return nextPossibleTokens.get();
    }

    private Set<TokenType> computeNextPossibleTokens() {
        THashSet<TokenType> nextPossibleTokens = new THashSet<>();
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
                        if (!child.optional) {
                            parentElementType = null;
                            break;
                        }
                        child = child.getNext();
                    }
                }
            } else if (parentElementType instanceof IterationElementType) {
                IterationElementType iteration = (IterationElementType) parentElementType;
                TokenElementType[] separatorTokens = iteration.separatorTokens;
                if (separatorTokens != null) {
                    for (TokenElementType separatorToken : separatorTokens) {
                        nextPossibleTokens.add(separatorToken.tokenType);
                    }
                }
            }
            if (parentElementType != null) {
                elementType = parentElementType;
                parentElementType = elementType.getParent();
            }
        }
        return nextPossibleTokens;
    }

    protected DBLanguage getLanguage() {
        return elementType.getLanguage();
    }

    protected SharedTokenTypeBundle getSharedTokenTypes() {
        return getLanguage().getSharedTokenTypes();
    }

    public void collectFirstPossibleTokens(Set<TokenType> bucket) {
        bucket.addAll(getFirstPossibleTokens());
    }

    public Set<LeafElementType> collectFirstPossibleLeafs(ElementLookupContext context) {
        return collectFirstPossibleLeafs(context.reset(), null);
    }

    public Set<TokenType> collectFirstPossibleTokens(ElementLookupContext context) {
        return collectFirstPossibleTokens(context.reset(), null);
    }

    public Set<LeafElementType> collectFirstPossibleLeafs(ElementLookupContext context, @Nullable Set<LeafElementType> bucket) {
        WrappingDefinition wrapping = elementType.getWrapping();
        if (wrapping != null) {
            bucket = initBucket(bucket);
            bucket.add(wrapping.getBeginElementType());
        }
        return bucket;
    }

    public Set<TokenType> collectFirstPossibleTokens(ElementLookupContext context, @Nullable Set<TokenType> bucket) {
        WrappingDefinition wrapping = elementType.getWrapping();
        if (wrapping != null) {
            bucket = initBucket(bucket);
            bucket.add(wrapping.getBeginElementType().tokenType);
        }
        return bucket;
    }

    public void registerLeaf(LeafElementType leaf, ElementTypeBase source) {
        ElementTypeBase parent = elementType.getParent();
        if (parent != null) {
            parent.lookupCache.registerLeaf(leaf, elementType);
        }
    }

    <E> Set<E> initBucket(Set<E> bucket) {
        if (bucket == null) bucket = new java.util.HashSet<E>();
        return bucket;
    }

    public abstract boolean containsToken(TokenType tokenType);

    public abstract boolean containsLeaf(LeafElementType elementType);

    public abstract Set<TokenType> getFirstPossibleTokens();

    public abstract Set<TokenType> getFirstRequiredTokens();

    public abstract boolean couldStartWithLeaf(LeafElementType leafElementType);

    public abstract boolean shouldStartWithLeaf(LeafElementType leafElementType);

    public abstract boolean couldStartWithToken(TokenType tokenType);

    public abstract Set<LeafElementType> getFirstPossibleLeafs();

    public abstract Set<LeafElementType> getFirstRequiredLeafs();

    public abstract boolean startsWithIdentifier();

    public abstract boolean isFirstPossibleToken(TokenType tokenType);

    public abstract boolean isFirstRequiredToken(TokenType tokenType);
}
