package com.dci.intellij.dbn.language.common.element;

import com.dci.intellij.dbn.common.thread.Background;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.TokenTypeBundle;
import com.dci.intellij.dbn.language.common.element.impl.BasicElementType;
import com.dci.intellij.dbn.language.common.element.impl.BlockElementType;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeBase;
import com.dci.intellij.dbn.language.common.element.impl.ExecVariableElementType;
import com.dci.intellij.dbn.language.common.element.impl.IdentifierElementType;
import com.dci.intellij.dbn.language.common.element.impl.IterationElementType;
import com.dci.intellij.dbn.language.common.element.impl.LeafElementType;
import com.dci.intellij.dbn.language.common.element.impl.NamedElementType;
import com.dci.intellij.dbn.language.common.element.impl.OneOfElementType;
import com.dci.intellij.dbn.language.common.element.impl.QualifiedIdentifierElementType;
import com.dci.intellij.dbn.language.common.element.impl.SequenceElementType;
import com.dci.intellij.dbn.language.common.element.impl.TokenElementType;
import com.dci.intellij.dbn.language.common.element.impl.WrapperElementType;
import com.dci.intellij.dbn.language.common.element.impl.WrappingDefinition;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeDefinition;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeDefinitionException;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.intellij.openapi.ide.CopyPasteManager;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import lombok.extern.slf4j.Slf4j;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;

@Slf4j
public class ElementTypeBundle {
    private final TokenTypeBundle tokenTypeBundle;
    private BasicElementType unknownElementType;
    private NamedElementType rootElementType;

    private final DBLanguageDialect languageDialect;
    private int idCursor;

    private transient Builder builder = new Builder();
    private final Map<String, NamedElementType> namedElementTypes = new THashMap<>();


    private static class Builder {
        private final Set<LeafElementType> leafElementTypes = new THashSet<>();
        private final Set<WrapperElementType> wrapperElementTypes = new THashSet<>();
        private final Set<ElementType> wrappedElementTypes = new THashSet<>();
        //private Set<OneOfElementType> oneOfElementTypes = new THashSet<OneOfElementType>();
        private final Set<ElementType> allElementTypes = new THashSet<>();
        private boolean rewriteIndexes;
    }


