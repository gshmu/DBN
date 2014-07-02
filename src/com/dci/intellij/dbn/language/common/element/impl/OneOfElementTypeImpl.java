package com.dci.intellij.dbn.language.common.element.impl;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.ElementTypeBundle;
import com.dci.intellij.dbn.language.common.element.OneOfElementType;
import com.dci.intellij.dbn.language.common.element.lookup.OneOfElementTypeLookupCache;
import com.dci.intellij.dbn.language.common.element.parser.impl.OneOfElementTypeParser;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeDefinitionException;
import com.dci.intellij.dbn.language.common.psi.SequencePsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import gnu.trove.THashSet;
import org.jdom.Element;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class OneOfElementTypeImpl extends AbstractElementType implements OneOfElementType {
    protected final ElementType[] elementTypes;
    private boolean sortable;

    public OneOfElementTypeImpl(ElementTypeBundle bundle, ElementType parent, String id, Element def) throws ElementTypeDefinitionException {
        super(bundle, parent, id, def);
        List children = def.getChildren();

        elementTypes = new ElementType[children.size()];

        for (int i=0; i<children.size(); i++) {
            Element child = (Element) children.get(i);
            String type = child.getName();
            elementTypes[i] = bundle.resolveElementDefinition(child, type, this);
        }
        sortable = Boolean.parseBoolean(def.getAttributeValue("sortable"));
    }

    @Override
    protected OneOfElementTypeLookupCache createLookupCache() {
        return new OneOfElementTypeLookupCache(this);
    }

    @Override
    protected OneOfElementTypeParser createParser() {
        return new OneOfElementTypeParser(this);
    }

    public void warnAmbiguousBranches() {
        Set<TokenType> ambiguousTokenTypes = new THashSet<TokenType>();
        Set<ElementType> ambiguousElementTypes = new THashSet<ElementType>();
        for (ElementType elementType : elementTypes) {
            Set<TokenType> possibleTokens = elementType.getLookupCache().getFirstPossibleTokens();
            for (TokenType possibleToken : possibleTokens) {
                if (ambiguousTokenTypes.contains(possibleToken)) {
                    ambiguousElementTypes.add(elementType);
                }
                ambiguousTokenTypes.add(possibleToken);
            }
        }
        if (ambiguousElementTypes.size() > 0) {
            StringBuilder message = new StringBuilder("WARNING - ambiguous one-of elements [").append(getId()).append("] " );
            for (ElementType elementType : ambiguousElementTypes) {
                message.append(elementType.getId()).append(" ");
            }
            System.out.println(message.toString());
        }
    }

    public boolean isLeaf() {
        return false;
    }

    public String getDebugName() {
        return "one-of (" + getId() + ")";
    }

    public PsiElement createPsiElement(ASTNode astNode) {
        return new SequencePsiElement(astNode, this);
    }

    boolean sorted;
    public synchronized void sort() {
        if (sortable && ! sorted) {
            Arrays.sort(elementTypes, ONE_OF_COMPARATOR);
            sorted = true;
        }
    }

    private static final Comparator ONE_OF_COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            ElementType et1 = (ElementType) o1;
            ElementType et2 = (ElementType) o2;

            int i1 = et1.getLookupCache().startsWithIdentifier() ? 1 : 2;
            int i2 = et2.getLookupCache().startsWithIdentifier() ? 1 : 2;
            return i2-i1;
        }
    };

    public ElementType[] getPossibleElementTypes() {
        return elementTypes;
    }
}
