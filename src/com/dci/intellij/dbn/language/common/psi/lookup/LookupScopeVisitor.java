package com.dci.intellij.dbn.language.common.psi.lookup;

import com.dci.intellij.dbn.language.common.psi.BasePsiElement;

@Deprecated
public abstract class LookupScopeVisitor extends PsiScopeVisitor {
    private BasePsiElement result;

    @Override
    protected final boolean visitScope(BasePsiElement scope) {
        result = performLookup(scope);
        return result != null;
    }

    public BasePsiElement getResult() {
        return result;
    }

    public void setResult(BasePsiElement result) {
        this.result = result;
    }

    protected abstract BasePsiElement performLookup(BasePsiElement scope);
}
