package com.dci.intellij.dbn.debugger.common.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.common.thread.ReadActionRunner;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;

public abstract class DBStatementRunConfig extends DBRunConfig<StatementExecutionInput> {
    private StatementExecutionInput executionInput;

    public DBStatementRunConfig(Project project, DBStatementRunConfigFactory factory, String name, boolean generic) {
        super(project, factory, name, generic);
    }

    @Nullable
    @Override
    public DBLanguagePsiFile getSource() {
        if (executionInput != null) {
            return executionInput.getExecutionProcessor().getPsiFile();
        }
        return null;
    }

    @Override
    public List<DBMethod> getMethods() {
        if (executionInput != null) {
            final ExecutablePsiElement executablePsiElement = executionInput.getExecutionProcessor().getCachedExecutable();
            if (executablePsiElement != null) {
                return new ReadActionRunner<List<DBMethod>>() {
                    @Override
                    protected List<DBMethod> run() {
                        Set<DBObject> objects = executablePsiElement.collectObjectReferences(DBObjectType.METHOD);
                        if (objects != null) {
                            List<DBMethod> methods = new ArrayList<DBMethod>();
                            for (DBObject object : objects) {
                                if (object instanceof DBMethod && !methods.contains(object)) {
                                    DBMethod method = (DBMethod) object;
                                    methods.add(method);
                                }
                            }
                            return methods;
                        }
                        return Collections.emptyList();
                    }
                }.start();
            }

        }
        return Collections.emptyList();
    }

    public StatementExecutionInput getExecutionInput() {
        return executionInput;
    }

    public void setExecutionInput(StatementExecutionInput executionInput) {
        this.executionInput = executionInput;
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        super.writeExternal(element);
        SettingsUtil.setBoolean(element, "compile-dependencies", isCompileDependencies());
    }

    @Override
    public void readExternal(Element element) throws InvalidDataException {
        super.readExternal(element);
        setCompileDependencies(SettingsUtil.getBoolean(element, "compile-dependencies", true));
    }

    @Nullable
    public String suggestedName() {
        if (isGeneric()) {
            String defaultRunnerName = getType().getDefaultRunnerName();
            if (getDebuggerType() == DBDebuggerType.JDWP) {
                defaultRunnerName = defaultRunnerName + " (JDWP)";
            }
            return defaultRunnerName;
        }
        return null;
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {

    }
}
