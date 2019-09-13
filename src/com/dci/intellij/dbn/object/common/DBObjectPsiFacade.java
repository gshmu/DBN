package com.dci.intellij.dbn.object.common;

import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.latent.RuntimeLatent;
import com.dci.intellij.dbn.language.common.PsiElementRef;
import com.dci.intellij.dbn.navigation.psi.DBObjectPsiDirectory;
import com.dci.intellij.dbn.navigation.psi.DBObjectPsiFile;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DBObjectPsiFacade extends DisposableBase {
    private DBObjectRef objectRef;
    private PsiElementRef<PsiElement> psiElementRef;

    private RuntimeLatent<PsiFile> psiFile = Latent.runtime(() -> new DBObjectPsiFile(objectRef));
    private RuntimeLatent<PsiElement> psiElement = Latent.runtime(() -> new DBObjectPsiElement(objectRef));
    private RuntimeLatent<PsiDirectory> psiDirectory = Latent.runtime(() -> new DBObjectPsiDirectory(objectRef));

    public DBObjectPsiFacade() {
    }

    public DBObjectPsiFacade(@NotNull PsiElement psiElement) {
        psiElementRef = PsiElementRef.from(psiElement);
    }

    public DBObjectPsiFacade(DBObjectRef objectRef) {
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
        return object == null ? null : Failsafe.nn(object).getPsiFacade().getPsiDirectory();
    }

    @Nullable
    public static PsiElement asPsiElement(@Nullable DBObject object) {
        return object == null ? null : Failsafe.nn(object).getPsiFacade().getPsiElement();
    }

    @Nullable
    public static PsiFile asPsiFile(@Nullable DBObject object) {
        return object == null ? null : Failsafe.nn(object).getPsiFacade().getPsiFile();
    }

}
