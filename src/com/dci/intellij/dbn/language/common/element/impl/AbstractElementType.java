package com.dci.intellij.dbn.language.common.element.impl;

import com.dci.intellij.dbn.code.common.style.formatting.FormattingDefinition;
import com.dci.intellij.dbn.code.common.style.formatting.FormattingDefinitionFactory;
import com.dci.intellij.dbn.code.common.style.formatting.IndentDefinition;
import com.dci.intellij.dbn.code.common.style.formatting.SpacingDefinition;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.ElementTypeBundle;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.SequenceElementType;
import com.dci.intellij.dbn.language.common.element.TokenElementType;
import com.dci.intellij.dbn.language.common.element.TokenPairTemplate;
import com.dci.intellij.dbn.language.common.element.lookup.ElementTypeLookupCache;
import com.dci.intellij.dbn.language.common.element.parser.Branch;
import com.dci.intellij.dbn.language.common.element.parser.BranchCheck;
import com.dci.intellij.dbn.language.common.element.parser.ElementTypeParser;
import com.dci.intellij.dbn.language.common.element.path.PathNode;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttributeHolder;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeDefinitionException;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.tree.IElementType;
import gnu.trove.THashSet;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Set;
import java.util.StringTokenizer;

public abstract class AbstractElementType extends IElementType implements ElementType {
    private static final Logger LOGGER = LoggerFactory.createLogger();
    private static final FormattingDefinition STATEMENT_FORMATTING = new FormattingDefinition(null, IndentDefinition.NORMAL, SpacingDefinition.MIN_LINE_BREAK, null);

    private int idx;

    private String id;
    private int hashCode;
    private String description;
    private Icon icon;
    private Branch branch;
    private FormattingDefinition formatting;
/*
    private Latent<ElementTypeLookupCache> lookupCache = Latent.basic(() -> createLookupCache());
    private Latent<ElementTypeParser> parser = Latent.basic(() -> createParser());
*/
    private ElementTypeLookupCache lookupCache = createLookupCache();
    private ElementTypeParser parser = createParser();
    private ElementTypeBundle bundle;
    private ElementType parent;
    private DBObjectType virtualObjectType;
    private ElementTypeAttributeHolder attributes;

    protected WrappingDefinition wrapping;

    public AbstractElementType(ElementTypeBundle bundle, ElementType parent, String id, @Nullable String description) {
        super(id, bundle.getLanguageDialect(), false);
        idx = TokenType.INDEXER.incrementAndGet();
        this.id = id;
        this.hashCode = id.hashCode();
        this.description = description;
        this.bundle = bundle;
        this.parent = parent;
    }

    public AbstractElementType(ElementTypeBundle bundle, ElementType parent, String id, Element def) throws ElementTypeDefinitionException {
        super(id, bundle.getLanguageDialect(), false);
        idx = TokenType.INDEXER.incrementAndGet();
        this.id = def.getAttributeValue("id");
        this.hashCode = id.hashCode();
        if (!id.equals(this.id)) {
            this.id = id;
            def.setAttribute("id", this.id);
            bundle.markIndexesDirty();
        }
        this.bundle = bundle;
        this.parent = parent;
        if (StringUtil.isNotEmpty(def.getAttributeValue("exit")) && !(parent instanceof SequenceElementType)) {
            LOGGER.warn('[' + getLanguageDialect().getID() + "] Invalid element attribute 'exit'. (id=" + this.id + "). Attribute is only allowed for direct child of sequence element");
        }
        loadDefinition(def);
    }

    protected Set<BranchCheck> parseBranchChecks(String definitions) {
        Set<BranchCheck> branches = null;
        if (definitions != null) {
            branches = new THashSet<BranchCheck>();
            StringTokenizer tokenizer = new StringTokenizer(definitions, " ");
            while (tokenizer.hasMoreTokens()) {
                String branchDef = tokenizer.nextToken().trim();
                branches.add(new BranchCheck(branchDef));
            }
        }
        return branches;
    }

    @Override
    public int getIdx() {
        return idx;
    }

    @Override
    public WrappingDefinition getWrapping() {
        return wrapping;
    }

    @Override
    public boolean isWrappingBegin(LeafElementType elementType) {
        return wrapping != null && wrapping.getBeginElementType() == elementType;
    }

    @Override
    public boolean isWrappingBegin(TokenType tokenType) {
        return wrapping != null && wrapping.getBeginElementType().getTokenType() == tokenType;
    }

    @Override
    public boolean isWrappingEnd(LeafElementType elementType) {
        return wrapping != null && wrapping.getEndElementType() == elementType;
    }

    @Override
    public boolean isWrappingEnd(TokenType tokenType) {
        return wrapping != null && wrapping.getEndElementType().getTokenType() == tokenType;
    }

    protected abstract ElementTypeLookupCache createLookupCache();

    @NotNull
    protected abstract ElementTypeParser createParser();