    public ElementTypeBundle(DBLanguageDialect languageDialect, TokenTypeBundle tokenTypeBundle, final Document elementTypesDef) {
        this.languageDialect = languageDialect;
        this.tokenTypeBundle = tokenTypeBundle;
        try {
            Element root = elementTypesDef.getRootElement();
            for (Element child : root.getChildren()) {
                createNamedElementType(child);
            }

            NamedElementType unknown = namedElementTypes.get("custom_undefined");
            for(NamedElementType namedElementType : namedElementTypes.values()){
                if (!namedElementType.isDefinitionLoaded()) {
                    namedElementType.update(unknown);
                    //log.info("ERROR: element '" + namedElementType.getId() + "' not defined.");
                    System.out.println("DEBUG - [" + this.languageDialect.getID() + "] undefined element type: " + namedElementType.getId());
/*
                    if (DatabaseNavigator.getInstance().isDebugModeEnabled()) {
                        System.out.println("WARNING - [" + getLanguageDialect().getID() + "] undefined element type: " + namedElementType.getId());
                    }
*/
                }
            }

            for (LeafElementType leafElementType: builder.leafElementTypes) {
                leafElementType.registerLeaf();
            }

            for (WrapperElementType wrapperElementType : builder.wrapperElementTypes) {
                wrapperElementType.getBeginTokenElement().registerLeaf();
                wrapperElementType.getEndTokenElement().registerLeaf();
            }

            for (ElementType wrappedElementType : builder.wrappedElementTypes) {
                WrappingDefinition wrapping = wrappedElementType.getWrapping();
                wrapping.getBeginElementType().registerLeaf();
                wrapping.getEndElementType().registerLeaf();
            }

            if (builder.rewriteIndexes) {
                StringWriter stringWriter = new StringWriter();
                new XMLOutputter().output(elementTypesDef, stringWriter);

                String data = stringWriter.getBuffer().toString();
                System.out.println(data);

                CopyPasteManager copyPasteManager = CopyPasteManager.getInstance();
                copyPasteManager.setContents(new StringSelection(data));
            } else {
            }

            Background.run(() -> {
                for (ElementType allElementType : builder.allElementTypes) {
                    allElementType.getLookupCache().cleanup();
                }
                builder = null;
            });

            //warnAmbiguousBranches();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public TokenTypeBundle getTokenTypeBundle() {
        return tokenTypeBundle;
    }

    public void markIndexesDirty() {
        builder.rewriteIndexes = true;
    }

    private void createNamedElementType(Element def) throws ElementTypeDefinitionException {
        String id = determineMandatoryAttribute(def, "id", "Invalid definition of named element type.");
        String languageId = def.getAttributeValue("language");
        log.debug("Updating complex element definition '" + id + '\'');
        NamedElementType elementType = getNamedElementType(id, null);
        elementType.loadDefinition(def);
        if (elementType.is(ElementTypeAttribute.ROOT)) {
            DBLanguage language = DBLanguage.getLanguage(languageId);
            if (language == languageDialect.getBaseLanguage()) {
                if (rootElementType == null) {
                    rootElementType = elementType;
                } else {
                    throw new ElementTypeDefinitionException("Duplicate root definition");
                }
            }
        }
    }


    public static String determineMandatoryAttribute(Element def, String attribute, String message) throws ElementTypeDefinitionException {
        String value = def.getAttributeValue(attribute);
        if (value == null) {
            throw new ElementTypeDefinitionException(message + "Missing '" + attribute + "' attribute.");
        }
        return value;
    }

    public ElementTypeBase resolveElementDefinition(Element def, String type, ElementTypeBase parent) throws ElementTypeDefinitionException {
        ElementTypeBase result;
        if (ElementTypeDefinition.SEQUENCE.is(type)){
            result = new SequenceElementType(this, parent, createId(), def);
            log.debug("Created sequence element definition");

        } else if (ElementTypeDefinition.BLOCK.is(type)) {
            result = new BlockElementType(this, parent, createId(), def);
            log.debug("Created iteration element definition");

        } else if (ElementTypeDefinition.ITERATION.is(type)) {
            result = new IterationElementType(this, parent, createId(), def);
            log.debug("Created iteration element definition");

        } else if (ElementTypeDefinition.ONE_OF.is(type)) {
            result = new OneOfElementType(this, parent, createId(), def);
            //builder.oneOfElementTypes.add((OneOfElementType) result);
            log.debug("Created one-of element definition");

        } else if (ElementTypeDefinition.QUALIFIED_IDENTIFIER.is(type)) {
            result =  new QualifiedIdentifierElementType(this, parent, createId(), def);
            log.debug("Created qualified identifier element definition");

        } else if (ElementTypeDefinition.WRAPPER.is(type)) {
            result = new WrapperElementType(this, parent, createId(), def);
            builder.wrapperElementTypes.add((WrapperElementType) result);
            log.debug("Created wrapper element definition");

        } else if (ElementTypeDefinition.ELEMENT.is(type)) {
            String id = determineMandatoryAttribute(def, "ref-id", "Invalid reference to element.");
            result = getNamedElementType(id, parent);

        } else if (ElementTypeDefinition.TOKEN.is(type)) {
            result = new TokenElementType(this, parent, createId(), def);

        } else if (
                ElementTypeDefinition.OBJECT_DEF.is(type) ||
                ElementTypeDefinition.OBJECT_REF.is(type) ||
                ElementTypeDefinition.ALIAS_DEF.is(type) ||
                ElementTypeDefinition.ALIAS_REF.is(type) ||
                ElementTypeDefinition.VARIABLE_DEF.is(type) ||
                ElementTypeDefinition.VARIABLE_REF.is(type)) {
            result = new IdentifierElementType(this, parent, createId(), def);

        } else if (ElementTypeDefinition.EXEC_VARIABLE.is(type)) {
            result = new ExecVariableElementType(this, parent, createId(), def);

        }  else {
            throw new ElementTypeDefinitionException("Could not resolve element definition '" + type + '\'');
        }
        if (result instanceof LeafElementType) {
            builder.leafElementTypes.add((LeafElementType) result);
        }

        builder.allElementTypes.add(result);

        WrappingDefinition wrapping = result.getWrapping();
        if (wrapping != null) {
            builder.wrappedElementTypes.add(result);
        }
        return result;
    }


    @NotNull
    public static DBObjectType resolveObjectType(String name) throws ElementTypeDefinitionException {
        DBObjectType objectType = DBObjectType.get(name);
        if (objectType == null)
            throw new ElementTypeDefinitionException("Invalid object type '" + name + "'");
        return objectType;
    }


    /*protected synchronized TokenElementType getTokenElementType(String id) {
        TokenElementType elementType = tokenElementTypes.get(id);
        if (elementType == null) {
            elementType = new TokenElementType(this, id);
            tokenElementTypes.put(id, elementType);
            log.info("Created token element objectType '" + id + "'");
        }
        return elementType;
    }*/

    private NamedElementType getNamedElementType(String id, ElementTypeBase parent) {
        NamedElementType elementType = namedElementTypes.get(id);
        if (elementType == null) {
            synchronized (this) {
                elementType = namedElementTypes.get(id);
                if (elementType == null) {
                    elementType = new NamedElementType(this, id);
                    namedElementTypes.put(id, elementType);
                    builder.allElementTypes.add(elementType);
                    log.debug("Created named element type '" + id + '\'');
                }
            }
        }

        if (parent != null) elementType.addParent(parent);
        return elementType;
    }

    public DBLanguageDialect getLanguageDialect() {
        return languageDialect;
    }

    public NamedElementType getRootElementType() {
        return rootElementType;
    }

    public NamedElementType getNamedElementType(String id) {
        return namedElementTypes.get(id);
    }

    public BasicElementType getUnknownElementType() {
        if (unknownElementType == null) {
            unknownElementType = new BasicElementType(this);
        }
        return unknownElementType;
    }

    private String createId() {
        String id = Integer.toString(idCursor++);
        StringBuilder buffer = new StringBuilder();
        while (buffer.length() + id.length() < 9) {
            buffer.append('0');
        }
        buffer.append(id);
        return buffer.toString();
    }
}
