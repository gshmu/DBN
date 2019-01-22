package com.dci.intellij.dbn.language.common.psi;

import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseOption;
import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseSettings;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.language.common.WeakRef;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.NamedElementType;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ExecutablePsiElement extends NamedPsiElement{
    private WeakRef<StatementExecutionProcessor> executionProcessor;

    public String prepareStatementText(){
        PsiElement lastChild = getLastChild();
        while (lastChild != null && !(lastChild instanceof BasePsiElement)) {
            lastChild = lastChild.getPrevSibling();
        }
        BasePsiElement basePsiElement = (BasePsiElement) lastChild;
        String text = getText();
        if (basePsiElement != null && basePsiElement.getElementType() instanceof NamedElementType) {
            NamedElementType namedElementType = (NamedElementType) basePsiElement.getElementType();
            if (namedElementType.truncateOnExecution()) {
                return text.substring(0, text.length() - basePsiElement.getTextLength());
            }
        }
        return text;
    }
    public ExecutablePsiElement(ASTNode astNode, NamedElementType elementType) {
        super(astNode, elementType);
    }

    public NamedElementType getElementType() {
        return (NamedElementType) super.getElementType();
    }

    public boolean isQuery() {
        return getSpecificElementType().is(ElementTypeAttribute.QUERY);
    }

    public boolean isTransactional() {
        return is(ElementTypeAttribute.TRANSACTIONAL) || getSpecificElementType().is(ElementTypeAttribute.TRANSACTIONAL);
    }

    public boolean isPotentiallyTransactional() {
        return is(ElementTypeAttribute.POTENTIALLY_TRANSACTIONAL) || getSpecificElementType().is(ElementTypeAttribute.POTENTIALLY_TRANSACTIONAL);
    }

    public boolean isTransactionControl() {
        return getSpecificElementType().is(ElementTypeAttribute.TRANSACTION_CONTROL);
    }

    public boolean isNestedExecutable() {
        PsiElement parent = getParent();
        while (parent != null && !(parent instanceof RootPsiElement)) {
            if (parent instanceof ExecutablePsiElement && parent.getTextOffset() != getTextOffset()) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    public boolean hasErrors() {
        return false;
    }

    public StatementExecutionProcessor getExecutionProcessor() {
        return executionProcessor == null ? null : executionProcessor.get();
    }

    public void setExecutionProcessor(StatementExecutionProcessor executionProcessor) {
        this.executionProcessor = WeakRef.from(executionProcessor);
    }

    public Object clone() {
        return super.clone();
    }



    /*********************************************************
     *                    ItemPresentation                   *
     *********************************************************/
    public String getPresentableText() {
        ElementType elementType = getSpecificElementType();
        String subject = null;
        String action = "";
        String subjectType = "";
        if (is(ElementTypeAttribute.DATA_DEFINITION)) {
            IdentifierPsiElement subjectPsiElement = (IdentifierPsiElement) findFirstPsiElement(ElementTypeAttribute.SUBJECT);
            if (subjectPsiElement != null) {
                subject = subjectPsiElement.getUnquotedText().toString();
            }
            BasePsiElement actionPsiElement = findFirstPsiElement(ElementTypeAttribute.ACTION);
            if (actionPsiElement != null) {
                action = actionPsiElement.getText() + " ";
                if (subjectPsiElement != null) {
                    BasePsiElement compilableBlockPsiElement = findFirstPsiElement(ElementTypeAttribute.COMPILABLE_BLOCK);
                    if (compilableBlockPsiElement != null) {
                        DBObjectType objectType = subjectPsiElement.getObjectType();
                        subjectType = objectType.getName().toUpperCase() + " ";
                        if (compilableBlockPsiElement.is(ElementTypeAttribute.OBJECT_DECLARATION)) {
                            subjectType += "BODY ";
                        }
                    }
                }
            }
        } else {
            subject = createSubjectList();
        }
        if (subject != null && isValid()) {
            CodeStyleCaseSettings caseSettings = getLanguage().getCodeStyleSettings(getProject()).getCaseSettings();
            CodeStyleCaseOption keywordCaseOption = caseSettings.getKeywordCaseOption();
            CodeStyleCaseOption objectCaseOption = caseSettings.getObjectCaseOption();
            action = keywordCaseOption.format(action);
            subjectType = keywordCaseOption.format(subjectType);
            subject = objectCaseOption.format(subject);
            return elementType.getDescription() + " (" + action + subjectType + subject + ")";
        } else {
            return elementType.getDescription();
        }
    }

    @Nullable
    public String getLocationString() {
        return null;
    }

    @Nullable
    public Icon getIcon(boolean open) {
        return super.getIcon(open);
    }

    @Nullable
    public TextAttributesKey getTextAttributesKey() {
        return null;
    }
}
