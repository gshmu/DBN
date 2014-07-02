package com.dci.intellij.dbn.language.common.element.impl;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.ElementTypeBundle;
import com.dci.intellij.dbn.language.common.element.SequenceElementType;
import com.dci.intellij.dbn.language.common.element.lookup.SequenceElementTypeLookupCache;
import com.dci.intellij.dbn.language.common.element.parser.impl.SequenceElementTypeParser;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeDefinitionException;
import com.dci.intellij.dbn.language.common.psi.SequencePsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import gnu.trove.THashSet;
import org.jdom.Element;

import java.util.List;
import java.util.Set;

public class SequenceElementTypeImpl extends AbstractElementType implements SequenceElementType {
    protected ElementType[] elementTypes;
    protected boolean[] optional;
    private int containsKeywords = -1;
    private int exitIndex;

    public ElementType[] getElementTypes() {
        return elementTypes;
    }

    public SequenceElementTypeImpl(ElementTypeBundle bundle, ElementType parent, String id) {
        super(bundle, parent, id, (String) null);
    }

    public SequenceElementTypeImpl(ElementTypeBundle bundle, ElementType parent, String id, Element def) throws ElementTypeDefinitionException {
        super(bundle, parent, id, def);
    }

    @Override
    public SequenceElementTypeLookupCache createLookupCache() {
        return new SequenceElementTypeLookupCache<SequenceElementType>(this);
    }

    @Override
    public SequenceElementTypeParser createParser() {
        return new SequenceElementTypeParser<SequenceElementType>(this);
    }

    protected void loadDefinition(Element def) throws ElementTypeDefinitionException {
        super.loadDefinition(def);
        List children = def.getChildren();
        elementTypes = new ElementType[children.size()];
        optional = new boolean[children.size()];

        for (int i = 0; i < children.size(); i++) {
            Element child = (Element) children.get(i);
            String type = child.getName();
            elementTypes[i] = getElementBundle().resolveElementDefinition(child, type, this);
            optional[i] = Boolean.parseBoolean(child.getAttributeValue("optional"));
            if (child.getAttributeValue("exit") != null) exitIndex = i;
        }
    }

    public boolean isLeaf() {
        return false;
    }

    public int elementsCount() {
        return elementTypes.length;
    }

    public PsiElement createPsiElement(ASTNode astNode) {
        return new SequencePsiElement(astNode, this);
    }

    public boolean isOptionalFromIndex(int index) {
        for (int i=index; i<optional.length; i++) {
            if (!optional[i]) return false;
        }
        return true;
    }

    public boolean isLast(int index) {
        return index == elementTypes.length - 1;
    }

    public boolean isFirst(int index) {
        return index == 0;
    }

    public boolean isOptional(int index) {
        return optional[index];
    }

    public boolean isOptional(ElementType elementType) {
        for (int i = 0; i<elementTypes.length; i++) {
            if (elementTypes[i] == elementType) {
                return optional[i];
            }
        }
        return true;
    }

    public boolean canStartWithElement(ElementType elementType) {
        for (int i = 0; i<optional.length; i++) {
            if (optional[i]) {
                if (elementType == elementTypes[i]) return true;
            } else {
                return elementType == elementTypes[i];
            }
        }
        return false;
    }

    public boolean shouldStartWithElement(ElementType elementType) {
        for (int i = 0; i<optional.length; i++) {
            if (!optional[i]) {
                return elementType == elementTypes[i];
            } 
        }
        return false;
    }


    public boolean isExitIndex(int index) {
        return index <= exitIndex;
    }

    public String getDebugName() {
        return "sequence (" + getId() + ")";
    }

    public boolean[] getOptionalElementsMap() {
        return optional;
    }

    /*********************************************************
     *                Cached lookup helpers                  *
     *********************************************************/
    public boolean containsLandmarkTokenFromIndex(TokenType tokenType, int index) {
        for (int i = index; i < elementTypes.length; i++) {
            if (elementTypes[i].getLookupCache().containsLandmarkToken(tokenType)) return true;
        }
        return false;
    }

    public Set<TokenType> getFirstPossibleTokensFromIndex(int index) {
        if (isOptional(index)) {
            Set<TokenType> tokenTypes = new THashSet<TokenType>();
            for (int i=index; i< elementTypes.length; i++) {
                tokenTypes.addAll(elementTypes[i].getLookupCache().getFirstPossibleTokens());
                if (!isOptional(i)) break;
            }
            return tokenTypes;
        } else {
            return elementTypes[index].getLookupCache().getFirstPossibleTokens();
        }
    }

    public boolean isPossibleTokenFromIndex(TokenType tokenType, int index) {
        if (index < elementTypes.length) {
            for (int i= index; i<elementTypes.length; i++) {
                if (elementTypes[i].getLookupCache().canStartWithToken(tokenType)){
                    return true;
                }
                if (!isOptional(i)) {
                    return false;
                }
            }
        }
        return false;
    }

    public int indexOf(ElementType elementType, int fromIndex) {
        for (int i=fromIndex; i<elementTypes.length; i++) {
            if (elementTypes[i] == elementType) return i;
        }
        return -1;
    }

    public int indexOf(ElementType elementType) {
        return indexOf(elementType, 0);
    }
}
