package com.dci.intellij.dbn.language.common.element;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.code.common.style.formatting.FormattingDefinition;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.TokenTypeCategory;
import com.dci.intellij.dbn.language.common.element.impl.WrappingDefinition;
import com.dci.intellij.dbn.language.common.element.lookup.ElementTypeLookupCache;
import com.dci.intellij.dbn.language.common.element.parser.ElementTypeParser;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttributesBundle;
import com.dci.intellij.dbn.language.common.psi.ChameleonPsiElement;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.ILazyParseableElementType;

public class ChameleonElementType extends ILazyParseableElementType implements ElementType, TokenType {
    public ChameleonElementType(DBLanguageDialect language) {
        super("chameleon (" + language.getDisplayName() + ")", language);
    }

    public String getId() {
        return "";
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

    public String getDescription() {
        return getDebugName();
    }

    public String getDebugName() {
        return toString();
    }

    public Icon getIcon() {
        return null;
    }

    public ElementType getParent() {
        return null;
    }

    public ElementTypeLookupCache getLookupCache() {
        return null;
    }

    public ElementTypeParser getParser() {
        return null;
    }

    @Override
    public FormattingDefinition getFormatting() {
        return null;
    }

    @Override
    public void setDefaultFormatting(FormattingDefinition defaults) {
    }

    @Override
    public ElementTypeAttributesBundle getAttributes() {
        return null;
    }

    @Override
    public WrappingDefinition getWrapping() {
        return null;
    }

    @Override
    @Nullable
    public String getBranch() {
        return null;
    }

    public boolean is(ElementTypeAttribute attribute) {
        return false;
    }

    public boolean isLeaf() {
        return false;
    }

    public boolean isVirtualObject() {
        return false;
    }

    public boolean isVirtualObjectInsideLookup() {
        return false;
    }

    public DBObjectType getVirtualObjectType() {
        return null;
    }

    public PsiElement createPsiElement(ASTNode astNode) {
        return new ChameleonPsiElement(astNode, this);
    }

    public String getResolveScopeId() {
        return null;
    }

    public ElementTypeBundle getElementBundle() {
        return null;
    }

    public void registerVirtualObject(DBObjectType objectType) {
    }


    public int getIdx() {
        return 0;
    }

    public String getValue() {
        return null;
    }

    public String getTypeName() {
        return null;
    }

    public boolean isSuppressibleReservedWord() {
        return false;
    }

    public boolean isIdentifier() {
        return false;
    }

    public boolean isVariable() {
        return false;
    }

    public boolean isQuotedIdentifier() {
        return false;
    }

    public boolean isKeyword() {
        return false;
    }

    public boolean isFunction() {
        return false;
    }

    public boolean isParameter() {
        return false;
    }

    public boolean isDataType() {
        return false;
    }

    public boolean isCharacter() {
        return false;
    }

    public boolean isOperator() {
        return false;
    }

    public boolean isChameleon() {
        return true;
    }

    public boolean isReservedWord() {
        return false;
    }

    public boolean isParserLandmark() {
        return false;
    }

    public TokenTypeCategory getCategory() {
        return null;
    }

    @Override
    public boolean isOneOf(TokenType... tokenTypes) {
        for (TokenType tokenType : tokenTypes) {
            if (this == tokenType) return true;
        }
        return false;
    }

    @Override
    public boolean matches(TokenType tokenType) {
        return this.equals(tokenType);
    }

}
