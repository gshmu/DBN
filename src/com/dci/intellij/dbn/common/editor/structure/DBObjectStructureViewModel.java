package com.dci.intellij.dbn.common.editor.structure;

import com.intellij.ide.structureView.FileEditorPositionListener;
import com.intellij.ide.structureView.ModelListener;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.util.treeView.smartTree.Grouper;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.ide.util.treeView.smartTree.Filter;
import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public abstract class DBObjectStructureViewModel implements StructureViewModel {
    protected Set<FileEditorPositionListener> fileEditorPositionListeners = new HashSet<FileEditorPositionListener>();
    protected Set<ModelListener> modelListeners = new HashSet<ModelListener>();

    public void addEditorPositionListener(FileEditorPositionListener listener) {
        fileEditorPositionListeners.add(listener);
    }

    public void removeEditorPositionListener(FileEditorPositionListener listener) {
        fileEditorPositionListeners.remove(listener);
    }

    public void addModelListener(ModelListener modelListener) {
        modelListeners.add(modelListener);
    }

    public void removeModelListener(ModelListener modelListener) {
        modelListeners.remove(modelListener);
    }

    public void dispose() {
    }

    public boolean shouldEnterElement(Object o) {
        return false;
    }

    @NotNull
    public Grouper[] getGroupers() {
        return Grouper.EMPTY_ARRAY;
    }

    @NotNull
    public Sorter[] getSorters() {
        return new Sorter[0];
    }

    @NotNull
    public Filter[] getFilters() {
        return new Filter[0];
    }

    public void rebuild() {
        for (ModelListener modelListener : modelListeners) {
            modelListener.onModelChanged();
        }

        for (FileEditorPositionListener positionListener : fileEditorPositionListeners) {
            positionListener.onCurrentElementChanged();
        }
    }
}
