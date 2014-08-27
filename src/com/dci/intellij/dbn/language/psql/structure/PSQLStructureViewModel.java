package com.dci.intellij.dbn.language.psql.structure;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.language.common.structure.DBLanguageStructureViewModel;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.Grouper;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;

public class PSQLStructureViewModel extends DBLanguageStructureViewModel {
    private Sorter[] sorters = new Sorter[] {new PSQLStructureViewModelSorter()};
    private Grouper[] groupers = new Grouper[]{new PSQLStructureViewModelGrouper()};

    public PSQLStructureViewModel(Editor editor, PsiFile psiFile) {
        super(editor, psiFile);
    }

    @NotNull
    public StructureViewTreeElement getRoot() {
        return new PSQLStructureViewElement(getPsiFile());
    }

    @NotNull
    public Grouper[] getGroupers() {
        return groupers;
    }

    @NotNull
    public Sorter[] getSorters() {
        return sorters;
    }
}