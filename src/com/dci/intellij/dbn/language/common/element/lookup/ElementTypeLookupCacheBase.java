package com.dci.intellij.dbn.language.common.element.lookup;

import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.SharedTokenTypeBundle;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.IterationElementType;
import com.dci.intellij.dbn.language.common.element.SequenceElementType;
import com.dci.intellij.dbn.language.common.element.TokenElementType;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeRef;
import gnu.trove.THashSet;

import java.util.Set;

public abstract class ElementTypeLookupCacheBase<T extends ElementType> implements ElementTypeLookupCache<T> {
    protected Set<TokenType> nextPossibleTokens;
    protected T elementType;

    public ElementTypeLookupCacheBase(T elementType) {
        this.elementType = elementType;
    }

    @Override
    public T getElementType() {
        return elementType;
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
                    nextPossibleTokens = new THashSet<TokenType>();
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
                                    child.getLookupCache().addFirstPossibleTokens(nextPossibleTokens);
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
    public void addFirstPossibleTokens(Set<TokenType> target) {
        target.addAll(getFirstPossibleTokens());
    }
}
