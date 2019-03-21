package com.dci.intellij.dbn.vfs;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.DBLanguageParserDefinition;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectPsiFacade;
import com.dci.intellij.dbn.vfs.file.DBObjectVirtualFile;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.impl.FileDocumentManagerImpl;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DatabaseFileViewProvider extends SingleRootFileViewProvider {
    public static final Key<DatabaseFileViewProvider> CACHED_VIEW_PROVIDER = new Key<DatabaseFileViewProvider>("CACHED_VIEW_PROVIDER");

    public DatabaseFileViewProvider(@NotNull PsiManager manager, @NotNull VirtualFile virtualFile, boolean eventSystemEnabled) {
        super(manager, virtualFile, eventSystemEnabled);
        virtualFile.putUserData(CACHED_VIEW_PROVIDER, this);
        //virtualFile.putUserData(FREE_THREADED, true);
    }

    public DatabaseFileViewProvider(@NotNull PsiManager psiManager, @NotNull VirtualFile virtualFile, boolean eventSystemEnabled, @NotNull Language language) {
        super(psiManager, virtualFile, eventSystemEnabled, language);
        virtualFile.putUserData(CACHED_VIEW_PROVIDER, this);

        //virtualFile.putUserData(FREE_THREADED, true);
    }

    @Override
    public boolean isPhysical() {
        return super.isPhysical();
    }

    @Override
    @Nullable
    protected PsiFile getPsiInner(@NotNull Language language) {
        if (language instanceof DBLanguage || language instanceof DBLanguageDialect) {
            VirtualFile virtualFile = getVirtualFile();
            if (virtualFile instanceof DBObjectVirtualFile) {
                return Failsafe.guarded(null, () -> {
                    DBObjectVirtualFile objectFile = (DBObjectVirtualFile) virtualFile;
                    DBObject object = objectFile.getObject();
                    return DBObjectPsiFacade.getPsiFile(object);
                });
            }

            Language baseLanguage = getBaseLanguage();
            PsiFile psiFile = super.getPsiInner(baseLanguage);
            if (psiFile == null) {
                DBParseableVirtualFile parseableFile = getParseableFile(virtualFile);
                if (parseableFile != null) {
                    parseableFile.initializePsiFile(this, language);
                }
            } else {
                return psiFile;
            }
        }

        return super.getPsiInner(language);
    }

    @NotNull
    public DBLanguagePsiFile initializePsiFile(@NotNull DBLanguageDialect languageDialect) {
        DBLanguagePsiFile file = (DBLanguagePsiFile) getCachedPsi(languageDialect);
        if (file == null) {
            file = (DBLanguagePsiFile) getCachedPsi(languageDialect.getBaseLanguage());
        }
        if (file == null) {
            DBLanguageParserDefinition parserDefinition = languageDialect.getParserDefinition();
            file = (DBLanguagePsiFile) parserDefinition.createFile(this);
            forceCachedPsi(file);
            Document document = DocumentUtil.getDocument(file);// cache hard reference to document (??)
            FileDocumentManagerImpl.registerDocument(document, getVirtualFile());
        }
        return file;
    }

    private static DBParseableVirtualFile getParseableFile(VirtualFile virtualFile) {
        if (virtualFile instanceof DBParseableVirtualFile) {
            return (DBParseableVirtualFile) virtualFile;
        }

        if (virtualFile instanceof LightVirtualFile) {
            LightVirtualFile lightVirtualFile = (LightVirtualFile) virtualFile;
            VirtualFile originalFile = lightVirtualFile.getOriginalFile();
            if (originalFile != null && !originalFile.equals(virtualFile)) {
                return getParseableFile(originalFile);
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
