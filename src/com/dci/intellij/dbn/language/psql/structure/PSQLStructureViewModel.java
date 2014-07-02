package com.dci.intellij.dbn.language.psql.structure;

import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.intellij.ide.structureView.TextEditorBasedStructureViewModel;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.Grouper;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.ide.util.treeView.smartTree.Filter;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class PSQLStructureViewModel extends TextEditorBasedStructureViewModel {
    private PsiFile psiFile;
    private Sorter[] sorters = new Sorter[] {new PSQLStructureViewModelSorter()};
    private Grouper[] groupers = new Grouper[]{new PSQLStructureViewModelGrouper()};

    public PSQLStructureViewModel(PsiFile psiFile) {
        super(psiFile);
        this.psiFile = psiFile;
    }

    protected PsiFile getPsiFile() {
        return psiFile;
    }

    @NotNull
    protected Class[] getSuitableClasses() {
        return new Class[] {BasePsiElement.class};
    }

    @NotNull
    public StructureViewTreeElement getRoot() {
        return new PSQLStructureViewElement(psiFile);
    }

    @NotNull
    public Grouper[] getGroupers() {
        return groupers;
    }

    @NotNull
    public Sorter[] getSorters() {
        return sorters;
    }

    @NotNull
    public Filter[] getFilters() {
        return new Filter[0];
    }

    @Override
    public boolean shouldEnterElement(Object element) {
        return false;
    }
}