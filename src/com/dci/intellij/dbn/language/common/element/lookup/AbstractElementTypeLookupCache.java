package com.dci.intellij.dbn.language.common.element.lookup;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.dci.intellij.dbn.language.common.SharedTokenTypeBundle;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.ElementTypeBundle;
import com.dci.intellij.dbn.language.common.element.IdentifierElementType;
import com.dci.intellij.dbn.language.common.element.IterationElementType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.SequenceElementType;
import com.dci.intellij.dbn.language.common.element.TokenElementType;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeRef;
import com.dci.intellij.dbn.language.common.element.impl.WrappingDefinition;
import gnu.trove.THashMap;
import gnu.trove.THashSet;

public abstract class AbstractElementTypeLookupCache<T extends ElementType> implements ElementTypeLookupCache<T> {
    private T elementType;

    protected Set<LeafElementType> allPossibleLeafs = new THashSet<LeafElementType>();
    protected Set<LeafElementType> firstPossibleLeafs = new THashSet<LeafElementType>();
    protected Set<LeafElementType> firstRequiredLeafs = new THashSet<LeafElementType>();
    protected Set<TokenType> allPossibleTokens = new THashSet<TokenType>();
    protected Set<TokenType> firstPossibleTokens = new THashSet<TokenType>();
    private Map<TokenType, Boolean> landmarkTokens;
    private Boolean startsWithIdentifier;

    private Set<TokenType> nextPossibleTokens;

    public AbstractElementTypeLookupCache(T elementType) {
        this.elementType = elementType;
        if (!elementType.isLeaf()) {
            landmarkTokens = new THashMap<TokenType, Boolean>();
        }
        WrappingDefinition wrapping = getElementType().getWrapping();
        if (wrapping != null) {
            TokenType wrappingBeginTokenType = wrapping.getBeginElementType().getTokenType();
            TokenType wrappingEndTokenType = wrapping.getEndElementType().getTokenType();
            allPossibleTokens.add(wrappingBeginTokenType);
            allPossibleTokens.add(wrappingEndTokenType);
            firstPossibleTokens.add(wrappingBeginTokenType);
        }
    }

    public void init() {}

    public T getElementType() {
        return elementType;
    }

    private ElementTypeBundle getBundle() {
        return elementType.getElementBundle();
    }


    public boolean containsToken(TokenType tokenType) {
        return allPossibleTokens != null && allPossibleTokens.contains(tokenType);
    }

    public boolean containsLeaf(LeafElementType leafElementType) {
        return leafElementType == getElementType() || (allPossibleLeafs != null && allPossibleLeafs.contains(leafElementType));
    }

    @Override
    public Set<LeafElementType> collectFirstPossibleLeafs() {
        return collectFirstPossibleLeafs(new ElementLookupContext());
    }

    @Override
    public Set<LeafElementType> collectFirstPossibleLeafs(ElementLookupContext context) {
        return collectFirstPossibleLeafs(context.reset(), null);
    }

    @Override
    public Set<TokenType> collectFirstPossibleTokens() {
        return collectFirstPossibleTokens(new ElementLookupContext());
    }

    @Override
    public Set<TokenType> collectFirstPossibleTokens(ElementLookupContext context) {
        return collectFirstPossibleTokens(context.reset(), null);
    }

    protected <E> Set<E> initBucket(Set<E> bucket) {
        if (bucket == null) bucket = new HashSet<E>();
        return bucket;
    }

    public Set<TokenType> getFirstPossibleTokens() {
        return firstPossibleTokens;
    }

    public Set<LeafElementType> getFirstRequiredLeafs() {
        return firstRequiredLeafs;
    }

    @Deprecated
    public boolean canStartWithLeaf(LeafElementType leafElementType) {
        return firstPossibleLeafs.contains(leafElementType);
    }

    public boolean canStartWithToken(TokenType tokenType) {
        return firstPossibleTokens.contains(tokenType);
    }

    public boolean shouldStartWithLeaf(LeafElementType leafElementType) {
        return firstRequiredLeafs.contains(leafElementType);
    }

    public void registerLeaf(LeafElementType leaf, ElementType pathChild) {
        boolean initAllElements = !containsLeaf(leaf);
        boolean isFirstPossibleElements = isFirstPossibleLeaf(leaf, pathChild);
        boolean isFirstRequiredLeaf = isFirstRequiredLeaf(leaf, pathChild);

        // register first possible leafs
        ElementTypeLookupCache lookupCache = leaf.getLookupCache();
        if (isFirstPossibleElements) {
            firstPossibleLeafs.add(leaf);
            firstPossibleTokens.addAll(lookupCache.getFirstPossibleTokens());
        }

        // register first required leafs
        if (isFirstRequiredLeaf) {
            firstRequiredLeafs.add(leaf);
        }

        if (initAllElements) {
            // register all possible leafs
            allPossibleLeafs.add(leaf);

            // register all possible tokens
            if (leaf instanceof IdentifierElementType) {
                SharedTokenTypeBundle sharedTokenTypes = getElementType().getLanguage().getSharedTokenTypes();
                allPossibleTokens.add(sharedTokenTypes.getIdentifier());
                allPossibleTokens.add(sharedTokenTypes.getQuotedIdentifier());
            } else {
                allPossibleTokens.add(leaf.getTokenType());
            }
        }

        if (isFirstPossibleElements || isFirstRequiredLeaf || initAllElements) {
            // walk the tree up
            registerLeafInParent(leaf);
        }
    }

    protected void registerLeafInParent(LeafElementType leaf) {
        ElementType parent = getElementType().getParent();
        if (parent != null) {
            parent.getLookupCache().registerLeaf(leaf, getElementType());
        }
    }

    public synchronized boolean containsLandmarkToken(TokenType tokenType) {
        if (getElementType().isLeaf()) return containsToken(tokenType);

        Boolean value = landmarkTokens.get(tokenType);
        if (value == null) {
            value = containsLandmarkToken(tokenType, null);
            landmarkTokens.put(tokenType, value);
        }
        return value;
    }


    public synchronized boolean startsWithIdentifier() {
        if (startsWithIdentifier == null) {
            startsWithIdentifier =  startsWithIdentifier(null);
        }
        return startsWithIdentifier;
    }

    public boolean containsIdentifiers() {
        return containsToken(getBundle().getTokenTypeBundle().getIdentifier());
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
            nextPossibleTokens = new THashSet<TokenType>();
            ElementType elementType = getElementType();
            ElementType parentElementType = elementType.getParent();
            while (parentElementType != null) {
                if (parentElementType instanceof SequenceElementType) {
                    SequenceElementType sequenceElementType = (SequenceElementType) parentElementType;
                    int elementsCount = sequenceElementType.getChildCount();
                    int index = sequenceElementType.indexOf(elementType, 0);

                    for (int i = index + 1; i < elementsCount; i++) {
                        ElementTypeRef next = sequenceElementType.getChild(i);
                        nextPossibleTokens.addAll(next.getLookupCache().getFirstPossibleTokens());
                        if (!next.isOptional()) {
                            parentElementType = null;
                            break;
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
        }
        return nextPossibleTokens;
    }
}