    @Override
    public void setDefaultFormatting(FormattingDefinition defaultFormatting) {
        formatting = FormattingDefinitionFactory.mergeDefinitions(formatting, defaultFormatting);
    }

    protected void loadDefinition(Element def) throws ElementTypeDefinitionException {
        String attributesString = def.getAttributeValue("attributes");
        if (StringUtil.isNotEmptyOrSpaces(attributesString)) {
            attributes =  new ElementTypeAttributeHolder(attributesString);
        }

        String objectTypeName = def.getAttributeValue("virtual-object");
        if (objectTypeName != null) {
            virtualObjectType = ElementTypeBundle.resolveObjectType(objectTypeName);
        }
        formatting = FormattingDefinitionFactory.loadDefinition(def);
        if (is(ElementTypeAttribute.STATEMENT)) {
            setDefaultFormatting(STATEMENT_FORMATTING);
        }

        String iconKey = def.getAttributeValue("icon");
        if (iconKey != null)  icon = Icons.getIcon(iconKey);

        String branchDef = def.getAttributeValue("branch");
        if (branchDef != null) {
            branch = new Branch(branchDef);
        }

        loadWrappingAttributes(def);
    }

    private void loadWrappingAttributes(Element def) throws ElementTypeDefinitionException {
        String templateId = def.getAttributeValue("wrapping-template");
        TokenElementType beginTokenElement = null;
        TokenElementType endTokenElement = null;
        if (StringUtil.isEmpty(templateId)) {
            String beginTokenId = def.getAttributeValue("wrapping-begin-token");
            String endTokenId = def.getAttributeValue("wrapping-end-token");

            if (StringUtil.isNotEmpty(beginTokenId) && StringUtil.isNotEmpty(endTokenId)) {
                beginTokenElement = new TokenElementTypeImpl(bundle, this, beginTokenId, id);
                endTokenElement = new TokenElementTypeImpl(bundle, this, endTokenId, id);
            }
        } else {
            TokenPairTemplate template = TokenPairTemplate.valueOf(templateId);
            String beginTokenId = template.getBeginToken();
            String endTokenId = template.getEndToken();
            beginTokenElement = new TokenElementTypeImpl(bundle, this, beginTokenId, id);
            endTokenElement = new TokenElementTypeImpl(bundle, this, endTokenId, id);

            if (template.isBlock()) {
                beginTokenElement.setDefaultFormatting(FormattingDefinition.LINE_BREAK_AFTER);
                endTokenElement.setDefaultFormatting(FormattingDefinition.LINE_BREAK_BEFORE);
                setDefaultFormatting(FormattingDefinition.LINE_BREAK_BEFORE);
            }
        }

        if (beginTokenElement != null && endTokenElement != null) {
            wrapping = new WrappingDefinition(beginTokenElement, endTokenElement);
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

    @Override
    public ElementType getParent() {
        return parent;
    }

    @Override
    public Branch getBranch() {
        return branch;
    }

    @Override
    public ElementTypeLookupCache getLookupCache() {
        return lookupCache;
    }

    @Override
    @NotNull
    public ElementTypeParser getParser() {
        return parser;
    }


    @Override
    public boolean is(ElementTypeAttribute attribute) {
        return attributes != null && attributes.is(attribute);
    }

    @Override
    public boolean set(ElementTypeAttribute attribute, boolean value) {
        throw new AbstractMethodError("Operation not allowed");
    }

    @Override
    public FormattingDefinition getFormatting() {
        return formatting;
    }

    @Override
    @NotNull
    public DBLanguage getLanguage() {
        return getLanguageDialect().getBaseLanguage();
    }

    @Override
    public DBLanguageDialect getLanguageDialect() {
        return (DBLanguageDialect) super.getLanguage();
    }

    @Override
    public ElementTypeBundle getElementBundle() {
        return bundle;
    }

    public String toString() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public int getIndexInParent(PathNode pathNode) {
        PathNode parentNode = pathNode.getParent();
        if (parentNode != null && parentNode.getElementType() instanceof SequenceElementType) {
            SequenceElementType sequenceElementType = (SequenceElementType) parentNode.getElementType();
            return sequenceElementType.indexOf(this);
        }
        return 0;
    }

    /*********************************************************
     *                  Virtual Object                       *
     *********************************************************/
    @Override
    public boolean isVirtualObject() {
        return virtualObjectType != null;
    }

    @Override
    public DBObjectType getVirtualObjectType() {
        return virtualObjectType;
    }

    protected boolean getBooleanAttribute(Element element, String attributeName) {
        String attributeValue = element.getAttributeValue(attributeName);
        if (StringUtils.isNotEmpty(attributeValue)) {
            if (attributeValue.equals("true")) return true;
            if (attributeValue.equals("false")) return false;
            LOGGER.warn('[' + getLanguageDialect().getID() + "] Invalid element boolean attribute '" + attributeName + "' (id=" + this.id + "). Expected 'true' or 'false'");
        }
        return false;
    }
}
