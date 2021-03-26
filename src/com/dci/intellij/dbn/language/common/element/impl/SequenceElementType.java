package com.dci.intellij.dbn.language.common.element.impl;

import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.ElementTypeBundle;
import com.dci.intellij.dbn.language.common.element.lookup.ElementLookupContext;
import com.dci.intellij.dbn.language.common.element.lookup.ElementTypeLookupCache;
import com.dci.intellij.dbn.language.common.element.lookup.SequenceElementTypeLookupCache;
import com.dci.intellij.dbn.language.common.element.parser.BranchCheck;
import com.dci.intellij.dbn.language.common.element.parser.impl.SequenceElementTypeParser;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeDefinitionException;
import com.dci.intellij.dbn.language.common.psi.SequencePsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import gnu.trove.THashSet;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class SequenceElementType extends ElementTypeBase {
    protected ElementTypeRef[] children;
    private int exitIndex;

    public ElementTypeRef[] getChildren() {
        return children;
    }

    public ElementTypeRef getFirstChild() {return children[0];}

    public ElementTypeRef getChild(int index) {
        return children[index];
    }

    public SequenceElementType(ElementTypeBundle bundle, ElementTypeBase parent, String id) {
        super(bundle, parent, id, (String) null);
    }

    public SequenceElementType(ElementTypeBundle bundle, ElementTypeBase parent, String id, Element def) throws ElementTypeDefinitionException {
        super(bundle, parent, id, def);
    }

    @Override
    public SequenceElementTypeLookupCache createLookupCache() {
        return new SequenceElementTypeLookupCache<>(this);
    }

    @NotNull
    @Override
    public SequenceElementTypeParser createParser() {
        return new SequenceElementTypeParser<>(this);
    }

    @Override
    protected void loadDefinition(Element def) throws ElementTypeDefinitionException {
        super.loadDefinition(def);
        List children = def.getChildren();
        this.children = new ElementTypeRef[children.size()];

        ElementTypeRef previous = null;
        for (int i = 0; i < children.size(); i++) {
            Element child = (Element) children.get(i);
            String type = child.getName();
            ElementTypeBase elementType = getElementBundle().resolveElementDefinition(child, type, this);
            boolean optional = getBooleanAttribute(child, "optional");
            double version = Double.parseDouble(CommonUtil.nvl(child.getAttributeValue("version"), "0"));

            Set<BranchCheck> branchChecks = parseBranchChecks(child.getAttributeValue("branch-check"));
            this.children[i] = new ElementTypeRef(previous, this, elementType, optional, version, branchChecks);
            previous = this.children[i];

            if (child.getAttributeValue("exit") != null) exitIndex = i;
        }
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    public int getChildCount() {
        return children.length;
    }

    @Override
    public PsiElement createPsiElement(ASTNode astNode) {
        return new SequencePsiElement(astNode, this);
    }

    public boolean isExitIndex(int index) {
        return index <= exitIndex;
    }

    @NotNull
    @Override
    public String getName() {
        return "sequence (" + getId() + ")";
    }

    /*********************************************************
     *                Cached lookup helpers                  *
     *********************************************************/
    public boolean containsLandmarkTokenFromIndex(TokenType tokenType, int index) {
        if (index < children.length) {
            ElementTypeRef child = children[index];
            while (child != null) {
                if (child.getLookupCache().couldStartWithToken(tokenType)) return true;
                child = child.getNext();
            }
        }
        return false;
    }

    public Set<TokenType> getFirstPossibleTokensFromIndex(ElementLookupContext context, int index) {
        if (children[index].optional) {
            Set<TokenType> tokenTypes = new THashSet<>();
            for (int i=index; i< children.length; i++) {
                ElementTypeLookupCache lookupCache = children[i].getLookupCache();
                lookupCache.collectFirstPossibleTokens(context.reset(), tokenTypes);
                if (!children[i].optional) break;
            }
            return tokenTypes;
        } else {
            ElementTypeLookupCache lookupCache = children[index].getLookupCache();
            return lookupCache.collectFirstPossibleTokens(context.reset());
        }
    }

    public boolean isPossibleTokenFromIndex(TokenType tokenType, int index) {
        if (index < children.length) {
            for (int i= index; i< children.length; i++) {
                if (children[i].getLookupCache().couldStartWithToken(tokenType)){
                    return true;
                }
                if (!children[i].optional) {
                    return false;
                }
            }
        }
        return false;
    }

    public int indexOf(LeafElementType leafElementType) {
        WrappingDefinition wrapping = getWrapping();
        if (wrapping != null && leafElementType instanceof TokenElementType) {
            TokenElementType tokenElementType = (TokenElementType) leafElementType;
            if (wrapping.getEndElementType().tokenType == tokenElementType.tokenType) {
                return children.length-1;
            }
        }
        ElementTypeRef child = children[0];
        while (child != null) {
            ElementTypeBase childElementType = child.elementType;
            if (childElementType == leafElementType || childElementType.lookupCache.containsLeaf(leafElementType)) {
                return child.getIndex();
            }
            child = child.getNext();
        }

        return -1;
    }

    public int indexOf(ElementType elementType, int fromIndex) {
        WrappingDefinition wrapping = getWrapping();
        if (wrapping != null && elementType instanceof TokenElementType) {
            TokenElementType tokenElementType = (TokenElementType) elementType;
            if (wrapping.getEndElementType().tokenType == tokenElementType.tokenType) {
                return children.length-1;
            }
        }

        if (fromIndex < children.length) {
            ElementTypeRef child = children[fromIndex];
            while (child != null) {
                if (child.elementType == elementType) {
                    return child.getIndex();
                }
                child = child.getNext();
            }
        }
        return -1;
    }

    public int indexOf(ElementType elementType) {
        return indexOf(elementType, 0);
    }
}
