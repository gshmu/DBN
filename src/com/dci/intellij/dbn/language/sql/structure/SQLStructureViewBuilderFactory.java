package com.dci.intellij.dbn.language.sql.structure;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.editor.structure.EmptyStructureViewModel;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
import com.intellij.lang.PsiStructureViewFactory;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiEditorUtil;

public class SQLStructureViewBuilderFactory implements PsiStructureViewFactory {

    public StructureViewBuilder getStructureViewBuilder(final PsiFile psiFile) {
        return new TreeBasedStructureViewBuilder() {
            @NotNull
            public StructureViewModel createStructureViewModel() {
                return psiFile == null || !psiFile.isValid() || PsiEditorUtil.Service.getInstance() == null ? EmptyStructureViewModel.INSTANCE : new SQLStructureViewModel(null, psiFile);
            }

            @NotNull
            @Override
            public StructureViewModel createStructureViewModel(@Nullable Editor editor) {
                PsiFile psiFile = DocumentUtil.getFile(editor);
                return psiFile == null || !psiFile.isValid() || PsiEditorUtil.Service.getInstance() == null ? EmptyStructureViewModel.INSTANCE : new SQLStructureViewModel(editor, psiFile);
            }
        };
    }
}
