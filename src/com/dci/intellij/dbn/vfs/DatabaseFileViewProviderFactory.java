package com.dci.intellij.dbn.vfs;

import com.dci.intellij.dbn.language.common.DBLanguageFileType;
import com.intellij.lang.Language;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.FileViewProviderFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SingleRootFileViewProvider;
import org.jetbrains.annotations.NotNull;

public class DatabaseFileViewProviderFactory implements FileViewProviderFactory{
    @NotNull
    public FileViewProvider createFileViewProvider(@NotNull VirtualFile file, Language language, @NotNull PsiManager manager, boolean eventSystemEnabled) {

        return file instanceof DBObjectVirtualFile ||
                file instanceof DBConsoleVirtualFile ||
                file instanceof DBSourceCodeVirtualFile ||
                (file instanceof DBVirtualFile && file.getFileType() instanceof DBLanguageFileType) ?
                new DatabaseFileViewProvider(manager, file, eventSystemEnabled, language) :
                new SingleRootFileViewProvider(manager, file);
    }
}
