package com.dci.intellij.dbn.vfs;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.navigation.psi.NavigationPsiCache;
import com.dci.intellij.dbn.object.common.DBObject;
import com.intellij.lang.Language;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.testFramework.LightVirtualFile;

public class DatabaseFileViewProvider extends SingleRootFileViewProvider {
    public DatabaseFileViewProvider(@NotNull PsiManager manager, @NotNull VirtualFile virtualFile, boolean eventSystemEnabled) {
        super(manager, virtualFile, eventSystemEnabled);
    }

    public DatabaseFileViewProvider(@NotNull PsiManager psiManager, @NotNull VirtualFile virtualFile, boolean eventSystemEnabled, @NotNull Language language) {
        super(psiManager, virtualFile, eventSystemEnabled, language);
    }

    @Override
    public boolean isPhysical() {
        return super.isPhysical();
    }

    @Override
    protected PsiFile getPsiInner(@NotNull Language language) {
        if (language instanceof DBLanguage) {
            VirtualFile virtualFile = getVirtualFile();
            if (virtualFile instanceof DatabaseObjectFile) {
                DatabaseObjectFile objectFile = (DatabaseObjectFile) virtualFile;
                DBObject object = objectFile.getObject();
                return NavigationPsiCache.getPsiFile(object);
            }

            DBLanguage baseLanguage = (DBLanguage) getBaseLanguage();
            PsiFile psiFile = super.getPsiInner(baseLanguage);
            if (psiFile == null) {
                DatabaseFile databaseFile = getDatabaseFile(virtualFile);
                if (databaseFile != null) {
                    return databaseFile.initializePsiFile(this, (DBLanguage) language);
                }
            } else {
                return psiFile;
            }
        }

        return super.getPsiInner(language);
    }

    private DatabaseFile getDatabaseFile(VirtualFile virtualFile) {
        if (virtualFile instanceof DatabaseFile) {
            return (DatabaseFile) virtualFile;
        }

        if (virtualFile instanceof LightVirtualFile) {
            LightVirtualFile lightVirtualFile = (LightVirtualFile) virtualFile;
            VirtualFile originalFile = lightVirtualFile.getOriginalFile();
            if (originalFile != null && originalFile != virtualFile) {
                return getDatabaseFile(originalFile);
            }
        }
        return null;
    }

    @NotNull
    @Override
    public SingleRootFileViewProvider createCopy(@NotNull VirtualFile copy) {
        return new DatabaseFileViewProvider(getManager(), copy, false, getBaseLanguage());
    }

    @NotNull
    @Override
    public VirtualFile getVirtualFile() {
        VirtualFile virtualFile = super.getVirtualFile();
/*
        if (virtualFile instanceof SourceCodeFile)  {
            SourceCodeFile sourceCodeFile = (SourceCodeFile) virtualFile;
            return sourceCodeFile.getDatabaseFile();
        }
*/
        return virtualFile;
    }
}
