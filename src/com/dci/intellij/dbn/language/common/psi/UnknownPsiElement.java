package com.dci.intellij.dbn.language.common.psi;

import com.dci.intellij.dbn.code.common.style.formatting.FormattingAttributes;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.lookup.PsiLookupAdapter;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.lang.ASTNode;

import java.util.Set;

public class UnknownPsiElement extends BasePsiElement {
    public UnknownPsiElement(ASTNode astNode, ElementType elementType) {
        super(astNode, elementType);
    }

    @Override
    public FormattingAttributes getFormattingAttributes() {
        return FormattingAttributes.NO_ATTRIBUTES;
    }

    public int approximateLength() {
        return getTextLength();
    }

    public BasePsiElement lookupPsiElement(PsiLookupAdapter lookupAdapter, int scopeCrossCount) {return null;}
    public Set<BasePsiElement> collectPsiElements(PsiLookupAdapter lookupAdapter, Set<BasePsiElement> bucket, int scopeCrossCount) {return bucket;}


    public void collectExecVariablePsiElements(Set<ExecVariablePsiElement> bucket) {}
    public void collectSubjectPsiElements(Set<BasePsiElement> bucket) {}
    public NamedPsiElement lookupNamedPsiElement(String id) {return null;}
    public BasePsiElement lookupFirstPsiElement(ElementTypeAttribute attribute) {return null;}
    public BasePsiElement lookupFirstLeafPsiElement() {return null;}
    public BasePsiElement lookupPsiElementByAttribute(ElementTypeAttribute attribute) {return null;}

    public BasePsiElement lookupPsiElementBySubject(ElementTypeAttribute attribute, CharSequence subjectName, DBObjectType subjectType) {return null;}

    public boolean hasErrors() {
        return true;
    }

    @Override
    public boolean equals(BasePsiElement basePsiElement) {
        if (this == basePsiElement) {
            return true;
        } else {
            return basePsiElement instanceof UnknownPsiElement && basePsiElement.getText().equalsIgnoreCase(getText());
        }
    }

    @Override
    public boolean matches(BasePsiElement basePsiElement) {
        if (this == basePsiElement) {
            return true;
        } else {
            return basePsiElement instanceof UnknownPsiElement;
        }

    }

    public String toString() {
        return getElementType().getDebugName();

    }
}
