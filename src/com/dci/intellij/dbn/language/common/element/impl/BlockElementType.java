package com.dci.intellij.dbn.language.common.element.impl;

import com.dci.intellij.dbn.language.common.element.ElementTypeBundle;
import com.dci.intellij.dbn.language.common.element.lookup.BlockElementTypeLookupCache;
import com.dci.intellij.dbn.language.common.element.parser.impl.BlockElementTypeParser;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeDefinitionException;
import com.dci.intellij.dbn.language.common.psi.BlockPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class BlockElementType extends SequenceElementType {
    public static final int INDENT_NONE = 0;
    public static final int INDENT_NORMAL = 1;

    private int indent;

    public BlockElementType(ElementTypeBundle bundle, ElementTypeBase parent, String id, Element def) throws ElementTypeDefinitionException {
        super(bundle, parent, id, def);
    }

    @Override
    public BlockElementTypeLookupCache createLookupCache() {
        return new BlockElementTypeLookupCache(this);
    }

    @NotNull
    @Override
    public BlockElementTypeParser createParser() {
        return new BlockElementTypeParser(this);
    }

    @Override
    protected void loadDefinition(Element def) throws ElementTypeDefinitionException {
        super.loadDefinition(def);
        String indentString = def.getAttributeValue("indent");
        if (indentString != null) {
            indent = "NORMAL".equals(indentString) ? INDENT_NORMAL : INDENT_NONE;
        }
    }

    @Override
    public PsiElement createPsiElement(ASTNode astNode) {
        return new BlockPsiElement(astNode, this);
    }

    @NotNull
    @Override
    public String getName() {
        return "block (" + getId() + ")";
    }


    public int getIndent() {
        return indent;
    }
}
