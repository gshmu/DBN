package com.dci.intellij.dbn.object.common;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.language.common.PsiElementRef;
import com.dci.intellij.dbn.navigation.psi.DBObjectPsiDirectory;
import com.dci.intellij.dbn.navigation.psi.DBObjectPsiFile;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DBObjectPsiCache {
    private DBObjectRef<?> objectRef;
    private PsiElementRef<PsiElement> psiElementRef;

    private final Latent<PsiFile> psiFile = Latent.basic(() -> new DBObjectPsiFile(objectRef));
    private final Latent<PsiElement> psiElement = Latent.basic(() -> new DBObjectPsiElement(objectRef));
    private final Latent<PsiDirectory> psiDirectory = Latent.basic(() -> new DBObjectPsiDirectory(objectRef));

    public DBObjectPsiCache() {
    }

    public DBObjectPsiCache(@NotNull PsiElement psiElement) {
        psiElementRef = PsiElementRef.from(psiElement);
    }

    public DBObjectPsiCache(DBObjectRef<?> objectRef) {
        this.objectRef = objectRef;
    }

    public PsiFile getPsiFile() {
        return psiFile.get();
    }

    @Nullable
    public PsiElement getPsiElement() {
        return psiElementRef == null ? psiElement.get() : PsiElementRef.get(psiElementRef);
    }

    public PsiDirectory getPsiDirectory() {
        return psiDirectory.get();
    }

    public static PsiDirectory asPsiDirectory(@Nullable DBObject object) {
        return object == null ? null : Failsafe.nn(object).getPsiCache().getPsiDirectory();
    }

    @Nullable
    public static PsiElement asPsiElement(@Nullable DBObject object) {
        return object == null ? null : Failsafe.nn(object).getPsiCache().getPsiElement();
    }

    @Nullable
    public static PsiFile asPsiFile(@Nullable DBObject object) {
        return object == null ? null : Failsafe.nn(object).getPsiCache().getPsiFile();
    }

}
