package com.dci.intellij.dbn.code.common.completion;

import com.dci.intellij.dbn.code.common.completion.options.filter.CodeCompletionFilterSettings;
import com.dci.intellij.dbn.code.common.lookup.AliasLookupItemFactory;
import com.dci.intellij.dbn.code.common.lookup.LookupItemFactory;
import com.dci.intellij.dbn.code.common.lookup.VariableLookupItemFactory;
import com.dci.intellij.dbn.common.content.DatabaseLoadMonitor;
import com.dci.intellij.dbn.common.lookup.ConsumerStoppedException;
import com.dci.intellij.dbn.common.lookup.LookupConsumer;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.DBLanguageFile;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.ElementTypeBundle;
import com.dci.intellij.dbn.language.common.element.IdentifierElementType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.TokenElementType;
import com.dci.intellij.dbn.language.common.element.impl.QualifiedIdentifierVariant;
import com.dci.intellij.dbn.language.common.element.path.ASTPathNode;
import com.dci.intellij.dbn.language.common.element.path.PathNode;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.language.common.psi.LeafPsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.language.common.psi.QualifiedIdentifierPsiElement;
import com.dci.intellij.dbn.language.common.psi.lookup.AliasDefinitionLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.ObjectDefinitionLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.PsiLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.VariableDefinitionLookupAdapter;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.ObjectTypeFilter;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public class CodeCompletionProvider extends CompletionProvider<CompletionParameters> {
    public static final CodeCompletionProvider INSTANCE = new CodeCompletionProvider();


    public CodeCompletionProvider() {
        super();
    }

    @Override
    protected void addCompletions(
            @NotNull CompletionParameters parameters,
            ProcessingContext processingContext,
            @NotNull CompletionResultSet result) {
        try {
            DatabaseLoadMonitor.setEnsureDataLoaded(false);
            doAddCompletions(parameters, result);
        } finally {
            DatabaseLoadMonitor.setEnsureDataLoaded(true);
        }

    }

    private void doAddCompletions(CompletionParameters parameters, CompletionResultSet result) {
        PsiFile originalFile = parameters.getOriginalFile();
        if (originalFile instanceof DBLanguageFile) {
            DBLanguageFile file = (DBLanguageFile) originalFile;

            CodeCompletionContext context = new CodeCompletionContext(file, parameters, result);
            CodeCompletionLookupConsumer consumer = new CodeCompletionLookupConsumer(context);
            DBLanguage language = context.getLanguage();


            int caretOffset = parameters.getOffset();
            if (file.findElementAt(caretOffset) instanceof PsiComment) return;

            LeafPsiElement leafBeforeCaret = PsiUtil.lookupLeafBeforeOffset(file, caretOffset);


            int invocationCount = parameters.getInvocationCount();
            if (invocationCount > 1) context.setExtended(true);

            if (leafBeforeCaret == null) {
                ElementTypeBundle elementTypeBundle = file.getElementTypeBundle();
                Set<LeafElementType> firstPossibleLeafs = elementTypeBundle.getRootElementType().getLookupCache().getFirstPossibleLeafs();
                for (LeafElementType firstPossibleLeaf : firstPossibleLeafs) {
                    if (firstPossibleLeaf instanceof TokenElementType) {
                        TokenElementType tokenElementType = (TokenElementType) firstPossibleLeaf;
                        if (context.getCodeCompletionFilterSettings().acceptReservedWord(tokenElementType.getTokenTypeCategory())) {
                            LookupItemFactory lookupItemFactory = tokenElementType.getLookupItemFactory(language);
                            lookupItemFactory.createLookupItem(tokenElementType, consumer);
                        }
                    }
                }
            } else {
                leafBeforeCaret = (LeafPsiElement) leafBeforeCaret.getOriginalElement();
                try {
                    buildElementRelativeVariants(leafBeforeCaret, consumer);
                } catch (ConsumerStoppedException e) {

                }
            }
        }
    }

    private String getLeafUniqueKey(LeafElementType leaf) {
        if (leaf instanceof TokenElementType) {
            TokenElementType tokenElementType = (TokenElementType) leaf;
            return tokenElementType.getTokenType().getId();
        } else if (leaf instanceof IdentifierElementType){
            IdentifierElementType identifierElementType = (IdentifierElementType) leaf;
            return identifierElementType.getQualifiedObjectTypeName();
        }
        return null;
    }

    private void buildElementRelativeVariants(LeafPsiElement element, CodeCompletionLookupConsumer consumer) throws ConsumerStoppedException {

        CodeCompletionContext context = consumer.getContext();
        ConnectionHandler connectionHandler = context.getConnectionHandler();

        CodeCompletionFilterSettings filterSettings = context.getCodeCompletionFilterSettings();
        Map<String, LeafElementType> nextPossibleLeafs = new THashMap<String, LeafElementType>();
        DBObject parentObject = null;
        if (element.getParent() instanceof QualifiedIdentifierPsiElement) {
            QualifiedIdentifierPsiElement qualifiedIdentifier = (QualifiedIdentifierPsiElement) element.getParent();
            ElementType separator = qualifiedIdentifier.getElementType().getSeparatorToken();

            if (element.getElementType() == separator){
                BasePsiElement parentPsiElement = element.getPrevElement();
                if (parentPsiElement != null && parentPsiElement instanceof IdentifierPsiElement) {
                    IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) parentPsiElement;
                    parentObject = identifierPsiElement.resolveUnderlyingObject();

                    if (parentObject != null) {
                        for (QualifiedIdentifierVariant parseVariant : qualifiedIdentifier.getParseVariants()){
                            boolean match = parseVariant.matchesPsiElement(qualifiedIdentifier);
                            if (match) {
                                int index = qualifiedIdentifier.getIndexOf(identifierPsiElement);
                                LeafElementType leafElementType = parseVariant.getLeaf(index + 1);
                                if (leafElementType != null) {
                                    nextPossibleLeafs.put(getLeafUniqueKey(leafElementType), leafElementType);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (nextPossibleLeafs.size() == 0) {
            LeafElementType elementType = (LeafElementType) element.getElementType();
            PathNode pathNode = new ASTPathNode(element.getNode());
            for (LeafElementType leafElementType : elementType.getNextPossibleLeafs(pathNode, filterSettings)) {
                String leafUniqueKey = getLeafUniqueKey(leafElementType);
                if (leafUniqueKey != null) {
                    nextPossibleLeafs.put(leafUniqueKey, leafElementType);    
                }
            }
        }

        for (LeafElementType nextPossibleLeaf : nextPossibleLeafs.values()) {
            consumer.check();
            //boolean addParenthesis =
            //        nextPossibleLeaf.getLookupCache().getNextRequiredTokens().contains(
            //                element.getLanguage().getSharedTokenTypes().getLeftParenthesis());
            //consumer.setAddParenthesis(addParenthesis);

            if (nextPossibleLeaf instanceof TokenElementType) {
                TokenElementType tokenElementType = (TokenElementType) nextPossibleLeaf;
                //consumer.setAddParenthesis(addParenthesis && tokenType.isFunction());
                if (filterSettings.acceptReservedWord(tokenElementType.getTokenTypeCategory())) {
                    consumer.consume(tokenElementType);
                }
            }
            else if (nextPossibleLeaf instanceof IdentifierElementType) {
                IdentifierElementType identifierElementType = (IdentifierElementType) nextPossibleLeaf;
                if (identifierElementType.isReference()) {
                    DBObjectType objectType = identifierElementType.getObjectType();
                    if (identifierElementType.isObject()) {
                        PsiLookupAdapter lookupAdapter = new ObjectDefinitionLookupAdapter(null, objectType,  null);
                        Set<BasePsiElement> objectDefinitions = lookupAdapter.collectInParentScopeOf(element);
                        if (objectDefinitions != null) {
                            for (BasePsiElement psiElement : objectDefinitions) {
                                if (psiElement instanceof IdentifierPsiElement) {
                                    IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) psiElement;
                                    PsiElement referencedPsiElement = identifierPsiElement.resolve();
                                    if (referencedPsiElement instanceof DBObject) {
                                        DBObject object = (DBObject) referencedPsiElement;
                                        LookupItemFactory lookupItemFactory = object.getLookupItemFactory(identifierElementType.getLanguage());
                                        lookupItemFactory.createLookupItem(object, consumer);
                                    }
                                }
                            }
                        }
                        
                        if (connectionHandler != null && !connectionHandler.isVirtual()) {
                            if (parentObject == null) {
                                BasePsiElement scope = element.getEnclosingScopePsiElement();
                                collectObjectMatchingScope(consumer, identifierElementType, filterSettings, scope, context);
                            } else {
                                for (DBObject object : parentObject.getChildObjects(identifierElementType.getObjectType())) {
                                    consumer.check();
                                    consumer.consume(object);
                                }
                            }
                        }

                    } else if (identifierElementType.isAlias()) {
                        PsiLookupAdapter lookupAdapter = new AliasDefinitionLookupAdapter(null, objectType,  null);
                        Set<BasePsiElement> aliasDefinitions = lookupAdapter.collectInParentScopeOf(element);
                        if (aliasDefinitions != null) {
                            for (PsiElement psiElement : aliasDefinitions) {
                                IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) psiElement;
                                AliasLookupItemFactory lookupItemFactory = new AliasLookupItemFactory(identifierPsiElement.getChars(), true);
                                lookupItemFactory.createLookupItem(identifierPsiElement, consumer);
                            }
                        }
                    } else if (identifierElementType.isVariable()) {
                        PsiLookupAdapter lookupAdapter = new VariableDefinitionLookupAdapter(null, objectType, null);
                        Set<BasePsiElement> aliasDefinitions = lookupAdapter.collectInParentScopeOf(element);
                        if (aliasDefinitions != null) {
                            for (BasePsiElement psiElement : aliasDefinitions) {
                                IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) psiElement;
                                VariableLookupItemFactory lookupItemFactory = new VariableLookupItemFactory(identifierPsiElement.getChars(), true);
                                lookupItemFactory.createLookupItem(identifierPsiElement, consumer);
                            }
                        }
                    }
                } else if (identifierElementType.isDefinition()) {
                    if (identifierElementType.isAlias()) {
                        String[] aliasNames = buildAliasDefinitionNames(element);
                        for (String aliasName : aliasNames) {
                            AliasLookupItemFactory lookupItemFactory = new AliasLookupItemFactory(aliasName, true);
                            lookupItemFactory.createLookupItem(aliasName, consumer);
                        }
                    }
                }
            }
        }
    }

    public String[] buildAliasDefinitionNames(BasePsiElement aliasElement) {
        IdentifierPsiElement aliasedObject = PsiUtil.lookupObjectPriorTo(aliasElement, DBObjectType.ANY);
        if (aliasedObject != null && aliasedObject.isObject()) {
            String[] aliasNames = NamingUtil.createAliasNames(aliasedObject.getUnquotedText());

            BasePsiElement scope = aliasElement.getEnclosingScopePsiElement();

            for (int i = 0; i< aliasNames.length; i++) {
                while (true) {
                    PsiLookupAdapter lookupAdapter = new AliasDefinitionLookupAdapter(null, DBObjectType.ANY, aliasNames[i]);
                    boolean isExisting = lookupAdapter.findInScope(scope) != null;
                    boolean isKeyword = aliasElement.getLanguageDialect().isReservedWord(aliasNames[i]);
                    if (isKeyword || isExisting) {
                        aliasNames[i] = NamingUtil.getNextNumberedName(aliasNames[i], false);
                    } else {
                        break;
                    }
                }
            }
            return aliasNames;
        }
        return new String[0];
    }

    private void collectObjectMatchingScope(
            LookupConsumer consumer,
            IdentifierElementType identifierElementType,
            ObjectTypeFilter filter,
            BasePsiElement sourceScope,
            CodeCompletionContext context) throws ConsumerStoppedException {
        DBObjectType objectType = identifierElementType.getObjectType();
        PsiElement sourceElement = context.getElementAtCaret();
        ConnectionHandler connectionHandler = context.getConnectionHandler();

        if (connectionHandler != null ) {
            DBObjectBundle objectBundle = connectionHandler.getObjectBundle();
            if (sourceElement.getParent() instanceof QualifiedIdentifierPsiElement && sourceElement.getParent().getFirstChild() != sourceElement) {
                QualifiedIdentifierPsiElement qualifiedIdentifierPsiElement = (QualifiedIdentifierPsiElement) sourceElement.getOriginalElement().getParent();
                DBObject parentObject = qualifiedIdentifierPsiElement.lookupParentObjectFor(identifierElementType);
                if (parentObject != null) {
                    DBSchema currentSchema = PsiUtil.getCurrentSchema(sourceScope);
                    objectBundle.lookupChildObjectsOfType(
                            consumer,
                            parentObject,
                            objectType,
                            filter,
                            currentSchema);

                }
            } else if (!identifierElementType.isLocalReference()){
                Set<DBObject> parentObjects = LeafPsiElement.identifyPotentialParentObjects(objectType, filter, sourceScope, null);
                if (parentObjects != null && parentObjects.size() > 0) {
                    for (DBObject parentObject : parentObjects) {
                        DBSchema currentSchema = PsiUtil.getCurrentSchema(sourceScope);
                        objectBundle.lookupChildObjectsOfType(
                                consumer,
                                parentObject.getUndisposedElement(),
                                objectType,
                                filter,
                                currentSchema);
                    }
                } else {
                    if (filter.acceptsRootObject(objectType)) {
                        objectBundle.lookupObjectsOfType(
                                consumer,
                                objectType);
                    }
                }
            }
        }
    }
}
