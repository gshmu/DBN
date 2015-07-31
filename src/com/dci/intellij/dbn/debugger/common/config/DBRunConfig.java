package com.dci.intellij.dbn.debugger.common.config;

import java.util.List;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.common.util.LazyValue;
import com.dci.intellij.dbn.common.util.SimpleLazyValue;
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

public abstract class DBRunConfig<I extends ExecutionInput> extends RunConfigurationBase implements RunConfigurationWithSuppressedDefaultRunAction, LocatableConfiguration {
    private LazyValue<DBRunConfigEditor> configurationEditor = new SimpleLazyValue<DBRunConfigEditor>() {
        @Override
        protected DBRunConfigEditor load() {
            return createConfigurationEditor();
        }
    };
    private boolean isGeneratedName = true;
    private boolean compileDependencies = true;
    private boolean generic;
    private I executionInput;

    protected DBRunConfig(Project project, DBRunConfigFactory factory, String name, boolean generic) {
        super(project, factory, name);
        this.generic = generic;
    }

    @NotNull
    @Override
    public DBRunConfigType getType() {
        return (DBRunConfigType) super.getType();
    }

    protected void resetConfigurationEditor() {
        configurationEditor.set(null);
    }

    public DBRunConfigEditor getConfigurationEditor() {
        return configurationEditor.get();
    }
    protected abstract DBRunConfigEditor createConfigurationEditor();

    @Override
    public boolean canRunOn(@NotNull ExecutionTarget target) {
        return super.canRunOn(target);
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        super.writeExternal(element);
        SettingsUtil.setBoolean(element, "is-generic", generic);
        SettingsUtil.setBoolean(element, "compile-dependencies", compileDependencies);
    }

    @Override
    public void readExternal(Element element) throws InvalidDataException {
        super.readExternal(element);
        generic = SettingsUtil.getBoolean(element, "is-generic", generic);
        compileDependencies = SettingsUtil.getBoolean(element, "compile-dependencies", compileDependencies);
    }

    public boolean isGeneric() {
        return generic;
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
        return factory.getDebuggerType();
    }
}
