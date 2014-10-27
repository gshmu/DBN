package com.dci.intellij.dbn.language.common.psi.lookup;

import com.dci.intellij.dbn.language.common.element.util.IdentifierCategory;
import com.dci.intellij.dbn.language.common.element.util.IdentifierType;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.LeafPsiElement;
import com.dci.intellij.dbn.object.common.DBObjectType;

public class VirtualObjectLookupAdapter extends IdentifierLookupAdapter {
    private DBObjectType parentObjectType;

    public VirtualObjectLookupAdapter(LeafPsiElement lookupIssuer, DBObjectType parentObjectType, DBObjectType objectType) {
        super(lookupIssuer, IdentifierType.OBJECT, IdentifierCategory.ALL, objectType, null);
        this.parentObjectType = parentObjectType;
    }

    @Override
    public boolean accepts(BasePsiElement element) {
        DBObjectType virtualObjectType = element.getElementType().getVirtualObjectType();
        return parentObjectType == null || virtualObjectType == null || !parentObjectType.matches(virtualObjectType);
    }

    @Override
    public boolean matches(BasePsiElement basePsiElement) {
        DBObjectType virtualObjectType = basePsiElement.getElementType().getVirtualObjectType();
        return virtualObjectType != null && virtualObjectType.matches(getObjectType());
    }
}
