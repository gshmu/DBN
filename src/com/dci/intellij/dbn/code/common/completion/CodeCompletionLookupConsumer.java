package com.dci.intellij.dbn.code.common.completion;

import com.dci.intellij.dbn.code.common.lookup.LookupItemFactory;
import com.dci.intellij.dbn.common.lookup.ConsumerStoppedException;
import com.dci.intellij.dbn.common.lookup.LookupConsumer;
import com.dci.intellij.dbn.language.common.element.TokenElementType;
import com.dci.intellij.dbn.object.common.DBObject;

import java.util.Collection;

public class CodeCompletionLookupConsumer implements LookupConsumer {
    private CodeCompletionContext context;
    boolean addParenthesis;

    public CodeCompletionLookupConsumer(CodeCompletionContext context) {
        this.context = context;
    }

    @Override
    public void consume(Object object) throws ConsumerStoppedException {
        check();

        LookupItemFactory lookupItemFactory = null;
        if (object instanceof DBObject) {
            DBObject dbObject = (DBObject) object;
            lookupItemFactory = dbObject.getLookupItemFactory(context.getLanguage());

        } else if (object instanceof TokenElementType) {
            TokenElementType tokenElementType = (TokenElementType) object;
            lookupItemFactory = tokenElementType.getLookupItemFactory(context.getLanguage());
        }

        if (lookupItemFactory != null) {
            lookupItemFactory.createLookupItem(object, this);
        }

    }

    public void consume(Collection objects) throws ConsumerStoppedException {
        check();
        for (Object object : objects) {
            consume(object);
        }
    }

    public void setAddParenthesis(boolean addParenthesis) {
        this.addParenthesis = addParenthesis;
    }

    @Override
    public void check() throws ConsumerStoppedException {
        if (context.getResult().isStopped()) {
            throw new ConsumerStoppedException();
        }
    }

    public CodeCompletionContext getContext() {
        return context;
    }

    public boolean isAddParenthesis() {
        return addParenthesis;
    }
}
