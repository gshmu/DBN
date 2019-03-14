package com.dci.intellij.dbn.language.common.element.impl;

import com.dci.intellij.dbn.code.common.style.formatting.FormattingDefinition;
import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.ElementTypeBundle;
import com.dci.intellij.dbn.language.common.element.IterationElementType;
import com.dci.intellij.dbn.language.common.element.TokenElementType;
import com.dci.intellij.dbn.language.common.element.lookup.IterationElementTypeLookupCache;
import com.dci.intellij.dbn.language.common.element.parser.impl.IterationElementTypeParser;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeDefinitionException;
import com.dci.intellij.dbn.language.common.psi.SequencePsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

public class IterationElementTypeImpl extends AbstractElementType implements IterationElementType {

    private ElementType iteratedElementType;
    private TokenElementType[] separatorTokens;
    private int[] elementsCountVariants;
    private int minIterations;
    private Latent<Boolean> followedBySeparator = Latent.basic(() -> {
        TokenElementType[] separatorTokens = getSeparatorTokens();
        if (separatorTokens != null) {
            Set<TokenType> nextPossibleTokens = getLookupCache().getNextPossibleTokens();
            for (TokenElementType separatorToken : separatorTokens) {
                if (nextPossibleTokens.contains(separatorToken.getTokenType())) {
                    return true;
                }
            }
        }
        return false;
    });

    public IterationElementTypeImpl(ElementTypeBundle bundle, ElementType parent, String id, Element def) throws ElementTypeDefinitionException {
        super(bundle, parent, id, def);
    }

    @Override
    protected IterationElementTypeLookupCache createLookupCache() {
        return new IterationElementTypeLookupCache(this);
    }

    @NotNull
    @Override
    protected IterationElementTypeParser createParser() {
        return new IterationElementTypeParser(this);
    }

    @Override
    protected void loadDefinition(Element def) throws ElementTypeDefinitionException {
        super.loadDefinition(def);
        ElementTypeBundle bundle = getElementBundle();
        String separatorTokenIds = def.getAttributeValue("separator");
        if (separatorTokenIds != null) {
            StringTokenizer tokenizer = new StringTokenizer(separatorTokenIds, ",");
            List<TokenElementType> separators = new ArrayList<TokenElementType>();
            while (tokenizer.hasMoreTokens()) {
                String separatorTokenId = tokenizer.nextToken().trim();
                TokenElementType separatorToken = new TokenElementTypeImpl(bundle, this, separatorTokenId, TokenElementType.SEPARATOR);
                        //bundle.getTokenElementType(separatorTokenId);
                separatorToken.setDefaultFormatting(separatorToken.isCharacter() ?
                        FormattingDefinition.NO_SPACE_BEFORE :
                        FormattingDefinition.ONE_SPACE_BEFORE);
                separators.add(separatorToken);
            }
            separatorTokens = separators.toArray(new TokenElementType[0]);
        }

        List children = def.getChildren();
        if (children.size() != 1) {
            throw new ElementTypeDefinitionException("[" + getLanguageDialect().getID() + "] Invalid iteration definition (id=" + getId() + "). Element should contain exactly one child.");
        }
        Element child = (Element) children.get(0);
        String type = child.getName();
        iteratedElementType = bundle.resolveElementDefinition(child, type, this);

        String elementsCountDef = def.getAttributeValue("elements-count");
        if (elementsCountDef != null) {
            List<Integer> variants = new ArrayList<Integer>();
            StringTokenizer tokenizer = new StringTokenizer(elementsCountDef, ",");
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                int index = token.indexOf('-');
                if (index > -1) {
                    int start = Integer.parseInt(token.substring(0, index).trim());
                    int end  = Integer.parseInt(token.substring(index + 1).trim());
                    for (int i=start; i<=end; i++) {
                        variants.add(i);
                    }
                } else {
                    variants.add(Integer.parseInt(token.trim()));
                }
            }

            elementsCountVariants = new int[variants.size()];
            for (int i=0; i< elementsCountVariants.length; i++) {
                elementsCountVariants[i] = variants.get(i);
            }
        }

        String minIterationsDef = def.getAttributeValue("min-iterations");
        if (minIterationsDef != null) {
            minIterations = Integer.parseInt(minIterationsDef);
        }
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public String getDebugName() {
        return "iteration (" + getId() + ")";
    }

    @Override
    public PsiElement createPsiElement(ASTNode astNode) {
        return new SequencePsiElement(astNode, this);
    }

    @Override
    public ElementType getIteratedElementType() {
        return iteratedElementType;
    }

    @Override
    public TokenElementType[] getSeparatorTokens() {
        return separatorTokens;
    }

    @Override
    public int[] getElementsCountVariants() {
        return elementsCountVariants;
    }

    @Override
    public Integer getMinIterations() {
        return minIterations;
    }

    @Override
    public boolean isSeparator(TokenElementType tokenElementType) {
        if (separatorTokens != null) {
            for (TokenElementType separatorToken: separatorTokens) {
                if (separatorToken == tokenElementType) return true;
            }
        }
        return false;
    }

    @Override
    public boolean isSeparator(TokenType tokenType) {
        if (separatorTokens != null) {
            for (TokenElementType separatorToken: separatorTokens) {
                if (separatorToken.getTokenType() == tokenType) return true;
            }
        }
        return false;
    }

    @Override
    public boolean isFollowedBySeparator() {
        return followedBySeparator.get();
    }
}
