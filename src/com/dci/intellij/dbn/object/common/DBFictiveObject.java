package com.dci.intellij.dbn.object.common;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DBFictiveObject extends DBObjectImpl implements PsiReference {
    private String name;
    private DBObjectType objectType;
    public DBFictiveObject(DBObjectType objectType, String name) {
        super(null, name);
        this.objectType = objectType;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    protected String initObject(ResultSet resultSet) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getQualifiedNameWithType() {
        return getName();
    }

    @Override
    public DBObjectType getObjectType() {
        return objectType;
    }

    @Override
    @NotNull
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return EMPTY_TREE_NODE_LIST;
    }

    @Override
    public void navigate(boolean requestFocus) {

    }

    /*********************************************************
     *                       PsiReference                    *
     *********************************************************/
    @Override
    @NotNull
    public PsiElement getElement() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public TextRange getRangeInElement() {
        return new TextRange(0, name.length());
    }

    @Override
    public PsiElement resolve() {
        return null;
    }

    @Override
    @NotNull
    public String getCanonicalText() {
        return name;
    }

    @Override
    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        return null;
    }

    @Override
    public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
        return null;
    }

    @Override
    public boolean isReferenceTo(PsiElement element) {
        return false;
    }

    @Override
    @NotNull
    public Object[] getVariants() {
        return new Object[0];
    }

    @Override
    public boolean isSoft() {
        return false;
    }

}
