package com.dci.intellij.dbn.language.sql.structure;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.language.common.structure.DBLanguageStructureViewModel;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.Filter;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;

public class SQLStructureViewModel extends DBLanguageStructureViewModel {
    private Filter[] filters = new Filter[]{new SQLStructureViewModelFilter()};

    public SQLStructureViewModel(Editor editor, PsiFile psiFile) {
        super(editor, psiFile);
    }

    @NotNull
    public StructureViewTreeElement getRoot() {
        return new SQLStructureViewElement(getPsiFile());
    }

    @NotNull
    @Override
    public Filter[] getFilters() {
        return filters;
    }
}
