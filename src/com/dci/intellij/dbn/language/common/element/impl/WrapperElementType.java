package com.dci.intellij.dbn.language.common.element.impl;

import com.dci.intellij.dbn.code.common.style.formatting.FormattingDefinition;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.language.common.element.ElementTypeBundle;
import com.dci.intellij.dbn.language.common.element.TokenPairTemplate;
import com.dci.intellij.dbn.language.common.element.lookup.WrapperElementTypeLookupCache;
import com.dci.intellij.dbn.language.common.element.parser.impl.WrapperElementTypeParser;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeDefinitionException;
import com.dci.intellij.dbn.language.common.psi.SequencePsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.stringAttribute;

public class WrapperElementType extends ElementTypeBase {
    private WrappingDefinition wrappingDefinition;
    private ElementTypeBase wrappedElement;
    public boolean wrappedElementOptional;

    public WrapperElementType(ElementTypeBundle bundle, ElementTypeBase parent, String id, Element def) throws ElementTypeDefinitionException {
        super(bundle, parent, id, def);
    }

    @Override
    protected void loadDefinition(Element def) throws ElementTypeDefinitionException {
        super.loadDefinition(def);
        ElementTypeBundle bundle = getElementBundle();
        String templateId = stringAttribute(def, "template");

        TokenElementType beginTokenElement;
        TokenElementType endTokenElement;
        if (StringUtil.isEmpty(templateId)) {
            String startTokenId = stringAttribute(def, "begin-token");
            String endTokenId = stringAttribute(def, "end-token");

            beginTokenElement = new TokenElementType(bundle, this, startTokenId, "begin-token");
            endTokenElement = new TokenElementType(bundle, this, endTokenId, "end-token");
        } else {
            TokenPairTemplate template = TokenPairTemplate.valueOf(templateId);
            beginTokenElement = new TokenElementType(bundle, this, template.getBeginToken(), "begin-token");
            endTokenElement = new TokenElementType(bundle, this, template.getEndToken(), "end-token");

            if (template.isBlock()) {
                beginTokenElement.setDefaultFormatting(FormattingDefinition.LINE_BREAK_AFTER);
                endTokenElement.setDefaultFormatting(FormattingDefinition.LINE_BREAK_BEFORE);
                setDefaultFormatting(FormattingDefinition.LINE_BREAK_BEFORE);
            }
        }

        wrappingDefinition = new WrappingDefinition(beginTokenElement, endTokenElement);


        List<Element> children = def.getChildren();
        if (children.size() != 1) {
            throw new ElementTypeDefinitionException(
                    "Invalid wrapper definition. " +
                    "Element should contain exact one child of type 'one-of', 'sequence', 'element', 'token'");
        }
        Element child = children.get(0);
        String type = child.getName();
        wrappedElement = bundle.resolveElementDefinition(child, type, this);
        wrappedElementOptional = getBooleanAttribute(child, "optional");

        //getLookupCache().registerFirstLeaf(beginTokenElement, isOptional);
    }

    @Override
    public WrapperElementTypeLookupCache createLookupCache() {
        return new WrapperElementTypeLookupCache(this);
    }

    @NotNull
    @Override
    public WrapperElementTypeParser createParser() {
        return new WrapperElementTypeParser(this);
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    public TokenElementType getBeginTokenElement() {
        return wrappingDefinition.getBeginElementType();
    }

    public TokenElementType getEndTokenElement() {
        return wrappingDefinition.getEndElementType();
    }

    public boolean isStrong() {
        if (getBeginTokenElement().getTokenType().isReservedWord()) {
            return true;
        }
        if (getParent() instanceof SequenceElementType) {
            SequenceElementType sequenceElementType = (SequenceElementType) getParent();
            int index = sequenceElementType.indexOf(this);

            ElementTypeRef child = sequenceElementType.getChild(index);
            if (child.optional) {
                return false;
            }

            return index > 0 && !child.isOptionalToHere();
        }
        return false;
    }

    @NotNull
    @Override
    public String getName() {
        return "wrapper (" + getId() + ")";
    }
    @Override
    public PsiElement createPsiElement(ASTNode astNode) {
        return new SequencePsiElement(astNode, this);
    }

    public ElementTypeBase getWrappedElement() {
        return wrappedElement;
    }

    public void setWrappedElement(ElementTypeBase wrappedElement) {
        this.wrappedElement = wrappedElement;
    }
}
