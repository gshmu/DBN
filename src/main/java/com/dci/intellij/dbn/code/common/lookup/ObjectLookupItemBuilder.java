package com.dci.intellij.dbn.code.common.lookup;

import com.dci.intellij.dbn.code.common.completion.CodeCompletionContext;
import com.dci.intellij.dbn.code.common.completion.CodeCompletionContributor;
import com.dci.intellij.dbn.code.common.completion.options.CodeCompletionSettings;
import com.dci.intellij.dbn.code.common.completion.options.general.CodeCompletionFormatSettings;
import com.dci.intellij.dbn.code.common.style.DBLCodeStyleManager;
import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseOption;
import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseSettings;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.QuotePair;
import com.dci.intellij.dbn.object.DBSynonym;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBVirtualObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ObjectLookupItemBuilder extends LookupItemBuilder {
    private final DBLanguage language;
    private final DBObjectRef objectRef;

    public ObjectLookupItemBuilder(DBObjectRef objectRef, DBLanguage language) {
        this.objectRef = objectRef;
        this.language = language;
    }

    @Override
    protected void adjustLookupItem(@NotNull CodeCompletionLookupItem lookupItem) {
        DBObject object = getObject();
        if (Failsafe.check(object)) {
            if (object.needsNameQuoting()) {
                DatabaseCompatibilityInterface compatibilityInterface = DatabaseCompatibilityInterface.getInstance(object);
                String lookupString = object.getName();
                QuotePair quotes = compatibilityInterface.getDefaultIdentifierQuotes();
                lookupString = quotes.quote(lookupString);
                lookupItem.setLookupString(lookupString);
            }


/*
            lookupItem.setInsertHandler(consumer.isAddParenthesis() ?
                                BracketsInsertHandler.INSTANCE :
                                BasicInsertHandler.INSTANCE);
*/

        }
    }

    @Nullable
    public DBObject getObject() {
        return DBObjectRef.get(objectRef);
    }

    @Override
    public String getTextHint() {
        DBObject object = getObject();
        if (object != null) {
            DBObject parentObject = object.getParentObject();

            String typePrefix = "";
            if (object instanceof DBSynonym) {
                DBSynonym synonym = (DBSynonym) object;
                DBObject underlyingObject = synonym.getUnderlyingObject();
                if (underlyingObject != null) {
                    typePrefix = underlyingObject.getTypeName() + ' ';
                }
            }

            return parentObject == null ?
                    typePrefix + object.getTypeName() :
                    typePrefix + object.getTypeName() + " (" +
                       parentObject.getTypeName() + ' ' +
                       parentObject.getName() + ')';
        }
        return "";
    }

    @Override
    public boolean isBold() {
        return false;
    }

    @Override
    public CharSequence getText(CodeCompletionContext context) {
        String text = objectRef.getObjectName();
        if (Strings.isNotEmptyOrSpaces(text)) {
            DBObject object = getObject();
            if (object instanceof DBVirtualObject && text.contains(CodeCompletionContributor.DUMMY_TOKEN)) {
                return null;
            }

            Project project = context.getFile().getProject();
            CodeStyleCaseSettings styleCaseSettings = DBLCodeStyleManager.getInstance(project).getCodeStyleCaseSettings(language);
            CodeStyleCaseOption caseOption = styleCaseSettings.getObjectCaseOption();

            text = caseOption.format(text);

            String userInput = context.getUserInput();
            CodeCompletionFormatSettings codeCompletionFormatSettings = CodeCompletionSettings.getInstance(project).getFormatSettings();
            if (Strings.isNotEmpty(userInput) && !text.startsWith(userInput) && !codeCompletionFormatSettings.isEnforceCodeStyleCase()) {
                char firstInputChar = userInput.charAt(0);
                char firstPresentationChar = text.charAt(0);

                if (Character.toUpperCase(firstInputChar) == Character.toUpperCase(firstPresentationChar)) {
                    boolean upperCaseInput = Character.isUpperCase(firstInputChar);
                    boolean upperCasePresentation = Character.isUpperCase(firstPresentationChar);

                    if (Strings.isMixedCase(text)) {
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
        return null;
    }

    @Override
    public Icon getIcon() {
        DBObject object = getObject();
        return object == null ? objectRef.getObjectType().getIcon() : object.getIcon();
    }
}
