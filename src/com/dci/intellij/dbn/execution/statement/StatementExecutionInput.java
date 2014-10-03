package com.dci.intellij.dbn.execution.statement;

import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
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
    private ConnectionHandler connectionHandler;
    private DBObjectRef<DBSchema> currentSchemaRef;

    private ExecutablePsiElement originalPsiElement;
    private String originalStatement;
    private String executeStatement;
    private boolean isDisposed;

    public StatementExecutionInput(String originalStatement, String executeStatement, StatementExecutionProcessor executionProcessor) {
        this.executionProcessor = executionProcessor;
        this.connectionHandler = executionProcessor.getConnectionHandler();
        this.currentSchemaRef = DBObjectRef.from(executionProcessor.getCurrentSchema());
        this.originalStatement = originalStatement;
        this.executeStatement = executeStatement;
    }


    public void setExecuteStatement(String executeStatement) {
        this.executeStatement = executeStatement;
    }

    public String getExecuteStatement() {
        return executeStatement;
    }

    public ExecutablePsiElement getExecutablePsiElement() {
        if (originalPsiElement == null) {
            PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(getProject());
            PsiFile previewFile = psiFileFactory.createFileFromText("preview", connectionHandler.getLanguageDialect(SQLLanguage.INSTANCE), originalStatement);

            PsiElement firstChild = previewFile.getFirstChild();
            if (firstChild instanceof ExecutableBundlePsiElement) {
                ExecutableBundlePsiElement rootPsiElement = (ExecutableBundlePsiElement) firstChild;
                originalPsiElement = rootPsiElement.getExecutablePsiElements().get(0);
            }
        }
        return originalPsiElement;
    }

    public PsiFile getPreviewFile() {
        PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(getProject());
        return psiFileFactory.createFileFromText("preview", connectionHandler.getLanguageDialect(SQLLanguage.INSTANCE), executeStatement);
    }

    public boolean isObsolete() {
        return  executionProcessor == null || executionProcessor.isOrphan() ||
                executionProcessor.getConnectionHandler() != connectionHandler || // connection changed since execution
                executionProcessor.getCurrentSchema() != getCurrentSchema() || // current schema changed since execution
                (executionProcessor.getExecutablePsiElement() != null &&
                        executionProcessor.getExecutablePsiElement().matches(getExecutablePsiElement()) &&
                        !executionProcessor.getExecutablePsiElement().equals(getExecutablePsiElement()));
    }

    public StatementExecutionProcessor getExecutionProcessor() {
        return executionProcessor;
    }

    public Project getProject() {
        return connectionHandler.getProject();
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandler;
    }

    public DBSchema getCurrentSchema() {
        return DBObjectRef.get(currentSchemaRef);
    }

    public boolean isDataDefinitionStatement() {
        if (executionProcessor != null) {
            ExecutablePsiElement executablePsiElement = executionProcessor.getExecutablePsiElement();
            return executablePsiElement != null && executablePsiElement.is(ElementTypeAttribute.DATA_DEFINITION);
        }
        return false;
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
            ExecutablePsiElement executablePsiElement = executionProcessor.getExecutablePsiElement();
            if (executablePsiElement != null) {
                return (IdentifierPsiElement) executablePsiElement.lookupFirstPsiElement(ElementTypeAttribute.SUBJECT);
            }
        }
        return null;
    }

    public BasePsiElement getCompilableBlockPsiElement() {
        if (executionProcessor != null && !executionProcessor.isDisposed()) {
            ExecutablePsiElement executablePsiElement = executionProcessor.getExecutablePsiElement();
            if (executablePsiElement != null) {
                return executablePsiElement.lookupFirstPsiElement(ElementTypeAttribute.COMPILABLE_BLOCK);
            }
        }
        return null;
    }

    public void dispose() {
        if (!isDisposed) {
            isDisposed = true;
            executionProcessor = null;
            connectionHandler = null;
            originalPsiElement = null;
        }
    }

    public boolean isDisposed() {
        return isDisposed;
    }
}
