package com.dci.intellij.dbn.language.common.psi;

import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.execution.statement.StatementExecutionManager;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionBasicProcessor;
import com.dci.intellij.dbn.language.common.element.NamedElementType;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import gnu.trove.THashSet;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.util.Set;

public class ExecutablePsiElement extends NamedPsiElement{
    private StatementExecutionBasicProcessor executionProcessor;

    public ExecutablePsiElement(ASTNode astNode, NamedElementType elementType) {
        super(astNode, elementType);
    }

    public synchronized StatementExecutionBasicProcessor getExecutionProcessor() {
        if (executionProcessor == null || !executionProcessor.matches(this, false)) {
            StatementExecutionManager  statementExecutionManager = StatementExecutionManager.getInstance(getProject());
            executionProcessor = statementExecutionManager.locateExecutionProcessor(this);
            if (executionProcessor == null) {
                executionProcessor = statementExecutionManager.createExecutionProcessor(this);
            }
        }
        return executionProcessor;
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

    public String createResultName() {
        Set<BasePsiElement> subjects = new THashSet<BasePsiElement>();
        collectSubjectPsiElements(subjects);
        return subjects.size() > 0 ? NamingUtil.createNamesList(subjects, 3) : null;
    }

    public NamedElementType getElementType() {
        return (NamedElementType) super.getElementType();
    }

    public boolean isQuery() {
        return getElementType().is(ElementTypeAttribute.QUERY);
    }

    public boolean isTransactional() {
        return getElementType().is(ElementTypeAttribute.TRANSACTIONAL);
    }

    public boolean isTransactionControl() {
        return getElementType().is(ElementTypeAttribute.TRANSACTION_CONTROL);
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
        String resultName = createResultName();
        if (resultName != null) {
            return getElementType().getDescription() + " (" + resultName + ")";
        } else {
            return getElementType().getDescription();
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
