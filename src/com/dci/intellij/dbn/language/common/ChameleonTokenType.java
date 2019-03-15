package com.dci.intellij.dbn.language.common;

import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.ElementTypeBundle;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.impl.WrappingDefinition;
import com.dci.intellij.dbn.language.common.element.lookup.ElementTypeLookupCache;
import com.dci.intellij.dbn.language.common.element.parser.Branch;
import com.dci.intellij.dbn.language.common.element.parser.ElementTypeParser;
import com.dci.intellij.dbn.language.common.element.path.BasicPathNode;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ChameleonTokenType extends SimpleTokenType implements ElementType {
    private DBLanguageDialect injectedLanguage;
    public ChameleonTokenType(@Nullable DBLanguageDialect hostLanguage, DBLanguageDialect injectedLanguage) {
        super(injectedLanguage.getID() + " block", hostLanguage);
        this.injectedLanguage = injectedLanguage;
    }

    @NotNull
    @Override
    public DBLanguage getLanguage() {
        return getLanguageDialect().getBaseLanguage();
    }

    @Override
    public DBLanguageDialect getLanguageDialect() {
        return (DBLanguageDialect) super.getLanguage();
    }

    public DBLanguageDialect getInjectedLanguage() {
        return injectedLanguage;
    }

    @Override
    public String getDebugName() {
        return toString();
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public ElementType getParent() {
        return null;
    }

    @Override
    public ElementTypeLookupCache getLookupCache() {
        return null;
    }

    @Override
    public ElementTypeParser getParser() {
        return null;
    }

    @Override
    public boolean is(ElementTypeAttribute attribute) {
        return false;
    }

    @Override
    public boolean set(ElementTypeAttribute status, boolean value) {
        throw new AbstractMethodError("Operation not allowed");
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public boolean isVirtualObject() {
        return false;
    }

    @Override
    public DBObjectType getVirtualObjectType() {
        return null;
    }

    @Override
    public PsiElement createPsiElement(ASTNode astNode) {
        return new ASTWrapperPsiElement(astNode);
    }

    @Override
    public ElementTypeBundle getElementBundle() {
        return null;
    }

    @Override
    public WrappingDefinition getWrapping() {
        return null;
    }

    @Override
    public boolean isWrappingBegin(LeafElementType elementType) {
        return false;
    }

    @Override
    public boolean isWrappingEnd(LeafElementType elementType) {
        return false;
    }

    @Override
    public boolean isWrappingBegin(TokenType tokenType) {
        return false;
    }

    @Override
    public boolean isWrappingEnd(TokenType tokenType) {
        return false;
    }

    @Nullable
    @Override
    public Branch getBranch() {
        return null;
    }

    @Override
    public boolean isScopeDemarcation() {
        return true;
    }

    @Override
    public boolean isScopeIsolation() {
        return true;
    }

    @Override
    public TokenType getTokenType() {
        return null;
    }

    @Override
    public int getIndexInParent(BasicPathNode pathNode) {
        return 0;
    }
}
