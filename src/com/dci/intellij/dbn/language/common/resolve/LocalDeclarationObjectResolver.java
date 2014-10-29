package com.dci.intellij.dbn.language.common.resolve;

import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.language.common.psi.NamedPsiElement;
import com.dci.intellij.dbn.language.common.psi.lookup.IdentifierLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.PsiLookupAdapter;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;

public class LocalDeclarationObjectResolver extends UnderlyingObjectResolver{
    private static final LocalDeclarationObjectResolver INSTANCE = new LocalDeclarationObjectResolver();

    public static LocalDeclarationObjectResolver getInstance() {
        return INSTANCE;
    }

    private LocalDeclarationObjectResolver() {
        super("LOCAL_DECLARATION_RESOLVER");
    }

    @Override
    public DBObject resolve(IdentifierPsiElement identifierPsiElement) {
        NamedPsiElement enclosingNamedPsiElement = identifierPsiElement.findEnclosingNamedPsiElement();
        PsiLookupAdapter lookupAdapter = new IdentifierLookupAdapter(identifierPsiElement, null, null, DBObjectType.TYPE, null);
        BasePsiElement underlyingObjectCandidate = lookupAdapter.findInElement(enclosingNamedPsiElement);

        if (underlyingObjectCandidate == null) {
            lookupAdapter = new IdentifierLookupAdapter(identifierPsiElement, null, null, DBObjectType.DATASET, null);
            underlyingObjectCandidate = lookupAdapter.findInElement(enclosingNamedPsiElement);
        }

        return underlyingObjectCandidate == null ? null : underlyingObjectCandidate.resolveUnderlyingObject() ;
    }
}
