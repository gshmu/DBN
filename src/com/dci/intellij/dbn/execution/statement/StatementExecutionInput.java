package com.dci.intellij.dbn.execution.statement;

import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.thread.ReadActionRunner;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.execution.statement.variables.StatementExecutionVariablesBundle;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.ExecutableBundlePsiElement;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.language.common.psi.QualifiedIdentifierPsiElement;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;

public class StatementExecutionInput implements Disposable {
    private StatementExecutionProcessor executionProcessor;
    private StatementExecutionVariablesBundle executionVariables;
    private ConnectionHandlerRef connectionHandlerRef;
    private DBObjectRef<DBSchema> currentSchemaRef;

    private String originalStatementText;
    private String executableStatementText;
    private ExecutablePsiElement executablePsiElement;
    private boolean isDisposed;

    public StatementExecutionInput(String originalStatementText, String executableStatementText, StatementExecutionProcessor executionProcessor) {
        this.executionProcessor = executionProcessor;
        this.connectionHandlerRef = executionProcessor.getConnectionHandler().getRef();
        this.currentSchemaRef = DBObjectRef.from(executionProcessor.getCurrentSchema());
        this.originalStatementText = originalStatementText;
        this.executableStatementText = executableStatementText;
    }

    public String getOriginalStatementText() {
        return originalStatementText;
    }

    public void setOriginalStatementText(String originalStatementText) {
        this.originalStatementText = originalStatementText;
        executablePsiElement = null;
    }

    public void setExecutableStatementText(String executableStatementText) {
        this.executableStatementText = executableStatementText;
    }

    public String getExecutableStatementText() {
        return executableStatementText;
    }

    @Nullable
    public ExecutablePsiElement getExecutablePsiElement() {
        if (executablePsiElement == null) {
            executablePsiElement = new ReadActionRunner<ExecutablePsiElement>() {

                @Override
                protected ExecutablePsiElement run() {
                    PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(getProject());
                    PsiFile previewFile = psiFileFactory.createFileFromText("preview", getConnectionHandler().getLanguageDialect(SQLLanguage.INSTANCE), originalStatementText);

                    PsiElement firstChild = previewFile.getFirstChild();
                    if (firstChild instanceof ExecutableBundlePsiElement) {
                        ExecutableBundlePsiElement rootPsiElement = (ExecutableBundlePsiElement) firstChild;
                        return rootPsiElement.getExecutablePsiElements().get(0);
                    }
                    return null;
                }
            }.start();
        }
        return executablePsiElement;
    }

    public StatementExecutionVariablesBundle getExecutionVariables() {
        return executionVariables;
    }

    public void setExecutionVariables(StatementExecutionVariablesBundle executionVariables) {
        if (this.executionVariables != null) {
            DisposerUtil.dispose(this.executionVariables);
        }
        this.executionVariables = executionVariables;
    }

    public PsiFile getPreviewFile() {
        PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(getProject());
        return psiFileFactory.createFileFromText("preview", getConnectionHandler().getLanguageDialect(SQLLanguage.INSTANCE), executableStatementText);
    }

    public StatementExecutionProcessor getExecutionProcessor() {
        return executionProcessor;
    }

    public Project getProject() {
        return getConnectionHandler().getProject();
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandlerRef.get();
    }

    public DBSchema getCurrentSchema() {
        return DBObjectRef.get(currentSchemaRef);
    }

    public boolean isDataDefinitionStatement() {
        ExecutablePsiElement executablePsiElement = getExecutablePsiElement();
        return executablePsiElement != null && executablePsiElement.is(ElementTypeAttribute.DATA_DEFINITION);
    }

    @Nullable
    public DBSchemaObject getAffectedObject() {
        if (isDataDefinitionStatement()) {
            IdentifierPsiElement subjectPsiElement = getSubjectPsiElement();
            if (subjectPsiElement != null) {
                DBObject object = subjectPsiElement.resolveUnderlyingObject();
                if (object != null && object instanceof DBSchemaObject) {
                    return (DBSchemaObject) object;
                }
            }
        }
        return null;
    }

    @Nullable
    public DBSchema getAffectedSchema() {
        if (isDataDefinitionStatement()) {
            IdentifierPsiElement subjectPsiElement = getSubjectPsiElement();
            if (subjectPsiElement != null) {
                PsiElement parent = subjectPsiElement.getParent();
                if (parent instanceof QualifiedIdentifierPsiElement) {
                    QualifiedIdentifierPsiElement qualifiedIdentifierPsiElement = (QualifiedIdentifierPsiElement) parent;
                    DBObject parentObject = qualifiedIdentifierPsiElement.lookupParentObjectFor(subjectPsiElement.getElementType());
                    if (parentObject instanceof DBSchema) {
                        return (DBSchema) parentObject;
                    }
                }
            }
        }
        return getCurrentSchema();
    }

    @Nullable
    public IdentifierPsiElement getSubjectPsiElement() {
        if (executionProcessor != null && !executionProcessor.isDisposed()) {
            ExecutablePsiElement executablePsiElement = getExecutablePsiElement();
            if (executablePsiElement != null) {
                return (IdentifierPsiElement) executablePsiElement.lookupFirstPsiElement(ElementTypeAttribute.SUBJECT);
            }
        }
        return null;
    }

    public BasePsiElement getCompilableBlockPsiElement() {
        if (executionProcessor != null && !executionProcessor.isDisposed()) {
            ExecutablePsiElement executablePsiElement = getExecutablePsiElement();
            if (executablePsiElement != null) {
                return executablePsiElement.lookupFirstPsiElement(ElementTypeAttribute.COMPILABLE_BLOCK);
            }
        }
        return null;
    }

    public DBContentType getCompilableContentType() {
        BasePsiElement compilableBlockPsiElement = getCompilableBlockPsiElement();
        if (compilableBlockPsiElement != null) {
            //if (compilableBlockPsiElement.is(ElementTypeAttribute.OBJECT_DEFINITION)) return DBContentType.CODE;
            if (compilableBlockPsiElement.is(ElementTypeAttribute.OBJECT_SPECIFICATION)) return DBContentType.CODE_SPEC;
            if (compilableBlockPsiElement.is(ElementTypeAttribute.OBJECT_DECLARATION)) return DBContentType.CODE_BODY;
        }
        return DBContentType.CODE;
    }


    public void dispose() {
        if (!isDisposed) {
            isDisposed = true;
            executionProcessor = null;
            executablePsiElement = null;
        }
    }

    public boolean isDisposed() {
        return isDisposed;
    }

    public String getStatementDescription() {
        ExecutablePsiElement executablePsiElement = getExecutablePsiElement();
        return executablePsiElement == null ? "SQL Statement" : executablePsiElement.getPresentableText();
    }
}
