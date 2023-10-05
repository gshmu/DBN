package com.dci.intellij.dbn.language.common.psi.lookup;

import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.element.util.IdentifierCategory;
import com.dci.intellij.dbn.language.common.element.util.IdentifierType;
import com.dci.intellij.dbn.language.common.psi.LeafPsiElement;
import com.dci.intellij.dbn.object.type.DBObjectType;

public class SimpleAliasLookupAdapter extends IdentifierLookupAdapter {
    public SimpleAliasLookupAdapter(LeafPsiElement lookupIssuer, DBObjectType objectType) {
        super(lookupIssuer, IdentifierType.ALIAS, IdentifierCategory.ALL, objectType, null);
    }

    public SimpleAliasLookupAdapter(LeafPsiElement lookupIssuer, DBObjectType objectType, CharSequence identifierName) {
        super(lookupIssuer, IdentifierType.ALIAS, IdentifierCategory.ALL, objectType, identifierName);
    }

    public SimpleAliasLookupAdapter(LeafPsiElement lookupIssuer, IdentifierCategory identifierCategory, DBObjectType objectType) {
        super(lookupIssuer, IdentifierType.ALIAS, identifierCategory, objectType, null);
    }

    public SimpleAliasLookupAdapter(LeafPsiElement lookupIssuer, IdentifierCategory identifierCategory, DBObjectType objectType, CharSequence identifierName) {
        super(lookupIssuer, IdentifierType.ALIAS, identifierCategory, objectType, identifierName);
    }

    public SimpleAliasLookupAdapter(LeafPsiElement lookupIssuer, IdentifierCategory identifierCategory, DBObjectType objectType, CharSequence identifierName, ElementTypeAttribute attribute) {
        super(lookupIssuer, IdentifierType.ALIAS, identifierCategory, objectType, identifierName, attribute);
    }
}
