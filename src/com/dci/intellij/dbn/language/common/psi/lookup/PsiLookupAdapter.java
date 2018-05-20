package com.dci.intellij.dbn.language.common.psi.lookup;

import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public abstract class PsiLookupAdapter {
    private boolean assertResolved = false;

    public void setAssertResolved(boolean assertResolved) {
        this.assertResolved = assertResolved;
    }

    public boolean isAssertResolved() {
        return assertResolved;
    }

    public abstract boolean matches(BasePsiElement element);

    public abstract boolean accepts(BasePsiElement element);

    public final BasePsiElement findInParentScopeOf(final BasePsiElement source) {
        //System.out.println(this);
        LookupScopeVisitor finder = new LookupScopeVisitor() {
            protected BasePsiElement performLookup(BasePsiElement scope) {
                BasePsiElement result = scope.findPsiElement(PsiLookupAdapter.this, 10);
                return result == null || result == source ? null : result;
            }
        };
        return finder.visit(source);
    }

    public final BasePsiElement findInScope(@NotNull BasePsiElement scope) {
        return scope.findPsiElement(this, 100);
    }

    public final BasePsiElement findInElement(@NotNull BasePsiElement element) {
        return element.findPsiElement(this, 100);
    }


    public final Set<BasePsiElement> collectInParentScopeOf(@NotNull BasePsiElement source) {
        return collectInParentScopeOf(source, null);
    }


    public final Set<BasePsiElement> collectInParentScopeOf(@NotNull BasePsiElement source, Set<BasePsiElement> bucket) {
        CollectScopeVisitor collector = new CollectScopeVisitor() {
            protected Set<BasePsiElement> performCollect(BasePsiElement scope) {
                return scope.collectPsiElements(PsiLookupAdapter.this, getResult(), 1);
            }

        };
        collector.setResult(bucket);
        return collector.visit(source);
    }

    @Nullable
    public final Set<BasePsiElement> collectInScope(@NotNull BasePsiElement scope, @Nullable Set<BasePsiElement> bucket) {
        BasePsiElement collectScope = scope.isScopeBoundary() ? scope : scope.findEnclosingScopePsiElement();
        if (collectScope != null) {
            return collectScope.collectPsiElements(this, bucket, 100);
        }
        return bucket;
    }

    @Nullable
    public final Set<BasePsiElement> collectInElement(@NotNull BasePsiElement element, @Nullable Set<BasePsiElement> bucket) {
        return element.collectPsiElements(this, bucket, 100);
    }
}
