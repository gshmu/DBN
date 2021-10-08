package com.dci.intellij.dbn.code.common.completion;

import com.dci.intellij.dbn.code.common.completion.options.filter.CodeCompletionFilterSettings;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.ElementTypeBundle;
import com.dci.intellij.dbn.language.common.element.cache.ElementLookupContext;
import com.dci.intellij.dbn.language.common.element.cache.ElementTypeLookupCache;
import com.dci.intellij.dbn.language.common.element.impl.ElementTypeBase;
import com.dci.intellij.dbn.language.common.element.impl.IdentifierElementType;
import com.dci.intellij.dbn.language.common.element.impl.LeafElementType;
import com.dci.intellij.dbn.language.common.element.impl.QualifiedIdentifierVariant;
import com.dci.intellij.dbn.language.common.element.impl.TokenElementType;
import com.dci.intellij.dbn.language.common.element.parser.Branch;
import com.dci.intellij.dbn.language.common.element.path.ASTPathNode;
import com.dci.intellij.dbn.language.common.element.path.PathNode;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.language.common.psi.LeafPsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.language.common.psi.QualifiedIdentifierPsiElement;
import com.dci.intellij.dbn.language.common.psi.lookup.AliasDefinitionLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.LookupAdapterCache;
import com.dci.intellij.dbn.language.common.psi.lookup.PsiLookupAdapter;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.object.common.DBObjectPsiElement;
import com.dci.intellij.dbn.object.common.DBVirtualObject;
import com.dci.intellij.dbn.object.common.ObjectTypeFilter;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.FileElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.Consumer;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;

public class CodeCompletionProvider extends CompletionProvider<CompletionParameters> {
    public static final CodeCompletionProvider INSTANCE = new CodeCompletionProvider();


    private CodeCompletionProvider() {
        super();
    }

    @Override
    protected void addCompletions(
            @NotNull CompletionParameters parameters,
            @NotNull ProcessingContext processingContext,
            @NotNull CompletionResultSet result) {
        PsiFile originalFile = parameters.getOriginalFile();
        if (originalFile instanceof DBLanguagePsiFile) {
            DBLanguagePsiFile file = (DBLanguagePsiFile) originalFile;

            CodeCompletionContext context = new CodeCompletionContext(file, parameters, result);
            CodeCompletionLookupConsumer consumer = new CodeCompletionLookupConsumer(context);

            int caretOffset = parameters.getOffset();
            if (file.findElementAt(caretOffset) instanceof PsiComment) return;

            LeafPsiElement leafAtOffset = caretOffset == 0 ? null : PsiUtil.lookupLeafAtOffset(file, caretOffset-1);
            LeafPsiElement leafBeforeCaret = leafAtOffset == null || leafAtOffset.isCharacterToken() ?
                    PsiUtil.lookupLeafBeforeOffset(file, caretOffset) :
                    PsiUtil.lookupLeafBeforeOffset(file, leafAtOffset.getTextOffset());


            int invocationCount = parameters.getInvocationCount();
            if (invocationCount > 1) context.setExtended(true);

            //Timeout.run(1, true, () -> collectCompletionVariants(consumer, leafBeforeCaret));
            collectCompletionVariants(consumer, leafBeforeCaret);
        }
    }

    private void collectCompletionVariants(CodeCompletionLookupConsumer consumer, LeafPsiElement leafBeforeCaret) {
        if (leafBeforeCaret == null) {
            collectRootCompletionVariants(consumer);
        } else {
            leafBeforeCaret = (LeafPsiElement) leafBeforeCaret.getOriginalElement();
            collectElementRelativeVariants(leafBeforeCaret, consumer);
        }
    }

    private static void collectRootCompletionVariants(CodeCompletionLookupConsumer consumer) {
        CodeCompletionContext context = consumer.getContext();
        DBLanguagePsiFile file = context.getFile();

        ElementTypeBundle elementTypeBundle = file.getElementTypeBundle();
        ElementTypeLookupCache lookupCache = elementTypeBundle.getRootElementType().getLookupCache();
        ElementLookupContext lookupContext = new ElementLookupContext(context.getDatabaseVersion());
        Set<LeafElementType> firstPossibleLeafs = lookupCache.captureFirstPossibleLeafs(lookupContext);

        for (LeafElementType firstPossibleLeaf : firstPossibleLeafs) {
            if (firstPossibleLeaf instanceof TokenElementType) {
                TokenElementType tokenElementType = (TokenElementType) firstPossibleLeaf;
                consumer.consume(tokenElementType);
            }
        }
    }

