package com.dci.intellij.dbn.code.common.lookup;

import com.dci.intellij.dbn.code.common.completion.BasicInsertHandler;
import com.dci.intellij.dbn.code.common.completion.BracketsInsertHandler;
import com.dci.intellij.dbn.code.common.completion.CodeCompletionContext;
import com.dci.intellij.dbn.code.common.completion.options.CodeCompletionSettings;
import com.dci.intellij.dbn.code.common.completion.options.general.CodeCompletionFormatSettings;
import com.dci.intellij.dbn.code.common.style.DBLCodeStyleManager;
import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseOption;
import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseSettings;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.TokenTypeCategory;
import com.dci.intellij.dbn.language.common.element.impl.TokenElementType;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupItem;
import com.intellij.openapi.project.Project;

import javax.swing.Icon;

public class TokenLookupItemBuilder extends LookupItemBuilder {

    private TokenElementType tokenElementType;

    public TokenLookupItemBuilder(TokenElementType tokenElementType) {
        this.tokenElementType = tokenElementType;
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public boolean isBold() {
        return tokenElementType.getTokenType().isKeyword();
    }

    @Override
    public CharSequence getText(CodeCompletionContext completionContext) {
        String text = tokenElementType.getText();
        TokenType tokenType = tokenElementType.getTokenType();
        if (StringUtil.isEmpty(text)) {
            text = tokenType.getValue();
        }
        Project project = completionContext.getParameters().getOriginalFile().getProject();

        DBLanguage language = tokenElementType.getLanguage();
        CodeStyleCaseSettings styleCaseSettings = DBLCodeStyleManager.getInstance(project).getCodeStyleCaseSettings(language);
        CodeStyleCaseOption caseOption =
                tokenType.isFunction() ? styleCaseSettings.getFunctionCaseOption() :
                tokenType.isKeyword() ? styleCaseSettings.getKeywordCaseOption() :
                tokenType.isParameter() ? styleCaseSettings.getParameterCaseOption() :
                tokenType.isDataType() ? styleCaseSettings.getDatatypeCaseOption() : null;

        if (caseOption != null) {
            text = caseOption.format(text);
        }

        String userInput = completionContext.getUserInput();
        CodeCompletionFormatSettings codeCompletionFormatSettings = CodeCompletionSettings.getInstance(project).getFormatSettings();
        if (StringUtil.isNotEmpty(userInput) && !text.startsWith(userInput) && !codeCompletionFormatSettings.isEnforceCodeStyleCase()) {
            char firstInputChar = userInput.charAt(0);
            char firstPresentationChar = text.charAt(0);

            if (Character.toUpperCase(firstInputChar) == Character.toUpperCase(firstPresentationChar)) {
                text = Character.isUpperCase(firstInputChar) ?
                        text.toUpperCase() :
                        text.toLowerCase();
            } else {
                return null;
            }
        }

        return text;
    }

    @Override
    public String getTextHint() {
        return getTokenTypeCategory().getName();
    }

    private void createLookupItem(CompletionResultSet resultSet, String presentation, CodeCompletionContext completionContext, boolean insertParenthesis) {
        LookupItem lookupItem = new CodeCompletionLookupItem(this, presentation, completionContext);
        lookupItem.setInsertHandler(
                insertParenthesis ?
                        BracketsInsertHandler.INSTANCE :
                        BasicInsertHandler.INSTANCE);
        resultSet.addElement(lookupItem);
    }

    public TokenType getTokenType() {
        return tokenElementType.getTokenType();
    }
    public TokenTypeCategory getTokenTypeCategory() {
        return tokenElementType.getTokenTypeCategory();
    }
}
