package com.dci.intellij.dbn.code.common.completion;

import com.dci.intellij.dbn.code.common.completion.options.filter.CodeCompletionFilterSettings;
import com.dci.intellij.dbn.code.common.lookup.AliasLookupItemBuilder;
import com.dci.intellij.dbn.code.common.lookup.BasicLookupItemBuilder;
import com.dci.intellij.dbn.code.common.lookup.IdentifierLookupItemBuilder;
import com.dci.intellij.dbn.code.common.lookup.LookupItemBuilder;
import com.dci.intellij.dbn.code.common.lookup.VariableLookupItemBuilder;
import com.dci.intellij.dbn.common.consumer.CancellableConsumer;
import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.TokenTypeCategory;
import com.dci.intellij.dbn.language.common.element.impl.TokenElementType;
import com.dci.intellij.dbn.language.common.element.util.IdentifierType;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectPsiElement;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.intellij.openapi.progress.ProcessCanceledException;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Collection;

public class CodeCompletionLookupConsumer implements CancellableConsumer<Object> {
    private final CodeCompletionContext context;
    private @Getter @Setter @Deprecated boolean addParenthesis;

    CodeCompletionLookupConsumer(CodeCompletionContext context) {
        this.context = context;
    }

    @Override
    public void consume(Object object) {
        try {
            if (object instanceof Object[]) {
                consumeArray((Object[]) object);

            } else if (object instanceof Collection) {
                consumeCollection((Collection) object);

            } else {
                checkCancelled();
                LookupItemBuilder lookupItemBuilder = null;
                DBLanguage language = context.getLanguage();
                if (object instanceof DBObject) {
                    DBObject dbObject = (DBObject) object;
                    lookupItemBuilder = dbObject.getLookupItemBuilder(language);
                }
                else if (object instanceof DBObjectPsiElement) {
                    DBObjectPsiElement objectPsiElement = (DBObjectPsiElement) object;
                    lookupItemBuilder = objectPsiElement.ensureObject().getLookupItemBuilder(language);

                } else if (object instanceof TokenElementType) {
                    TokenElementType tokenElementType = (TokenElementType) object;
                    String text = tokenElementType.getText();
                    if (StringUtil.isNotEmpty(text)) {
                        lookupItemBuilder = tokenElementType.getLookupItemBuilder(language);
                    } else {
                        CodeCompletionFilterSettings filterSettings = context.getCodeCompletionFilterSettings();
                        TokenTypeCategory tokenTypeCategory = tokenElementType.getTokenTypeCategory();
                        if (tokenTypeCategory == TokenTypeCategory.OBJECT) {
                            TokenType tokenType = tokenElementType.tokenType;
                            DBObjectType objectType = tokenType.getObjectType();
                            if (objectType != null) {
                                if (filterSettings.acceptsRootObject(objectType)) {
                                    lookupItemBuilder = new BasicLookupItemBuilder(tokenType.getValue(), objectType.getName(), objectType.getIcon());
                                }
                            }
                        } else if (filterSettings.acceptReservedWord(tokenTypeCategory)) {
                            lookupItemBuilder = tokenElementType.getLookupItemBuilder(language);
                        }
                    }
                } else if (object instanceof IdentifierPsiElement) {
                    IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) object;
                    if (identifierPsiElement.isValid()) {
                        CharSequence chars = identifierPsiElement.getChars();
                        IdentifierType identifierType = identifierPsiElement.getIdentifierType();
                        if (identifierType == IdentifierType.VARIABLE) {
                            lookupItemBuilder = new VariableLookupItemBuilder(chars, true);
                        } else if (identifierType == IdentifierType.ALIAS) {
                            lookupItemBuilder = new AliasLookupItemBuilder(chars, true);
                        } else if (identifierType == IdentifierType.OBJECT && identifierPsiElement.isDefinition()) {
                            lookupItemBuilder = new IdentifierLookupItemBuilder(identifierPsiElement);

                        }
                    }
                } else if (object instanceof String) {
                    lookupItemBuilder = new AliasLookupItemBuilder((CharSequence) object, true);
                }

                if (lookupItemBuilder != null) {
                    lookupItemBuilder.createLookupItem(object, this);
                }
            }
        } catch (ProcessCanceledException ignore) {}
    }

    private void consumeArray(Object[] array) {
        checkCancelled();
        if (array != null && array.length > 0) {
            Arrays.stream(array).forEach(element -> {
                checkCancelled();
                consume(element);
            });
        }
    }

    private void consumeCollection(Collection<Object> objects) {
        checkCancelled();
        if (objects != null && !objects.isEmpty()) {
            objects.forEach(element -> {
                checkCancelled();
                consume(element);
            });
        }
    }

    public void checkCancelled() throws ProcessCanceledException{
        if (context.getResult().isStopped() || context.isFinished()) {
            throw AlreadyDisposedException.INSTANCE;
        }
    }

    public CodeCompletionContext getContext() {
        return context;
    }
}