    private static void collectElementRelativeVariants(LeafPsiElement element, CodeCompletionLookupConsumer consumer) {
        CodeCompletionContext context = consumer.getContext();

        IdentifierPsiElement parentIdentifierPsiElement = null;

        DBObject parentObject = null;
        PsiElement parent = element.getParent();
        if (parent instanceof QualifiedIdentifierPsiElement) {
            QualifiedIdentifierPsiElement qualifiedIdentifier = (QualifiedIdentifierPsiElement) parent;
            ElementType separator = qualifiedIdentifier.getElementType().getSeparatorToken();

            if (element.getElementType() == separator){
                BasePsiElement parentPsiElement = element.getPrevElement();
                if (parentPsiElement instanceof IdentifierPsiElement) {
                    parentIdentifierPsiElement = (IdentifierPsiElement) parentPsiElement;
                    parentObject = parentIdentifierPsiElement.getUnderlyingObject();

                    if (parentObject != null) {
                        for (QualifiedIdentifierVariant parseVariant : qualifiedIdentifier.getParseVariants()){
                            boolean match = parseVariant.matchesPsiElement(qualifiedIdentifier);
                            if (match) {
                                int index = qualifiedIdentifier.getIndexOf(parentIdentifierPsiElement);
                                LeafElementType leafElementType = parseVariant.getLeaf(index + 1);
                                context.addCompletionCandidate(leafElementType);
                            }
                        }
                    }
                }
            }
        } else if (element.getElementType().getTokenType() == element.getLanguage().getSharedTokenTypes().getChrDot()) {
            LeafPsiElement parentPsiElement = element.getPrevLeaf();
            if (parentPsiElement instanceof IdentifierPsiElement) {
                parentIdentifierPsiElement = (IdentifierPsiElement) parentPsiElement;
                parentObject = parentIdentifierPsiElement.getUnderlyingObject();
            }

        } else if (parent instanceof BasePsiElement) {
            BasePsiElement basePsiElement = (BasePsiElement) parent;
            ElementTypeBase elementType = basePsiElement.getElementType();
            if (elementType.isWrappingBegin((LeafElementType) element.getElementType())) {
                Set<LeafElementType> candidates = elementType.getLookupCache().getFirstPossibleLeafs();
                candidates.forEach(candidate -> context.addCompletionCandidate(candidate));
            }
        }

        if (!context.hasCompletionCandidates()) {
            LeafElementType elementType = (LeafElementType) element.getElementType();
            PathNode pathNode = new ASTPathNode(element.node);
            ElementLookupContext lookupContext = computeParseBranches(element.node, context.getDatabaseVersion());
            if (!context.isNewLine()) {
                lookupContext.addBreakOnAttribute(ElementTypeAttribute.STATEMENT);
            }
            Set<LeafElementType> candidates = elementType.getNextPossibleLeafs(pathNode, lookupContext);
            candidates.forEach(candidate -> context.addCompletionCandidate(candidate));
        }

        context.setParentIdentifierPsiElement(parentIdentifierPsiElement);
        context.setParentObject(parentObject);

        collectTokenElements(consumer);
        collectIdentifierElements(element, consumer);
        context.awaitCompletion();
    }

    private static void collectTokenElements(CodeCompletionLookupConsumer consumer) {
        CodeCompletionContext context = consumer.getContext();
        context.queue(() -> {
            Collection<LeafElementType> completionCandidates = context.getCompletionCandidates();
            completionCandidates.stream().filter(elementType -> elementType instanceof TokenElementType).forEach(elementType -> {
                TokenElementType tokenElementType = (TokenElementType) elementType;
                //consumer.setAddParenthesis(addParenthesis && tokenType.isFunction());
                consumer.consume(tokenElementType);
            });
        });
    }

    private static void collectIdentifierElements(LeafPsiElement element, CodeCompletionLookupConsumer consumer) {
        CodeCompletionContext context = consumer.getContext();
        IdentifierPsiElement parentIdentifierPsiElement = context.getParentIdentifierPsiElement();
        DBObject parentObject = context.getParentObject();

        Collection<LeafElementType> completionCandidates = context.getCompletionCandidates();
        completionCandidates.stream().filter(elementType -> elementType instanceof IdentifierElementType).forEach(elementType -> {
            IdentifierElementType identifierElementType = (IdentifierElementType) elementType;
            if (identifierElementType.isReference()) {
                DBObjectType objectType = identifierElementType.getObjectType();
                if (parentIdentifierPsiElement == null) {
                    if (identifierElementType.isObject()) {
                        context.queue(() -> collectObjectElements(element, consumer, identifierElementType, objectType));

                    } else if (identifierElementType.isAlias()) {
                        context.queue(() -> collectAliasElements(element, consumer, objectType));

                    } else if (identifierElementType.isVariable()) {
                        context.queue(() -> collectVariableElements(element, consumer, objectType));
                    }
                }
                if (parentObject != null && (context.isLiveConnection() || parentObject instanceof DBVirtualObject)) {
                    context.queue(() -> collectChildObjects(consumer, parentObject, objectType));
                }
            } else if (identifierElementType.isDefinition()) {
                if (identifierElementType.isAlias()) {
                    context.queue(() -> buildAliasDefinitionNames(element, consumer));
                }
            }
        });
    }

