package com.dci.intellij.dbn.language.common.psi;

import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseOption;
import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseSettings;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.NamedElementType;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public class ExecutablePsiElement extends NamedPsiElement{
    public ExecutablePsiElement(ASTNode astNode, NamedElementType elementType) {
        super(astNode, elementType);
    }

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

    public NamedElementType getElementType() {
        return (NamedElementType) super.getElementType();
    }

    public boolean isQuery() {
        return getSpecificElementType().is(ElementTypeAttribute.QUERY);
    }

    public boolean isTransactional() {
        return getSpecificElementType().is(ElementTypeAttribute.TRANSACTIONAL);
    }

    public boolean isTransactionControl() {
        return getSpecificElementType().is(ElementTypeAttribute.TRANSACTION_CONTROL);
    }


    public boolean isNestedExecutable() {
        PsiElement parent = getParent();
        while (parent != null && !(parent instanceof RootPsiElement)) {
            if (parent instanceof ExecutablePsiElement) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    public boolean hasErrors() {
        return false;
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
            IdentifierPsiElement subjectPsiElement = (IdentifierPsiElement) lookupFirstPsiElement(ElementTypeAttribute.SUBJECT);
            if (subjectPsiElement != null) {
                subject = subjectPsiElement.getUnquotedText().toString();
            }
            BasePsiElement actionPsiElement = lookupFirstPsiElement(ElementTypeAttribute.ACTION);
            if (actionPsiElement != null) {
                action = actionPsiElement.getText() + " ";
                if (subjectPsiElement != null) {
                    BasePsiElement compilableBlockPsiElement = lookupFirstPsiElement(ElementTypeAttribute.COMPILABLE_BLOCK);
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
        if (subject != null) {
            CodeStyleCaseSettings caseSettings = getLanguage().getCodeStyleSettings(getProject()).getCaseSettings();
            CodeStyleCaseOption keywordCaseOption = caseSettings.getKeywordCaseOption();
            CodeStyleCaseOption objectCaseOption = caseSettings.getObjectCaseOption();
            action = keywordCaseOption.changeCase(action);
            subjectType = keywordCaseOption.changeCase(subjectType);
            subject = objectCaseOption.changeCase(subject);
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
