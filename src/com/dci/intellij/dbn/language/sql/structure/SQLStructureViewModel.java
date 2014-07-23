package com.dci.intellij.dbn.language.sql.structure;

import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.intellij.ide.structureView.TextEditorBasedStructureViewModel;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.Grouper;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.ide.util.treeView.smartTree.Filter;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class SQLStructureViewModel extends TextEditorBasedStructureViewModel {

    public SQLStructureViewModel(Editor editor, PsiFile psiFile) {
        super(editor, psiFile);
    }

    @NotNull
    protected Class[] getSuitableClasses() {
        return new Class[] {BasePsiElement.class};
    }

    @NotNull
    public StructureViewTreeElement getRoot() {
        return new SQLStructureViewElement(getPsiFile());
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
}