    private static void collectObjectElements(LeafPsiElement element, CodeCompletionLookupConsumer consumer, IdentifierElementType identifierElementType, DBObjectType objectType) {
        CodeCompletionContext context = consumer.getContext();
        CodeCompletionFilterSettings filterSettings = context.getCodeCompletionFilterSettings();
        PsiLookupAdapter lookupAdapter = LookupAdapterCache.OBJECT_DEFINITION.get(objectType);
        lookupAdapter.collectInParentScopeOf(element, psiElement -> {
            if (psiElement instanceof IdentifierPsiElement) {
                IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) psiElement;
                PsiElement referencedPsiElement = identifierPsiElement.resolve();
                if (referencedPsiElement instanceof DBObjectPsiElement) {
                    DBObjectPsiElement objectPsiElement = (DBObjectPsiElement) referencedPsiElement;
                    consumer.consume(objectPsiElement);
                } else {
                    consumer.consume(identifierPsiElement);
                }
            }
        });

        BasePsiElement scope = element.getEnclosingScopePsiElement();
        if (scope != null) {
            collectObjectMatchingScope(consumer, identifierElementType, filterSettings, scope, context);
        }
    }

    private static void collectAliasElements(LeafPsiElement scopeElement, CodeCompletionLookupConsumer consumer, DBObjectType objectType) {
        PsiLookupAdapter lookupAdapter = LookupAdapterCache.ALIAS_DEFINITION.get(objectType);
        lookupAdapter.collectInParentScopeOf(scopeElement, psiElement -> consumer.consume(psiElement));
    }

    private static void collectVariableElements(LeafPsiElement scopeElement, CodeCompletionLookupConsumer consumer, DBObjectType objectType) {
        PsiLookupAdapter lookupAdapter = LookupAdapterCache.VARIABLE_DEFINITION.get(objectType);
        lookupAdapter.collectInParentScopeOf(scopeElement, psiElement -> consumer.consume(psiElement));
    }

    private static void collectChildObjects(CodeCompletionLookupConsumer consumer, DBObject object, DBObjectType objectType) {
        object.collectChildObjects(objectType, consumer);
    }

    @NotNull
    private static ElementLookupContext computeParseBranches(ASTNode astNode, double databaseVersion) {
        ElementLookupContext lookupContext = new ElementLookupContext(databaseVersion);
        while (astNode != null && !(astNode instanceof FileElement)) {
            IElementType elementType = astNode.getElementType();
            if (elementType instanceof ElementType) {
                ElementType basicElementType = (ElementType) elementType;
                Branch branch = basicElementType.getBranch();
                if (branch != null) {
                    lookupContext.addBranchMarker(astNode, branch);
                }
            }
            ASTNode prevAstNode = astNode.getTreePrev();
            if (prevAstNode == null) {
                prevAstNode = astNode.getTreeParent();
            }
            astNode = prevAstNode;
        }
        return lookupContext;
    }

    private static void buildAliasDefinitionNames(BasePsiElement aliasElement, CodeCompletionLookupConsumer consumer) {
        IdentifierPsiElement aliasedObject = PsiUtil.lookupObjectPriorTo(aliasElement, DBObjectType.ANY);
        if (aliasedObject != null && aliasedObject.isObject()) {
            CharSequence unquotedText = aliasedObject.getUnquotedText();
            if (unquotedText.length() > 0) {
                String[] aliasNames = NamingUtil.createAliasNames(unquotedText);

                BasePsiElement scope = aliasElement.getEnclosingScopePsiElement();

                for (int i = 0; i< aliasNames.length; i++) {
                    while (true) {
                        PsiLookupAdapter lookupAdapter = new AliasDefinitionLookupAdapter(null, DBObjectType.ANY, aliasNames[i]);
                        boolean isExisting = scope != null && lookupAdapter.findInScope(scope) != null;
                        boolean isKeyword = aliasElement.getLanguageDialect().isReservedWord(aliasNames[i]);
                        if (isKeyword || isExisting) {
                            aliasNames[i] = NamingUtil.getNextNumberedName(aliasNames[i], false);
                        } else {
                            break;
                        }
                    }
                }
                consumer.consume(aliasNames);
            }
        }
    }

    private static void collectObjectMatchingScope(
            Consumer consumer,
            IdentifierElementType identifierElementType,
            ObjectTypeFilter filter,
            @NotNull  BasePsiElement sourceScope,
            CodeCompletionContext context) {
        DBObjectType objectType = identifierElementType.getObjectType();
        PsiElement sourceElement = context.getElementAtCaret();
        ConnectionHandler connectionHandler = context.getConnectionHandler();

        if (Failsafe.check(connectionHandler) && !connectionHandler.isVirtual()) {
            DBObjectBundle objectBundle = connectionHandler.getObjectBundle();
            if (sourceElement.getParent() instanceof QualifiedIdentifierPsiElement && sourceElement.getParent().getFirstChild() != sourceElement) {
                QualifiedIdentifierPsiElement qualifiedIdentifierPsiElement = (QualifiedIdentifierPsiElement) sourceElement.getOriginalElement().getParent();
                DBObject parentObject = qualifiedIdentifierPsiElement.lookupParentObjectFor(identifierElementType);
                if (parentObject != null) {
                    DBSchema currentSchema = PsiUtil.getDatabaseSchema(sourceScope);
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
                        DBSchema currentSchema = PsiUtil.getDatabaseSchema(sourceScope);
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
