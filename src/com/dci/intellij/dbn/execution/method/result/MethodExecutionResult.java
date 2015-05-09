package com.dci.intellij.dbn.execution.method.result;

import javax.swing.Icon;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.action.DBNDataKeys;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetDataModel;
import com.dci.intellij.dbn.execution.ExecutionResult;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.method.ArgumentValue;
import com.dci.intellij.dbn.execution.method.ArgumentValueHolder;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.result.ui.MethodExecutionResultForm;
import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBTypeAttribute;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.PsiFile;

public class MethodExecutionResult implements ExecutionResult, Disposable {
    private MethodExecutionInput executionInput;
    private MethodExecutionResultForm resultPanel;
    private List<ArgumentValue> argumentValues = new ArrayList<ArgumentValue>();
    private Map<DBObjectRef<DBArgument>, ResultSetDataModel> cursorModels;
    private boolean debug;
    private String logOutput;
    private int executionDuration;

    public MethodExecutionResult(MethodExecutionInput executionInput, MethodExecutionResultForm resultPanel, boolean debug) {
        this.executionInput = executionInput;
        executionInput.setExecutionResult(this);
        this.debug = debug;
        this.resultPanel = resultPanel;
    }

    public int getExecutionDuration() {
        return executionDuration;
    }

    public void calculateExecDuration() {
        this.executionDuration = (int) (System.currentTimeMillis() - executionInput.getExecutionTimestamp());
    }

    public void addArgumentValue(DBArgument argument, Object value) throws SQLException {
        ArgumentValueHolder<Object> valueStore = ArgumentValue.createBasicValueHolder(value);
        ArgumentValue argumentValue = new ArgumentValue(argument, valueStore);
        argumentValues.add(argumentValue);
        if (value instanceof ResultSet) {
            ResultSet resultSet = (ResultSet) value;
            if (cursorModels == null) {
                cursorModels = new HashMap<DBObjectRef<DBArgument>, ResultSetDataModel>();
            }

            ExecutionEngineSettings settings = ExecutionEngineSettings.getInstance(argument.getProject());
            int maxRecords = settings.getStatementExecutionSettings().getResultSetFetchBlockSize();
            ResultSetDataModel dataModel = new ResultSetDataModel(resultSet, getConnectionHandler(), maxRecords);
            cursorModels.put(DBObjectRef.from(argument), dataModel);

            Disposer.register(this, dataModel);
        }
    }

    public void addArgumentValue(DBArgument argument, DBTypeAttribute attribute, Object value) {
        ArgumentValueHolder<Object> valueStore = ArgumentValue.createBasicValueHolder(value);
        ArgumentValue argumentValue = new ArgumentValue(argument, attribute, valueStore);
        argumentValues.add(argumentValue);
    }


    public List<ArgumentValue> getArgumentValues() {
        return argumentValues;
    }

    public ArgumentValue getArgumentValue(DBObjectRef<DBArgument> argumentRef) {
        for (ArgumentValue argumentValue : argumentValues) {
            if (argumentValue.getArgumentRef().equals(argumentRef)) {
                return argumentValue;
            }
        }
        return null;
    }

    @Nullable
    public MethodExecutionResultForm getForm(boolean create) {
        if (resultPanel == null && create) {
            resultPanel = new MethodExecutionResultForm(getProject(), this);
        }
        return resultPanel == null || resultPanel.isDisposed() ? null : resultPanel;
    }

    @NotNull
    public String getName() {
        return getMethod().getName();
    }

    public Icon getIcon() {
        return getMethod().getOriginalIcon();
    }

    @NotNull
    public MethodExecutionInput getExecutionInput() {
        return FailsafeUtil.get(executionInput);
    }

    @NotNull
    public DBMethod getMethod() {
        return FailsafeUtil.get(getExecutionInput().getMethod());
    }


    @NotNull
    public Project getProject() {
        return getMethod().getProject();
    }

    @Override
    public String getConnectionId() {
        return executionInput.getConnectionId();
    }

    @NotNull
    public ConnectionHandler getConnectionHandler() {
        return getMethod().getConnectionHandler();
    }

    @Override
    public PsiFile createPreviewFile() {
        return null;
    }

    public boolean hasCursorResults() {
        for (ArgumentValue argumentValue: argumentValues) {
            if (argumentValue.isCursor()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSimpleResults() {
        for (ArgumentValue argumentValue: argumentValues) {
            if (!argumentValue.isCursor()) {
                return true;
            }
        }
        return false;
    }


    public void setResultPanel(MethodExecutionResultForm resultPanel) {
        this.resultPanel = resultPanel;
    }

    public boolean isDebug() {
        return debug;
    }

    public ResultSetDataModel getTableModel(DBArgument argument) {
        return cursorModels.get(argument.getRef());
    }

    public String getLogOutput() {
        return logOutput;
    }

    public void setLogOutput(String logOutput) {
        this.logOutput = logOutput;
    }

    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    private boolean disposed;

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    public void dispose() {
        disposed = true;

        resultPanel = null;
        executionInput = null;
        CollectionUtil.clearMap(cursorModels);
        argumentValues.clear();
    }

    /********************************************************
     *                    Data Provider                     *
     ********************************************************/
    public DataProvider dataProvider = new DataProvider() {
        @Override
        public Object getData(@NonNls String dataId) {
            if (DBNDataKeys.METHOD_EXECUTION_RESULT.is(dataId)) {
                return MethodExecutionResult.this;
            }
            return null;
        }
    };

    @Nullable
    public DataProvider getDataProvider() {
        return dataProvider;
    }
}
