package com.dci.intellij.dbn.language.common.resolve;

import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.object.common.DBObject;
import com.intellij.psi.PsiElement;

public class AliasObjectResolver extends UnderlyingObjectResolver{
    private static final AliasObjectResolver INSTANCE = new AliasObjectResolver();

    public static AliasObjectResolver getInstance() {
        return INSTANCE;
    }

    private AliasObjectResolver() {
        super("ALIAS_RESOLVER");
    }

    @Override
    public DBObject resolve(IdentifierPsiElement identifierPsiElement) {
        BasePsiElement aliasedObject = PsiUtil.resolveAliasedEntityElement(identifierPsiElement);
        if (aliasedObject != null) {
            if (aliasedObject.isVirtualObject()) {
                return aliasedObject.resolveUnderlyingObject();
            } else if (aliasedObject instanceof IdentifierPsiElement) {
                identifierPsiElement = (IdentifierPsiElement) aliasedObject;
                PsiElement underlyingPsiElement = identifierPsiElement.resolve();
                if (underlyingPsiElement instanceof DBObject) {
                    return (DBObject) underlyingPsiElement;
                }

                if (underlyingPsiElement instanceof IdentifierPsiElement) {
                    IdentifierPsiElement underlyingIdentifierPsiElement = (IdentifierPsiElement) underlyingPsiElement;
                    if (underlyingIdentifierPsiElement.isAlias() && underlyingIdentifierPsiElement.isDefinition()) {
                        return INSTANCE.resolve(underlyingIdentifierPsiElement);
                    }
                }
            }
        }
        return null;
    }
}
