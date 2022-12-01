package com.dci.intellij.dbn.language.psql.structure;

import com.dci.intellij.dbn.common.editor.structure.EmptyStructureViewModel;
import com.dci.intellij.dbn.common.thread.Read;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
import com.intellij.lang.PsiStructureViewFactory;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiEditorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PSQLStructureViewBuilderFactory implements PsiStructureViewFactory {

    @Override
    public StructureViewBuilder getStructureViewBuilder(@NotNull final PsiFile psiFile) {
        return new TreeBasedStructureViewBuilder() {
            @NotNull
            //@Override TODO older versions support. Decommission
            public StructureViewModel createStructureViewModel() {
                return createStructureViewModel(null);
            }

            @NotNull
            @Override
            public StructureViewModel createStructureViewModel(@Nullable Editor editor) {
                return Read.call(() -> {
                    try {
                        return !psiFile.isValid() ||
                                psiFile.getProject().isDisposed() ||
                                PsiEditorUtil.Service.getInstance() == null ?
                                EmptyStructureViewModel.INSTANCE :
                                new PSQLStructureViewModel(editor, psiFile);
                    } catch (Throwable e) {
                        // TODO dirty workaround (compatibility issue)
                        return EmptyStructureViewModel.INSTANCE;
                    }
                });
            }
        };
    }
}