package com.dci.intellij.dbn.execution.explain.result;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.action.DBNDataKeys;
import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.DataProviderSupplier;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ResultSetUtil;
import com.dci.intellij.dbn.execution.ExecutionResult;
import com.dci.intellij.dbn.execution.explain.result.ui.ExplainPlanResultForm;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExplainPlanResult extends DisposableBase implements ExecutionResult, DataProviderSupplier {
    private String planId;
    private Date timestamp;
    private ExplainPlanEntry root;
    private ConnectionHandlerRef connectionHandlerRef;
    private DBObjectRef<DBSchema> databaseSchemaRef;
    private String statementText;
    private String resultName;
    private String errorMessage;
    private VirtualFile virtualFile;
    private ExplainPlanResultForm resultForm;

    public ExplainPlanResult(ExecutablePsiElement executablePsiElement, ResultSet resultSet) throws SQLException {
        this(executablePsiElement, (String) null);
        // entries must be sorted by PARENT_ID NULLS FIRST, ID
        Map<Integer, ExplainPlanEntry> entries = new HashMap<Integer, ExplainPlanEntry>();
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
        ConnectionHandler connectionHandler = Failsafe.get(psiFile.getConnectionHandler());
        connectionHandlerRef = connectionHandler.getRef();
        databaseSchemaRef = DBObjectRef.from(psiFile.getDatabaseSchema());
        virtualFile = psiFile.getVirtualFile();
        this.resultName = CommonUtil.nvl(executablePsiElement.createSubjectList(), "Explain Plan");
        this.errorMessage = errorMessage;
        this.statementText = executablePsiElement.getText();
    }

    public VirtualFile getVirtualFile() {
        return virtualFile;
    }

    public ExplainPlanEntry getRoot() {
        return root;
    }

    @Override
    public ConnectionId getConnectionId() {
        return connectionHandlerRef.getConnectionId();
    }

    @Override
    @NotNull
    public ConnectionHandler getConnectionHandler() {
        return ConnectionHandlerRef.getnn(connectionHandlerRef);
    }

    public DBSchema getDatabaseSchema() {
        return DBObjectRef.get(databaseSchemaRef);
    }

    @Override
    public PsiFile createPreviewFile() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        DBSchema currentSchema = getDatabaseSchema();
        DBLanguageDialect languageDialect = connectionHandler.getLanguageDialect(SQLLanguage.INSTANCE);
        return DBLanguagePsiFile.createFromText(getProject(), "preview", languageDialect, statementText, connectionHandler, currentSchema);
    }

    @NotNull
    @Override
    public Project getProject() {
        return getConnectionHandler().getProject();
    }

    @Override
    public ExplainPlanResultForm getForm(boolean create) {
        if (resultForm == null && create) {
            resultForm = new ExplainPlanResultForm(this);
        }
        return resultForm;
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

    public String getErrorMessage() {
        return errorMessage;
    }

    /********************************************************
     *                    Data Provider                     *
     ********************************************************/
    public DataProvider dataProvider = new DataProvider() {
        @Override
        public Object getData(@NonNls String dataId) {
            if (DBNDataKeys.EXPLAIN_PLAN_RESULT.is(dataId)) {
                return ExplainPlanResult.this;
            }
            return null;
        }
    };

    @Override
    @Nullable
    public DataProvider getDataProvider() {
        return dataProvider;
    }

    /********************************************************
     *                    Disposable                   *
     ********************************************************/
    @Override
    public void dispose() {
        if (!isDisposed()) {
            super.dispose();
            resultForm = null;
            DisposerUtil.dispose(root);
        }
    }
}
