package com.dci.intellij.dbn.language.common.element.path;

import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.SequenceElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.FileElement;

public class ASTPathNode implements PathNode{
    private ASTNode astNode;

    public ASTPathNode(ASTNode astNode) {
        this.astNode = astNode;
    }

    public PathNode getParent() {
        ASTNode treeParent = astNode.getTreeParent();
        if (treeParent != null && !(treeParent instanceof FileElement)) {
            return new ASTPathNode(treeParent);
        }
        return null;
    }

    public int getCurrentSiblingIndex() {
        ASTNode parentAstNode = astNode.getTreeParent();
        if (parentAstNode.getElementType() instanceof SequenceElementType) {
            SequenceElementType sequenceElementType = (SequenceElementType) parentAstNode.getElementType();
            int index = 0;
            ASTNode child = parentAstNode.getFirstChildNode();
            while (child != null) {
                if (astNode == child) {
                    break;
                }
                index++;
                child = child.getTreeNext();
                if (child instanceof PsiWhiteSpace){
                    child = child.getTreeNext();
                }
            }
            return sequenceElementType.indexOf((ElementType) astNode.getElementType(), index);
        }
        return 0;
    }

    public ElementType getElementType() {
        return (ElementType) astNode.getElementType();
    }

    public PathNode getRootPathNode() {
        return null;
    }

    public boolean isRecursive() {
        return false; 
    }

    public boolean isRecursive(ElementType elementType) {
        return false;
    }

    @Override
    public void detach() {

    }
}
