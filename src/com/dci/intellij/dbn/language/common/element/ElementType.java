package com.dci.intellij.dbn.language.common.element;

import com.dci.intellij.dbn.code.common.style.formatting.FormattingDefinition;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.element.impl.WrappingDefinition;
import com.dci.intellij.dbn.language.common.element.lookup.ElementTypeLookupCache;
import com.dci.intellij.dbn.language.common.element.parser.ElementTypeParser;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttributesBundle;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;

import javax.swing.Icon;

public interface ElementType {

    String getId();

    String getDescription();

    String getDebugName();

    Icon getIcon();

    ElementType getParent();

    DBLanguage getLanguage();

    DBLanguageDialect getLanguageDialect();

    ElementTypeLookupCache getLookupCache();

    ElementTypeParser getParser();

    boolean is(ElementTypeAttribute attribute);

    boolean isLeaf();

    boolean isVirtualObject();

    boolean isVirtualObjectInsideLookup();    

    DBObjectType getVirtualObjectType();

    PsiElement createPsiElement(ASTNode astNode);

    ElementTypeBundle getElementBundle();

    void registerVirtualObject(DBObjectType objectType);

    FormattingDefinition getFormatting();

    void setDefaultFormatting(FormattingDefinition defaults);

    ElementTypeAttributesBundle getAttributes();

    WrappingDefinition getWrapping();
}
