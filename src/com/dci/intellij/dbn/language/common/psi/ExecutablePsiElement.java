package com.dci.intellij.dbn.language.common.psi;

import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseOption;
import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseSettings;
import com.dci.intellij.dbn.common.util.Cloneable;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.language.common.WeakRef;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.impl.NamedElementType;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ExecutablePsiElement extends NamedPsiElement implements Cloneable<ExecutablePsiElement> {
    private WeakRef<StatementExecutionProcessor> executionProcessor;

    public String prepareStatementText(){
        PsiElement lastChild = getLastChild();
        while (lastChild != null && !(lastChild instanceof BasePsiElement)) {
            lastChild = lastChild.getPrevSibling();
        }
        BasePsiElement basePsiElement = (BasePsiElement) lastChild;
        String text = getText();
        if (basePsiElement != null && basePsiElement.elementType instanceof NamedElementType) {
            NamedElementType namedElementType = (NamedElementType) basePsiElement.elementType;
            if (namedElementType.truncateOnExecution()) {
                return text.substring(0, text.length() - basePsiElement.getTextLength());
            }
        }
        return text;
    }
    public ExecutablePsiElement(ASTNode astNode, NamedElementType elementType) {
        super(astNode, elementType);
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

    @Override
    public boolean hasErrors() {
        return false;
    }

    public StatementExecutionProcessor getExecutionProcessor() {
        return executionProcessor == null ? null : executionProcessor.get();
    }

    public void setExecutionProcessor(StatementExecutionProcessor executionProcessor) {
        this.executionProcessor = WeakRef.from(executionProcessor);
    }

    @Override
    public ExecutablePsiElement clone() {
        return (ExecutablePsiElement) super.clone();
    }



    /*********************************************************
     *                    ItemPresentation                   *
     *********************************************************/
    @Override
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

    @Override
    @Nullable
    public String getLocationString() {
        return null;
    }

    @Override
    @Nullable
    public Icon getIcon(boolean open) {
        return super.getIcon(open);
    }

    @Override
    @Nullable
    public TextAttributesKey getTextAttributesKey() {
        return null;
    }
}
