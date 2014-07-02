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
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.ElementTypeBundle;
import com.dci.intellij.dbn.language.common.element.SequenceElementType;
import com.dci.intellij.dbn.language.common.element.TokenElementType;
import com.dci.intellij.dbn.language.common.element.WrapperElementTypeTemplate;
import com.dci.intellij.dbn.language.common.element.lookup.ElementTypeLookupCache;
import com.dci.intellij.dbn.language.common.element.parser.ElementTypeParser;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttributesBundle;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeDefinitionException;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.tree.IElementType;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public abstract class AbstractElementType extends IElementType implements ElementType {
    private static final Logger LOGGER = LoggerFactory.createLogger();
    private static final FormattingDefinition STATEMENT_FORMATTING = new FormattingDefinition(null, IndentDefinition.NORMAL, SpacingDefinition.MIN_LINE_BREAK, null);

    private String id;
    private String description;
    private Icon icon;
    private FormattingDefinition formatting;
    private ElementTypeLookupCache lookupCache;
    private ElementTypeParser parser;
    private ElementTypeBundle bundle;
    private ElementType parent;

    private DBObjectType virtualObjectType;
    private boolean isVirtualObjectInsideLookup;
    private ElementTypeAttributesBundle attributes = ElementTypeAttributesBundle.EMPTY;

    protected WrappingDefinition wrapping;

    public AbstractElementType(ElementTypeBundle bundle, ElementType parent, String id, @Nullable String description) {
        super(id, bundle.getLanguageDialect(), false);
        this.id = id;
        this.description = description;
        this.bundle = bundle;
        this.parent = parent;
    }

    public AbstractElementType(ElementTypeBundle bundle, ElementType parent, String id, Element def) throws ElementTypeDefinitionException {
        super(id, bundle.getLanguageDialect(), false);
        this.id = def.getAttributeValue("id");
        if (!id.equals(this.id)) {
            this.id = id;
            def.setAttribute("id", this.id);
            bundle.markIndexesDirty();
        }
        this.bundle = bundle;
        this.parent = parent;
        if (StringUtil.isNotEmpty(def.getAttributeValue("exit")) && !(parent instanceof SequenceElementType)) {
            LOGGER.warn("[" + getLanguageDialect().getID() + "] Invalid element attribute 'exit'. (id=" + getId() + "). Attribute is only allowed for direct child of sequence element");
        }
        loadDefinition(def);
    }

    public WrappingDefinition getWrapping() {
        return wrapping;
    }

    protected abstract ElementTypeLookupCache createLookupCache();

    protected abstract ElementTypeParser createParser();

    public void setDefaultFormatting(FormattingDefinition defaultFormatting) {
        formatting = FormattingDefinitionFactory.mergeDefinitions(formatting, defaultFormatting);
    }

    protected void loadDefinition(Element def) throws ElementTypeDefinitionException {
        String attributesString = def.getAttributeValue("attributes");
        if (StringUtil.isNotEmptyOrSpaces(attributesString)) {
            attributes =  new ElementTypeAttributesBundle(attributesString);
        }

        isVirtualObjectInsideLookup = Boolean.parseBoolean(def.getAttributeValue("virtual-object-inside-lookup"));
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
                beginTokenElement = new TokenElementTypeImpl(bundle, this, beginTokenId, getId());
                endTokenElement = new TokenElementTypeImpl(bundle, this, endTokenId, getId());
            }
        } else {
            WrapperElementTypeTemplate template = WrapperElementTypeTemplate.valueOf(templateId);
            String beginTokenId = template.getBeginToken();
            String endTokenId = template.getEndToken();
            beginTokenElement = new TokenElementTypeImpl(bundle, this, beginTokenId, getId());
            endTokenElement = new TokenElementTypeImpl(bundle, this, endTokenId, getId());

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

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Icon getIcon() {
        return icon;
    }

    public ElementType getParent() {
        return parent;
    }

    public synchronized ElementTypeLookupCache getLookupCache() {
        if (lookupCache == null) {
            lookupCache = createLookupCache();
        }
        return lookupCache;
    }

    public synchronized  @NotNull ElementTypeParser getParser() {
        if (parser == null) {
            parser = createParser();
        }

        return parser;
    }

    public boolean is(ElementTypeAttribute attribute) {
        return attributes.is(attribute);
    }

    public ElementTypeAttributesBundle getAttributes() {
        return attributes;
    }

    public FormattingDefinition getFormatting() {
        return formatting;
    }

    @NotNull
    public DBLanguage getLanguage() {
        return getLanguageDialect().getBaseLanguage();
    }

    @Override
    public DBLanguageDialect getLanguageDialect() {
        return (DBLanguageDialect) super.getLanguage();
    }

    public ElementTypeBundle getElementBundle() {
        return bundle;
    }

    public String toString() {
        return getId();
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    public void registerVirtualObject(DBObjectType objectType) {
        getLookupCache().registerVirtualObject(objectType);
    }

    /*********************************************************
     *                  Virtual Object                       *
     *********************************************************/
    public boolean isVirtualObject() {
        return virtualObjectType != null;
    }

    public DBObjectType getVirtualObjectType() {
        return virtualObjectType;
    }

    public boolean isVirtualObjectInsideLookup() {
        return isVirtualObjectInsideLookup;
    }
}
