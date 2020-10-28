package com.dci.intellij.dbn.language.common.resolve;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectPsiElement;
import com.intellij.openapi.diagnostic.Attachment;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public class AliasObjectResolver extends UnderlyingObjectResolver{
    private static final Logger LOGGER = LoggerFactory.createLogger();

    private static final AliasObjectResolver INSTANCE = new AliasObjectResolver();

    public static AliasObjectResolver getInstance() {
        return INSTANCE;
    }

    private AliasObjectResolver() {
        super("ALIAS_RESOLVER");
    }

    @Override
    @Nullable
    protected DBObject resolve(IdentifierPsiElement identifierPsiElement, int recursionCheck) {
        if (recursionCheck > 10) {
            DBLanguagePsiFile psiFile = identifierPsiElement.getFile();
            LOGGER.error("Recursive alias lookup", new Attachment(psiFile.getVirtualFile().getPath(), psiFile.getText()));
            return null;
        }

        BasePsiElement aliasedObject = PsiUtil.resolveAliasedEntityElement(identifierPsiElement);
        if (aliasedObject != null) {
            if (aliasedObject.isVirtualObject()) {
                return aliasedObject.getUnderlyingObject();
            } else if (aliasedObject instanceof IdentifierPsiElement) {
                IdentifierPsiElement aliasedPsiElement = (IdentifierPsiElement) aliasedObject;
                PsiElement underlyingPsiElement = aliasedPsiElement.resolve();
                if (underlyingPsiElement instanceof DBObjectPsiElement) {
                    DBObjectPsiElement objectPsiElement = (DBObjectPsiElement) underlyingPsiElement;
                    return objectPsiElement.ensureObject();
                }

                if (underlyingPsiElement instanceof IdentifierPsiElement && underlyingPsiElement != identifierPsiElement) {
                    IdentifierPsiElement underlyingIdentifierPsiElement = (IdentifierPsiElement) underlyingPsiElement;
                    if (underlyingIdentifierPsiElement.isAlias() && underlyingIdentifierPsiElement.isDefinition()) {
                        recursionCheck++;
                        return resolve(underlyingIdentifierPsiElement, recursionCheck);
                    }
                }
            }
        }
        return null;
    }
}
