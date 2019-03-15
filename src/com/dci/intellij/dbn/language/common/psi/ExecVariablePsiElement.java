package com.dci.intellij.dbn.language.common.psi;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.language.common.element.impl.ExecVariableElementType;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.lookup.PsiLookupAdapter;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Set;

public class ExecVariablePsiElement extends LeafPsiElement<ExecVariableElementType> {
    public ExecVariablePsiElement(ASTNode astNode, ExecVariableElementType elementType) {
        super(astNode, elementType);
    }

    @Override
    @Nullable
    public BasePsiElement findPsiElement(PsiLookupAdapter lookupAdapter, int scopeCrossCount) {return null;}

    @Override
    @Nullable
    public Set<BasePsiElement> collectPsiElements(PsiLookupAdapter lookupAdapter, @Nullable Set<BasePsiElement> bucket, int scopeCrossCount) {return bucket;}


    @Override
    public void collectExecVariablePsiElements(@NotNull Set<ExecVariablePsiElement> bucket) { bucket.add(this);}

    @Override
    public void collectSubjectPsiElements(@NotNull Set<IdentifierPsiElement> bucket) {}

    @Override
    public NamedPsiElement findNamedPsiElement(String id) {return null;}

    @Override
    public BasePsiElement findPsiElementBySubject(ElementTypeAttribute attribute, CharSequence subjectName, DBObjectType subjectType) {return null;}


    /*********************************************************
     *                       PsiReference                    *
     *********************************************************/
    @Override
    public boolean isReferenceTo(@NotNull PsiElement element) {
        return false;
    }

    @Override
    public boolean isSoft() {
        return false;
    }

    /*********************************************************
     *                       ItemPresentation                *
     *********************************************************/
    @Override
    public String getPresentableText() {
        return elementType.tokenType.getValue();
    }

    @Override
    @Nullable
    public String getLocationString() {
        return null;
    }

    @Override
    @Nullable
    public Icon getIcon(boolean open) {
        return null;
    }

    @Override
    @Nullable
    public TextAttributesKey getTextAttributesKey() {
        return null;
    }

    @Override
    public boolean hasErrors() {
        return false;
    }

    @Override
    public boolean matches(BasePsiElement basePsiElement, MatchType matchType) {
        if (basePsiElement instanceof ExecVariablePsiElement) {
            ExecVariablePsiElement execVariablePsiElement = (ExecVariablePsiElement) basePsiElement;
            return matchType == MatchType.SOFT || StringUtil.equalsIgnoreCase(execVariablePsiElement.getChars(), getChars());
        }
        return false;
    }
}
