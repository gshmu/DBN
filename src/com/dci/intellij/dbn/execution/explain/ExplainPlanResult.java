package com.dci.intellij.dbn.execution.explain;

import javax.swing.Icon;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.action.DBNDataKeys;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.execution.ExecutionResult;
import com.dci.intellij.dbn.execution.common.result.ui.ExecutionResultForm;
import com.dci.intellij.dbn.execution.explain.ui.ExplainPlanResultForm;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class ExplainPlanResult implements ExecutionResult {
    private String planId;
    private Date timestamp;
    private ExplainPlanEntry root;
    private ConnectionHandlerRef connectionHandlerRef;
    private String resultName;
    private String errorMessage;
    private VirtualFile virtualFile;
    private ExplainPlanResultForm resultForm;

    public ExplainPlanResult(ExecutablePsiElement executablePsiElement, ResultSet resultSet) throws SQLException {
        this(executablePsiElement, (String) null);
        // entries must be sorted by PARENT_ID NULLS FIRST, ID
        Map<Integer, ExplainPlanEntry> entries = new HashMap<Integer, ExplainPlanEntry>();
        ConnectionHandler connectionHandler = getConnectionHandler();

        while (resultSet.next()) {
            ExplainPlanEntry entry = new ExplainPlanEntry(connectionHandler, resultSet);
            Integer id = entry.getId();
            Integer parentId = entry.getParentId();
            entries.put(id, entry);
            if (parentId == null) {
                root = entry;
            } else {
                ExplainPlanEntry parentEntry = entries.get(parentId);
                parentEntry.addChild(entry);
            }
        }
    }

    public ExplainPlanResult(ExecutablePsiElement executablePsiElement, String errorMessage) {
        DBLanguagePsiFile file = executablePsiElement.getFile();
        ConnectionHandler connectionHandler = file.getConnectionHandler();
        connectionHandlerRef = connectionHandler.getRef();
        virtualFile = file.getVirtualFile();
        this.resultName = executablePsiElement.createSubjectList();
        this.errorMessage = errorMessage;
    }

    public VirtualFile getVirtualFile() {
        return virtualFile;
    }

    public ExplainPlanEntry getRoot() {
        return root;
    }

    @Nullable
    @Override
    public ConnectionHandler getConnectionHandler() {
        return connectionHandlerRef.get();
    }

    @Override
    public Project getProject() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        return connectionHandler == null ? null : connectionHandler.getProject();
    }

    @Override
    public ExecutionResultForm getResultPanel() {
        if (resultForm == null) {
            resultForm = new ExplainPlanResultForm(this);
        }
        return resultForm;
    }

    @Override
    public String getResultName() {
        return resultName;
    }

    @Override
    public Icon getResultIcon() {
        return null;
    }

    public boolean isError() {
        return errorMessage != null;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override public void setExecutionDuration(int executionDuration) {}
    @Override public int getExecutionDuration() { return 0; }

    /********************************************************
     *                    Data Provider                     *
     ********************************************************/
    public DataProvider dataProvider = new DataProvider() {
        @Override
        public Object getData(@NonNls String dataId) {
            if (DBNDataKeys.EXPLAIN_PLAN_RESULT.is(dataId)) {
                return ExplainPlanResult.this;
            }
            if (PlatformDataKeys.PROJECT.is(dataId)) {
                return getProject();
            }
            return null;
        }
    };

    public DataProvider getDataProvider() {
        return dataProvider;
    }

    /********************************************************
     *                    Disposable                   *
     ********************************************************/
    private boolean disposed;

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public void dispose() {
        disposed = true;
        DisposerUtil.dispose(resultForm);
        DisposerUtil.dispose(root);
    }

}
