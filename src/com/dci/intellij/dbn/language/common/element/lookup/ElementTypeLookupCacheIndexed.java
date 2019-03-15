package com.dci.intellij.dbn.language.common.element.lookup;

import com.dci.intellij.dbn.common.index.IndexedContainer;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import com.dci.intellij.dbn.language.common.SharedTokenTypeBundle;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.ElementTypeBundle;
import com.dci.intellij.dbn.language.common.element.IdentifierElementType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.impl.WrappingDefinition;
import gnu.trove.THashSet;

import java.util.Set;

public abstract class ElementTypeLookupCacheIndexed<T extends ElementType> extends ElementTypeLookupCache<T> {

    private IndexedContainer<LeafElementType> allPossibleLeafs;
    Set<LeafElementType> firstPossibleLeafs;
    Set<LeafElementType> firstRequiredLeafs;

    private IndexedContainer<TokenType> allPossibleTokens;
    private Set<TokenType> firstPossibleTokens;
    private Set<TokenType> firstRequiredTokens;
    private Boolean startsWithIdentifier;

    ElementTypeLookupCacheIndexed(T elementType) {
        super(elementType);
        if (!elementType.isLeaf()) {
            allPossibleLeafs = new IndexedContainer<>();
            firstPossibleLeafs = new THashSet<>();
            firstRequiredLeafs = new THashSet<>();
            allPossibleTokens = new IndexedContainer<>();
            firstPossibleTokens = new THashSet<>();
            firstRequiredTokens = new THashSet<>();
        }
    }

    @Override
    public void cleanup() {
        super.cleanup();
        CollectionUtil.compact(allPossibleLeafs);
        CollectionUtil.compact(firstPossibleLeafs);
        CollectionUtil.compact(firstRequiredLeafs);

        CollectionUtil.compact(allPossibleTokens);
        CollectionUtil.compact(firstPossibleTokens);
        CollectionUtil.compact(firstRequiredTokens);
    }

    @Override
    public boolean isFirstPossibleToken(TokenType tokenType) {
        return firstPossibleTokens.contains(tokenType);
    }

    @Override
    public boolean isFirstRequiredToken(TokenType tokenType) {
        return firstRequiredTokens.contains(tokenType);
    }

    private ElementTypeBundle getBundle() {
        return elementType.getElementBundle();
    }


    @Override
    public boolean containsToken(TokenType tokenType) {
        return allPossibleTokens != null && allPossibleTokens.contains(tokenType);
    }

    @Override
    public boolean containsLeaf(LeafElementType elementType) {
        return allPossibleLeafs != null && allPossibleLeafs.contains(elementType);
    }

    @Override
    public Set<TokenType> getFirstRequiredTokens() {
        return firstRequiredTokens;
    }

    @Override
    public Set<TokenType> getFirstPossibleTokens() {
        return firstPossibleTokens;
    }

    @Override
    public Set<LeafElementType> getFirstRequiredLeafs() {
        return firstRequiredLeafs;
    }
    @Override
    public Set<LeafElementType> getFirstPossibleLeafs() {
        return firstPossibleLeafs;
    }



    @Override
    public boolean couldStartWithLeaf(LeafElementType leafElementType) {
        return firstPossibleLeafs.contains(leafElementType);
    }

    @Override
    public boolean couldStartWithToken(TokenType tokenType) {
        return firstPossibleTokens.contains(tokenType);
    }

    @Override
    public boolean shouldStartWithLeaf(LeafElementType leafElementType) {
        return firstRequiredLeafs.contains(leafElementType);
    }

    @Override
    public void registerLeaf(LeafElementType leaf, ElementType source) {
        boolean initAllElements = initAllElements(leaf);
        boolean initAsFirstPossibleLeaf = initAsFirstPossibleLeaf(leaf, source);
        boolean initAsFirstRequiredLeaf = initAsFirstRequiredLeaf(leaf, source);

        // register first possible leafs
        ElementTypeLookupCache lookupCache = leaf.getLookupCache();
        if (initAsFirstPossibleLeaf) {
            firstPossibleLeafs.add(leaf);
            lookupCache.collectFirstPossibleTokens(firstPossibleTokens);
        }

        // register first required leafs
        if (initAsFirstRequiredLeaf) {
            firstRequiredLeafs.add(leaf);
            lookupCache.collectFirstPossibleTokens(firstRequiredTokens);
        }

        if (initAllElements) {
            // register all possible leafs
            allPossibleLeafs.put(leaf);

            // register all possible tokens
            if (leaf instanceof IdentifierElementType) {
                SharedTokenTypeBundle sharedTokenTypes = getSharedTokenTypes();
                allPossibleTokens.put(sharedTokenTypes.getIdentifier());
                allPossibleTokens.put(sharedTokenTypes.getQuotedIdentifier());
            } else {
                allPossibleTokens.put(leaf.getTokenType());
            }
        }

        if (initAsFirstPossibleLeaf || initAsFirstRequiredLeaf || initAllElements) {
            // walk the tree up
            registerLeafInParent(leaf);
        }
    }

    abstract boolean initAsFirstPossibleLeaf(LeafElementType leaf, ElementType source);
    abstract boolean initAsFirstRequiredLeaf(LeafElementType leaf, ElementType source);
    private boolean initAllElements(LeafElementType leafElementType) {
        return leafElementType != elementType && !allPossibleLeafs.contains(leafElementType);
    }

    protected void registerLeafInParent(LeafElementType leaf) {
        super.registerLeaf(leaf, null);
    }

    @Override
    public boolean startsWithIdentifier() {
        if (startsWithIdentifier == null) {
            synchronized (this) {
                if (startsWithIdentifier == null) {
                    startsWithIdentifier = checkStartsWithIdentifier();
                }
            }
        }
        return startsWithIdentifier;
    }

    protected abstract boolean checkStartsWithIdentifier();

    protected boolean isWrapperBeginLeaf(LeafElementType leaf) {
        WrappingDefinition wrapping = elementType.getWrapping();
        if (wrapping != null) {
            if (wrapping.getBeginElementType() == leaf) {
                return true;
            }
        }
        return false;
    }
}
