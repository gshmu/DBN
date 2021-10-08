package com.dci.intellij.dbn.language.common.element.impl;

import com.dci.intellij.dbn.language.common.element.ElementTypeBundle;
import com.dci.intellij.dbn.language.common.element.cache.BasicElementTypeLookupCache;
import com.dci.intellij.dbn.language.common.element.cache.ElementTypeLookupCache;
import com.dci.intellij.dbn.language.common.element.parser.impl.BasicElementTypeParser;
import com.dci.intellij.dbn.language.common.psi.UnknownPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class BasicElementType extends ElementTypeBase {

    protected BasicElementType(ElementTypeBundle bundle, String id, String description) {
        super(bundle, null, id, description);
    }

    @Override
    public ElementTypeLookupCache createLookupCache() {
        return new BasicElementTypeLookupCache(this);
    }

    @NotNull
    @Override
    public BasicElementTypeParser createParser() {
        return new BasicElementTypeParser(this);
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @NotNull
    @Override
    public String getName() {
        return getId();
    }

    @Override
    public PsiElement createPsiElement(ASTNode astNode) {
        return new UnknownPsiElement(astNode, this);
    }

}
