package com.dci.intellij.dbn.language.common.element;

import com.dci.intellij.dbn.code.common.style.formatting.FormattingDefinition;
import com.dci.intellij.dbn.common.index.Indexable;
import com.dci.intellij.dbn.common.property.PropertyHolder;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.impl.LeafElementType;
import com.dci.intellij.dbn.language.common.element.impl.WrappingDefinition;
import com.dci.intellij.dbn.language.common.element.lookup.ElementTypeLookupCache;
import com.dci.intellij.dbn.language.common.element.parser.Branch;
import com.dci.intellij.dbn.language.common.element.parser.ElementTypeParser;
import com.dci.intellij.dbn.language.common.element.path.BasicPathNode;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public interface ElementType extends Indexable, PropertyHolder<ElementTypeAttribute>{

    String getId();

    String getDescription();

    String getDebugName();

    Icon getIcon();

    ElementType getParent();

    DBLanguage getLanguage();

    DBLanguageDialect getLanguageDialect();

    ElementTypeLookupCache getLookupCache();

    ElementTypeParser getParser();

    boolean isLeaf();

    boolean isVirtualObject();

    DBObjectType getVirtualObjectType();

    PsiElement createPsiElement(ASTNode astNode);

    ElementTypeBundle getElementBundle();

    FormattingDefinition getFormatting();

    void setDefaultFormatting(FormattingDefinition defaults);

    WrappingDefinition getWrapping();

    boolean isWrappingBegin(LeafElementType elementType);

    boolean isWrappingBegin(TokenType tokenType);

    boolean isWrappingEnd(LeafElementType elementType);

    boolean isWrappingEnd(TokenType tokenType);

    int getIndexInParent(BasicPathNode pathNode);

    @Nullable
    Branch getBranch();

    boolean isScopeDemarcation();

    boolean isScopeIsolation();

    TokenType getTokenType();
}
