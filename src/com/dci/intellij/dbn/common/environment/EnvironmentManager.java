package com.dci.intellij.dbn.common.environment;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.environment.options.EnvironmentSettings;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.editor.DBEditorTabColorProvider;
import com.dci.intellij.dbn.options.general.GeneralProjectSettings;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.impl.EditorsSplitters;
import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class EnvironmentManager extends AbstractProjectComponent implements JDOMExternalizable, Disposable, EnvironmentChangeListener {
    private EnvironmentManager(Project project) {
        super(project);
        EventManager.subscribe(project, EnvironmentChangeListener.TOPIC, this);

    }

    public static EnvironmentManager getInstance(Project project) {
        return project.getComponent(EnvironmentManager.class);
    }
    
    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.EnvironmentManager";
    }
    
    @Override
    public void environmentConfigChanged(String environmentTypeId) {
        FileEditorManagerImpl fileEditorManager = (FileEditorManagerImpl) FileEditorManager.getInstance(getProject());
        VirtualFile[] openFiles = fileEditorManager.getOpenFiles();
        Set<EditorsSplitters> splitters = fileEditorManager.getAllSplitters();
        for (VirtualFile virtualFile : openFiles) {
            ConnectionHandler connectionHandler = DBEditorTabColorProvider.getConnectionHandler(virtualFile, getProject());
            if (connectionHandler != null && !connectionHandler.isVirtual() && !connectionHandler.isDisposed() && connectionHandler.getSettings().getDetailSettings().getEnvironmentTypeId().equals(environmentTypeId)) {
                for (EditorsSplitters splitter : splitters) {
                    splitter.updateFileBackgroundColor(virtualFile);
                }
            }
        }
    }

    @Override
    public void environmentVisibilitySettingsChanged() {
        FileEditorManagerImpl fileEditorManager = (FileEditorManagerImpl) FileEditorManager.getInstance(getProject());
        VirtualFile[] openFiles = fileEditorManager.getOpenFiles();
        Set<EditorsSplitters> splitters = fileEditorManager.getAllSplitters();
        for (VirtualFile virtualFile : openFiles) {
            for (EditorsSplitters splitter : splitters) {
                splitter.updateFileBackgroundColor(virtualFile);
            }
        }
    }

    /****************************************
    *            JDOMExternalizable         *
    *****************************************/
    public void readExternal(Element element) throws InvalidDataException {

    }

    public void writeExternal(Element element) throws WriteExternalException {

    }

    public void dispose() {
        EventManager.unsubscribe(this);
    }

    public EnvironmentType getEnvironmentType(String id) {
        EnvironmentSettings environmentSettings = GeneralProjectSettings.getInstance(getProject()).getEnvironmentSettings();
        return environmentSettings.getEnvironmentType(id);
    }
}
