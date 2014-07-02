package com.dci.intellij.dbn.code.common.lookup;

import com.dci.intellij.dbn.code.common.completion.CodeCompletionContext;
import com.dci.intellij.dbn.code.common.completion.CodeCompletionContributor;
import com.dci.intellij.dbn.code.common.completion.CodeCompletionLookupConsumer;
import com.dci.intellij.dbn.code.common.style.DBLCodeStyleManager;
import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseOption;
import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseSettings;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.object.DBSynonym;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBVirtualObject;
import com.intellij.openapi.project.Project;

import javax.swing.Icon;

public class DBObjectLookupItemFactory extends LookupItemFactory {
    private DBLanguage language;
    private DBObject object;
    private String typeName;

    public DBObjectLookupItemFactory(DBObject object, DBLanguage language) {
        this.object = object;
        this.language = language;
    }

    @Override
    public DBLookupItem createLookupItem(Object source, CodeCompletionLookupConsumer consumer) {
        DBLookupItem lookupItem = super.createLookupItem(source, consumer);

        if (lookupItem != null) {
            if (object.needsNameQuoting()) {
                char quoteChar = DatabaseCompatibilityInterface.getInstance(object).getIdentifierQuotes();
                String lookupString = quoteChar + lookupItem.getLookupString() + quoteChar;
                lookupItem.setLookupString(lookupString);
            }


/*
            lookupItem.setInsertHandler(consumer.isAddParenthesis() ?
                                BracketsInsertHandler.INSTANCE :
                                BasicInsertHandler.INSTANCE);
*/

        }
        return lookupItem;
    }

    public DBObject getObject() {
        return object;
    }

    public String getTextHint() {
        if (typeName == null) {
            DBObject parentObject = object.getParentObject();

            String typePrefix = "";
            if (object instanceof DBSynonym) {
                DBSynonym synonym = (DBSynonym) object;
                DBObject underlyingObject = synonym.getUnderlyingObject();
                if (underlyingObject != null) {
                    typePrefix = underlyingObject.getTypeName() + " ";
                }
            }

            typeName = parentObject == null ?
                    typePrefix + object.getTypeName() :
                    typePrefix + object.getTypeName() + " (" +
                       parentObject.getTypeName() + " " +
                       parentObject.getName() + ")";
        }
        return typeName;
    }

    public boolean isBold() {
        return false;
    }

    @Override
    public CharSequence getText(CodeCompletionContext context) {
        Project project = context.getFile().getProject();
        CodeStyleCaseSettings styleCaseSettings = DBLCodeStyleManager.getInstance(project).getCodeStyleCaseSettings(language);
        CodeStyleCaseOption caseOption = styleCaseSettings.getObjectCaseOption();
        String text = caseOption.changeCase(object.getName());

        if (object instanceof DBVirtualObject && text.contains(CodeCompletionContributor.DUMMY_TOKEN)) {
            return null;
        }

        String userInput = context.getUserInput();
        if (userInput != null && userInput.length() > 0 && !text.startsWith(userInput)) {
            char firstInputChar = userInput.charAt(0);
            char firstPresentationChar = text.charAt(0);

            if (Character.toUpperCase(firstInputChar) == Character.toUpperCase(firstPresentationChar)) {
                boolean upperCaseInput = Character.isUpperCase(firstInputChar);
                boolean upperCasePresentation = Character.isUpperCase(firstPresentationChar);

                if (StringUtil.isMixedCase(text)) {
                    if (upperCaseInput != upperCasePresentation) {
                        text = upperCaseInput ?
                                text.toUpperCase() :
                                text.toLowerCase();
                    }
                } else {
                    text = upperCaseInput ?
                            text.toUpperCase() :
                            text.toLowerCase();
                }
            } else {
                return null;
            }
        }

        return text;
    }

    public Icon getIcon() {
        return object.getIcon();
    }

    @Override
    public void dispose() {
        object = null;
        language = null;
    }
}
