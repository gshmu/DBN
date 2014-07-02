package com.dci.intellij.dbn.language.common.element.impl;

import com.dci.intellij.dbn.code.common.completion.options.filter.CodeCompletionFilterSettings;
import com.dci.intellij.dbn.code.common.lookup.LookupValueProvider;
import com.dci.intellij.dbn.code.common.lookup.TokenLookupItemFactory;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.TokenTypeCategory;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.ElementTypeBundle;
import com.dci.intellij.dbn.language.common.element.IterationElementType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.QualifiedIdentifierElementType;
import com.dci.intellij.dbn.language.common.element.TokenElementType;
import com.dci.intellij.dbn.language.common.element.WrapperElementType;
import com.dci.intellij.dbn.language.common.element.lookup.TokenElementTypeLookupCache;
import com.dci.intellij.dbn.language.common.element.parser.impl.TokenElementTypeParser;
import com.dci.intellij.dbn.language.common.element.path.PathNode;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeDefinitionException;
import com.dci.intellij.dbn.language.common.psi.TokenPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import org.jdom.Element;

import java.util.Set;

public class TokenElementTypeImpl extends LeafElementTypeImpl implements LookupValueProvider, TokenElementType {
    private TokenLookupItemFactory lookupItemFactory;
    private TokenTypeCategory flavor;

    public TokenElementTypeImpl(ElementTypeBundle bundle, ElementType parent, String id, Element def) throws ElementTypeDefinitionException {
        super(bundle, parent, id, def);
        String typeId = def.getAttributeValue("type-id");
        TokenType tokenType = bundle.getTokenTypeBundle().getTokenType(typeId);
        setTokenType(tokenType);
        setDefaultFormatting(tokenType.getFormatting());

        String flavorName = def.getAttributeValue("flavor");
        if (StringUtil.isNotEmpty(flavorName)) {
            flavor = TokenTypeCategory.getCategory(flavorName);
        }

        setDescription(tokenType.getValue() + " " + getTokenTypeCategory());
    }

    public TokenElementTypeImpl(ElementTypeBundle bundle, ElementType parent, String typeId, String id) throws ElementTypeDefinitionException {
        super(bundle, parent, id, (String)null);
        TokenType tokenType = bundle.getTokenTypeBundle().getTokenType(typeId);
        setTokenType(tokenType);
        setDescription(tokenType.getValue() + " " + getTokenTypeCategory());

        setDefaultFormatting(tokenType.getFormatting());
    }

    public TokenElementTypeLookupCache createLookupCache() {
        return new TokenElementTypeLookupCache(this);
    }

    public TokenElementTypeParser createParser() {
        return new TokenElementTypeParser(this);
    }

    public String getDebugName() {
        return "token (" + getId() + " - " + getTokenType().getId() + ")";
    }

    public Set<LeafElementType> getNextPossibleLeafs(PathNode pathNode, CodeCompletionFilterSettings filterSettings) {
        ElementType parent = getParent();
        if (isIterationSeparator()) {
            if (parent instanceof IterationElementType) {
                IterationElementType iterationElementType = (IterationElementType) parent;
                /*return codeCompletionSettings.isSmart() ?
                        iterationElementType.getIteratedElementType().getFirstPossibleLeafs() :
                        iterationElementType.getIteratedElementType().getFirstRequiredLeafs();*/
                return iterationElementType.getIteratedElementType().getLookupCache().getFirstPossibleLeafs();
            } else if (parent instanceof QualifiedIdentifierElementType){
                return super.getNextPossibleLeafs(pathNode, filterSettings);
            }
        }
        if (parent instanceof WrapperElementType) {
            WrapperElementType wrapperElementType = (WrapperElementType) parent;
            if (this.equals(wrapperElementType.getBeginTokenElement())) {
                return wrapperElementType.getWrappedElement().getLookupCache().getFirstPossibleLeafs();
            }
        }

        return super.getNextPossibleLeafs(pathNode, filterSettings);
    }

    public Set<LeafElementType> getNextRequiredLeafs(PathNode pathNode) {
        if (isIterationSeparator()) {
            if (getParent() instanceof IterationElementType) {
                IterationElementType iterationElementType = (IterationElementType) getParent();
                return iterationElementType.getIteratedElementType().getLookupCache().getFirstRequiredLeafs();
            } else if (getParent() instanceof QualifiedIdentifierElementType){
                return super.getNextRequiredLeafs(pathNode);
            }
        }
        return super.getNextRequiredLeafs(pathNode);
    }

    public boolean isIterationSeparator() {
        return getId().equals(SEPARATOR);
    }

    public boolean isLeaf() {
        return true;
    }

    public boolean isIdentifier() {
        return false;
    }

    public boolean isSameAs(LeafElementType elementType) {
        if (elementType instanceof TokenElementType) {
            TokenElementType token = (TokenElementType) elementType;
            return token.getTokenType() == getTokenType();
        }
        return false;
    }


    public PsiElement createPsiElement(ASTNode astNode) {
        return new TokenPsiElement(astNode, this);
    }

    public String toString() {
        return getTokenType().getId() + " (" + getId() + ")";
    }

    public boolean isCharacter() {
        return getTokenType().isCharacter();
    }

    public TokenLookupItemFactory getLookupItemFactory(DBLanguage language) {
        if (lookupItemFactory == null) {
            lookupItemFactory = new TokenLookupItemFactory(this);
        }
        return lookupItemFactory;
    }

    @Override
    public TokenTypeCategory getFlavor() {
        return flavor;
    }

    @Override
    public TokenTypeCategory getTokenTypeCategory() {
        return flavor == null ? getTokenType().getCategory() : flavor;
    }
}
