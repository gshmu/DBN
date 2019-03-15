package com.dci.intellij.dbn.language.common.psi;

import com.dci.intellij.dbn.language.common.element.impl.BlockElementType;
import com.intellij.lang.ASTNode;

public class BlockPsiElement extends SequencePsiElement<BlockElementType> {
    public BlockPsiElement(ASTNode astNode, BlockElementType elementType) {
        super(astNode, elementType);
    }
}
