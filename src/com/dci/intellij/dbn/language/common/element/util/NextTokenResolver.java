package com.dci.intellij.dbn.language.common.element.util;

import com.dci.intellij.dbn.common.index.IndexContainer;
import com.dci.intellij.dbn.common.util.Commons;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.cache.ElementTypeLookupCache;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeBase;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeRef;
import com.dci.intellij.dbn.language.common.element.impl.IterationElementType;
import com.dci.intellij.dbn.language.common.element.impl.NamedElementType;
import com.dci.intellij.dbn.language.common.element.impl.SequenceElementType;
import com.dci.intellij.dbn.language.common.element.impl.TokenElementType;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public final class NextTokenResolver {
    private final ElementType source;
    private final Set<NamedElementType> visited = new THashSet<>();
    private IndexContainer<TokenType> bucket;

    private NextTokenResolver(ElementType source) {
        this.source = source;
    }

    public static NextTokenResolver from(ElementTypeBase source) {
        return new NextTokenResolver(source);
    }

    public IndexContainer<TokenType> resolve() {
        if (source instanceof NamedElementType) {
            visit((NamedElementType) source);
        } else {
            visitElement(source.getParent(), source);
        }
        return bucket;
    }

    private void visit(@NotNull NamedElementType element) {
        if (!visited.contains(element)) {
            visited.add(element);
            Set<ElementTypeBase> parents = element.getParents();
            for (ElementTypeBase parent : parents) {
                visitElement(parent, element);
            }
        }
    }

    private void visitElement(ElementType parent, ElementType child) {
        while (parent != null) {
            if (parent instanceof SequenceElementType) {
                parent = visitSequence((SequenceElementType) parent, child);

            } else if (parent instanceof IterationElementType) {
                visitIteration((IterationElementType) parent);
            }

            if (parent != null) {
                child = parent;
                parent = child.getParent();
                if (child instanceof NamedElementType) {
                    visit((NamedElementType) child);
                }
            }
        }
    }

    private void visitIteration(IterationElementType parent) {
        TokenElementType[] separatorTokens = parent.getSeparatorTokens();
        if (separatorTokens != null) {
            ensureBucket();
            for (TokenElementType separatorToken : separatorTokens) {
                bucket.add(separatorToken.getTokenType());
            }
        }
    }

    @Nullable
    private ElementType visitSequence(SequenceElementType parent, ElementType element) {
        int elementsCount = parent.getChildCount();
        int index = parent.indexOf(element, 0) + 1;

        if (index < elementsCount) {
            ElementTypeRef child = parent.getChild(index);
            while (child != null) {
                ensureBucket();
                ElementTypeLookupCache lookupCache = child.getLookupCache();
                lookupCache.captureFirstPossibleTokens(bucket);
                if (!child.optional) {
                    parent = null;
                    break;
                }
                child = child.getNext();
            }
        }
        return parent;
    }

    private void ensureBucket() {
        bucket = Commons.nvl(bucket, () -> new IndexContainer<>());
    }
}
