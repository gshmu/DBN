package com.dci.intellij.dbn.language.common.element.lookup;

import com.dci.intellij.dbn.common.index.IndexedContainer;
import com.dci.intellij.dbn.language.common.SharedTokenTypeBundle;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeBase;
import com.dci.intellij.dbn.language.common.element.impl.IdentifierElementType;
import com.dci.intellij.dbn.language.common.element.impl.LeafElementType;
import com.dci.intellij.dbn.language.common.element.impl.WrappingDefinition;
import gnu.trove.THashSet;

import java.util.Set;

import static com.dci.intellij.dbn.common.util.CollectionUtil.compact;

public abstract class ElementTypeLookupCacheIndexed<T extends ElementTypeBase> extends ElementTypeLookupCache<T> {

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
        compact(allPossibleLeafs);
        firstPossibleLeafs = compact(firstPossibleLeafs);
        firstRequiredLeafs = compact(firstRequiredLeafs);

        compact(allPossibleTokens);
        firstPossibleTokens = compact(firstPossibleTokens);
        firstRequiredTokens = compact(firstRequiredTokens);
    }

    @Override
    public boolean isFirstPossibleToken(TokenType tokenType) {
        return firstPossibleTokens.contains(tokenType);
    }

    @Override
    public boolean isFirstRequiredToken(TokenType tokenType) {
        return firstRequiredTokens.contains(tokenType);
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
    public void registerLeaf(LeafElementType leaf, ElementTypeBase source) {
        boolean initAllElements = initAllElements(leaf);
        boolean initAsFirstPossibleLeaf = initAsFirstPossibleLeaf(leaf, source);
        boolean initAsFirstRequiredLeaf = initAsFirstRequiredLeaf(leaf, source);

        // register first possible leafs
        ElementTypeLookupCache lookupCache = leaf.lookupCache;
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
                allPossibleTokens.put(leaf.tokenType);
            }
        }

        if (initAsFirstPossibleLeaf || initAsFirstRequiredLeaf || initAllElements) {
            // walk the tree up
            registerLeafInParent(leaf);
        }
    }

    abstract boolean initAsFirstPossibleLeaf(LeafElementType leaf, ElementTypeBase source);
    abstract boolean initAsFirstRequiredLeaf(LeafElementType leaf, ElementTypeBase source);
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

    boolean isWrapperBeginLeaf(LeafElementType leaf) {
        WrappingDefinition wrapping = elementType.getWrapping();
        if (wrapping != null) {
            if (wrapping.getBeginElementType() == leaf) {
                return true;
            }
        }
        return false;
    }
}
