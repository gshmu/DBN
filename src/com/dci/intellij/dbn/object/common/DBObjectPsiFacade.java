package com.dci.intellij.dbn.object.common;

import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.navigation.psi.DBObjectPsiDirectory;
import com.dci.intellij.dbn.navigation.psi.DBObjectPsiFile;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

public final class DBObjectPsiFacade{
    private DBObjectRef objectRef;

    private PsiFile psiFile;
    private PsiElement psiElement;
    private PsiDirectory psiDirectory;

    public DBObjectPsiFacade() {
    }

    public DBObjectPsiFacade(PsiElement psiElement) {
        this.psiElement = psiElement;
    }

    public DBObjectPsiFacade(DBObjectRef objectRef) {
        this.objectRef = objectRef;
    }

    public PsiFile getPsiFile() {
        if (psiFile == null) {
            synchronized (this) {
                if (psiFile == null) {
                    psiFile = new DBObjectPsiFile(objectRef);
                }
            }
        }
        return psiFile;
    }

    public PsiElement getPsiElement() {
        if (psiElement == null) {
            synchronized (this) {
                if (psiElement == null) {
                    psiElement = new DBObjectPsiElement(objectRef);
                }
            }
        }
        return psiElement;
    }

    public PsiDirectory getPsiDirectory() {
        if (psiDirectory == null) {
            synchronized (this) {
                if (psiDirectory == null) {
                    psiDirectory = new DBObjectPsiDirectory(objectRef);
                }
            }
        }
        return psiDirectory;
    }

    public static PsiDirectory getPsiDirectory(DBObject object) {
        return object == null ? null : FailsafeUtil.get(object).getPsiFacade().getPsiDirectory();
    }

    public static PsiElement getPsiElement(DBObject object) {
        return object == null ? null : FailsafeUtil.get(object).getPsiFacade().getPsiElement();
    }

    public static PsiFile getPsiFile(DBObject object) {
        return object == null ? null : FailsafeUtil.get(object).getPsiFacade().getPsiFile();
    }

}
