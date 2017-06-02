package com.dci.intellij.dbn.vfs;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.language.common.DBLanguageFileType;
import com.intellij.lang.Language;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.FileViewProviderFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SingleRootFileViewProvider;

public class DatabaseFileViewProviderFactory implements FileViewProviderFactory{

    public static final Key<DatabaseFileViewProvider> CACHED_VIEW_PROVIDER = new Key<>("CACHED_VIEW_PROVIDER");

    @NotNull
    public FileViewProvider createFileViewProvider(@NotNull VirtualFile file, Language language, @NotNull PsiManager manager, boolean eventSystemEnabled) {

        if (file instanceof DBObjectVirtualFile ||
                file instanceof DBConsoleVirtualFile ||
                file instanceof DBSourceCodeVirtualFile ||
                (file instanceof DBVirtualFile && file.getFileType() instanceof DBLanguageFileType)) {

            DBVirtualFile virtualFile = (DBVirtualFile) file;

            DatabaseFileViewProvider viewProvider = virtualFile.getUserData(CACHED_VIEW_PROVIDER);
            if (viewProvider == null) {
                viewProvider = new DatabaseFileViewProvider(manager, file, eventSystemEnabled, language);
                virtualFile.putUserData(CACHED_VIEW_PROVIDER, viewProvider);
            }
            return viewProvider;
        } else{
            return new SingleRootFileViewProvider(manager, file);
        }
    }
}
