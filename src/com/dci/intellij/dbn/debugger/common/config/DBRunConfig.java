package com.dci.intellij.dbn.debugger.common.config;

import com.dci.intellij.dbn.common.options.setting.SettingsSupport;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.PresentableConnectionProvider;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.execution.ExecutionInput;
import com.dci.intellij.dbn.object.DBMethod;
import com.intellij.execution.ExecutionTarget;
import com.intellij.execution.configurations.LocatableConfiguration;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.runners.RunConfigurationWithSuppressedDefaultRunAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class DBRunConfig<I extends ExecutionInput> extends RunConfigurationBase implements RunConfigurationWithSuppressedDefaultRunAction, LocatableConfiguration {
    private boolean isGeneratedName = true;
    private boolean compileDependencies = true;
    private boolean canRun = false;
    private DBRunConfigCategory category;
    private I executionInput;

    protected DBRunConfig(Project project, DBRunConfigFactory factory, String name, DBRunConfigCategory category) {
        super(project, factory, name);
        this.category = category;
    }

    public boolean canRun() {
        return category == DBRunConfigCategory.CUSTOM || canRun;
    }

    public void setCanRun(boolean canRun) {
        this.canRun = canRun;
    }

    @NotNull
    @Override
    public DBRunConfigType getType() {
        return (DBRunConfigType) super.getType();
    }

    @Override
    public boolean canRunOn(@NotNull ExecutionTarget target) {
        return super.canRunOn(target);
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        super.writeExternal(element);
        SettingsSupport.setEnum(element, "category", category);
        SettingsSupport.setBoolean(element, "compile-dependencies", compileDependencies);
    }

    @Override
    public void readExternal(Element element) throws InvalidDataException {
        super.readExternal(element);
        category = SettingsSupport.getEnum(element, "category", category);
        compileDependencies = SettingsSupport.getBoolean(element, "compile-dependencies", compileDependencies);
    }

    public DBRunConfigCategory getCategory() {
        return category;
    }

    public void setCategory(DBRunConfigCategory category) {
        this.category = category;
    }

    public boolean isCompileDependencies() {
        return compileDependencies;
    }

    public void setCompileDependencies(boolean compileDependencies) {
        this.compileDependencies = compileDependencies;
    }

    public I getExecutionInput() {
        return executionInput;
    }

    @Override
    public boolean isGeneratedName() {
        return isGeneratedName;
    }

    public void setGeneratedName(boolean generatedName) {
        isGeneratedName = generatedName;
    }

    public void setExecutionInput(I executionInput) {
        this.executionInput = executionInput;
    }

    public abstract List<DBMethod> getMethods();

    @Nullable
    public abstract PresentableConnectionProvider getSource();

    @Nullable
    public final ConnectionHandler getConnectionHandler() {
        PresentableConnectionProvider source = getSource();
        return source == null ? null : source.getConnectionHandler();
    }


    @Override
    public boolean excludeCompileBeforeLaunchOption() {
        return true;
    }

    public DBDebuggerType getDebuggerType() {
        DBRunConfigFactory factory = (DBRunConfigFactory) getFactory();
        return factory == null ? DBDebuggerType.NONE : factory.getDebuggerType();
    }
}
