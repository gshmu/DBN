package com.dci.intellij.dbn.language.sql.structure;

import javax.swing.Icon;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.ChameleonPsiElement;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiElement;

public class SQLStructureViewElement implements StructureViewTreeElement {
    PsiElement psiElement;

    SQLStructureViewElement(PsiElement psiElement) {
        this.psiElement = psiElement;
    }

    public Object getValue() {
        return psiElement;
    }

    @NotNull
    public ItemPresentation getPresentation() {
        if (psiElement instanceof BasePsiElement) return (ItemPresentation) psiElement;
        return new ItemPresentation() {
            public String getPresentableText() {
                if (psiElement instanceof DBLanguagePsiFile) {
                    DBLanguagePsiFile file = (DBLanguagePsiFile) psiElement;
                    return file.getName();
                }
                if (psiElement instanceof ChameleonPsiElement) {
                    ChameleonPsiElement chameleonPsiElement = (ChameleonPsiElement) psiElement;
                    //return chameleonPsiElement.getLanguage().getDisplayName() + " block";
                    // todo make this dynamic
                    return "PL/SQL block";
                }
                return psiElement.getText();
            }

            @Nullable
            public String getLocationString() {
                return null;
            }

            @Nullable
            public Icon getIcon(boolean open) {
                return psiElement.isValid() ? psiElement.getIcon(open ? Iconable.ICON_FLAG_OPEN : Iconable.ICON_FLAG_CLOSED) : null;
            }

            @Nullable
            public TextAttributesKey getTextAttributesKey() {
                return null;
            }
        };
    }

    @NotNull/**/
    public StructureViewTreeElement[] getChildren() {
        List<SQLStructureViewElement> elements = new ArrayList<SQLStructureViewElement>();
        getChildren(psiElement, elements);
        return elements.toArray(new StructureViewTreeElement[elements.size()]);
    }

    private void getChildren(PsiElement parent, List<SQLStructureViewElement> elements) {
        PsiElement child = parent.getFirstChild();
        while (child != null) {
            if (child instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) child;
                if (basePsiElement.is(ElementTypeAttribute.STRUCTURE)) {
                    elements.add(new SQLStructureViewElement(child));
                } else {
                    getChildren(basePsiElement, elements);
                }
            }
            if (child instanceof ChameleonPsiElement) {
                elements.add(new SQLStructureViewElement(child));
            }
            child = child.getNextSibling();
        }
    }

    public void navigate(boolean requestFocus) {
        if (psiElement instanceof NavigationItem) {
            NavigationItem navigationItem = (NavigationItem) psiElement;
            navigationItem.navigate(requestFocus);
        }
    }

    public boolean canNavigate() {
        return true;
    }

    public boolean canNavigateToSource() {
        return true;
    }
}

