package com.dci.intellij.dbn.execution.explain.result;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.action.DataKeys;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.SafeDisposer;
import com.dci.intellij.dbn.common.util.Commons;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ResultSetUtil;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.execution.ExecutionResultBase;
import com.dci.intellij.dbn.execution.explain.result.ui.ExplainPlanResultForm;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExplainPlanResult extends ExecutionResultBase<ExplainPlanResultForm> {
    private String planId;
    private Date timestamp;
    private @Getter ExplainPlanEntry root;
    private final ConnectionHandlerRef connectionHandler;
    private final @Getter VirtualFile virtualFile;
    private final @Getter SchemaId currentSchema;
    private final @Getter String errorMessage;
    private final String statementText;
    private final String resultName;

    public ExplainPlanResult(ExecutablePsiElement executablePsiElement, ResultSet resultSet) throws SQLException {
        this(executablePsiElement, (String) null);
        // entries must be sorted by PARENT_ID NULLS FIRST, ID
        Map<Integer, ExplainPlanEntry> entries = new HashMap<>();
        ConnectionHandler connectionHandler = getConnectionHandler();
        List<String> explainColumnNames = ResultSetUtil.getColumnNames(resultSet);

        while (resultSet.next()) {
            ExplainPlanEntry entry = new ExplainPlanEntry(connectionHandler, resultSet, explainColumnNames);
            Integer id = entry.getId();
            Integer parentId = entry.getParentId();
            entries.put(id, entry);
            if (parentId == null) {
                root = entry;
            } else {
                ExplainPlanEntry parentEntry = entries.get(parentId);
                parentEntry.addChild(entry);
                entry.setParent(parentEntry);
            }
        }
    }

    public ExplainPlanResult(ExecutablePsiElement executablePsiElement, String errorMessage) {
        DBLanguagePsiFile psiFile = executablePsiElement.getFile();
        ConnectionHandler connectionHandler = Failsafe.nn(psiFile.getConnectionHandler());
        this.connectionHandler = connectionHandler.getRef();
        this.currentSchema = psiFile.getSchemaId();
        this.virtualFile = psiFile.getVirtualFile();
        this.resultName = Commons.nvl(executablePsiElement.createSubjectList(), "Explain Plan");
        this.errorMessage = errorMessage;
        this.statementText = executablePsiElement.getText();
    }

    @Override
    public ConnectionId getConnectionId() {
        return connectionHandler.getConnectionId();
    }

    @Override
    @NotNull
    public ConnectionHandler getConnectionHandler() {
        return ConnectionHandlerRef.ensure(connectionHandler);
    }

    @Override
    public PsiFile createPreviewFile() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        SchemaId currentSchema = getCurrentSchema();
        DBLanguageDialect languageDialect = connectionHandler.getLanguageDialect(SQLLanguage.INSTANCE);
        return DBLanguagePsiFile.createFromText(
                getProject(),
                "preview",
                languageDialect,
                statementText,
                connectionHandler,
                currentSchema);
    }

    @NotNull
    @Override
    public Project getProject() {
        return getConnectionHandler().getProject();
    }

    @Nullable
    @Override
    public ExplainPlanResultForm createForm() {
        return new ExplainPlanResultForm(this);
    }

    @Override
    @NotNull
    public String getName() {
        return resultName;
    }

    @Override
    public Icon getIcon() {
        return Icons.EXPLAIN_PLAN_RESULT;
    }

    public boolean isError() {
        return errorMessage != null;
    }

    /********************************************************
     *                    Data Provider                     *
     ********************************************************/
    @Nullable
    @Override
    public Object getData(@NotNull String dataId) {
        if (DataKeys.EXPLAIN_PLAN_RESULT.is(dataId)) {
            return ExplainPlanResult.this;
        }
        return null;
    }

    /********************************************************
     *                    Disposable                   *
     *******************************************************  */
    @Override
    public void disposeInner() {
        SafeDisposer.dispose(root);
        super.disposeInner();
    }
}
