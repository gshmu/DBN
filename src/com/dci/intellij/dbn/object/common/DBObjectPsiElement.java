package com.dci.intellij.dbn.object.common;

import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.language.common.psi.EmptySearchScope;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiInvalidElementAccessException;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.EverythingGlobalScope;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class DBObjectPsiElement extends DisposableBase implements PsiNamedElement, NavigationItem {
    private static PsiFile DUMMY_FILE;
    private DBObjectRef objectRef;

    public DBObjectPsiElement(DBObjectRef objectRef) {
        this.objectRef = objectRef;
    }

    @Nullable
    @Override
    public String getName() {
        return objectRef.objectName;
    }

    @Nullable
    @Override
    public ItemPresentation getPresentation() {
        return getObject().getPresentation();
    }

    /*********************************************************
     *                    PsiNamedElement                    *
     *********************************************************/
    @Override
    public final PsiElement setName(@NonNls @NotNull String name) {
        throw new IncorrectOperationException("Operation not supported");
    }

    @Override
    public PsiManager getManager() {return PsiManager.getInstance(getProject());}

    @Override
    @NotNull
    public PsiElement[] getChildren() {
        return PsiElement.EMPTY_ARRAY;
    }

    @Override
    public PsiElement getParent(){return null;}

    @Override
    public PsiElement getFirstChild() {return null;}

    @Override
    public PsiElement getLastChild() {return null;}

    @Override
    public PsiElement getNextSibling() {return null;}

    @Override
    public PsiElement getPrevSibling() {return null;}

    @Override
    public PsiElement findElementAt(int offset) {return null;}

    @Override
    public PsiReference findReferenceAt(int offset) {return null;}

    @Override
    public PsiFile getContainingFile() throws PsiInvalidElementAccessException {
        if (DUMMY_FILE == null) {
            PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(getProject());

            DUMMY_FILE = psiFileFactory.createFileFromText(
                    "object", SQLLanguage.INSTANCE, "");

        }
        return DUMMY_FILE;
    }

    @Override
    public PsiElement getOriginalElement() {return this;}

    @Override
    public boolean textMatches(@NotNull CharSequence text) {return false;}

    @Override
    public boolean textMatches(@NotNull PsiElement element) {return false;}

    @Override
    public boolean textContains(char c) {return false;}

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {}

    @Override
    public PsiElement getNavigationElement() {return this;}

    @Override
    public int getStartOffsetInParent() {return 0;}

    @Override
    public int getTextOffset() {return 0;}

    @Override
    public void acceptChildren(@NotNull PsiElementVisitor visitor) {}

    @Override
    public PsiElement copy() {return this;}

    @Override
    public PsiElement add(@NotNull PsiElement element) {return null;}

    @Override
    public PsiElement addBefore(@NotNull PsiElement element, PsiElement anchor){return null;}

    @Override
    public PsiElement addAfter(@NotNull PsiElement element, PsiElement anchor) {return null;}

    @Override
    public void checkAdd(@NotNull PsiElement element) {}

    @Override
    public PsiElement addRange(PsiElement first, PsiElement last) {return null;}

    @Override
    public PsiElement addRangeBefore(@NotNull PsiElement first, @NotNull PsiElement last, PsiElement anchor) {return null;}

    @Override
    public PsiElement addRangeAfter(PsiElement first, PsiElement last, PsiElement anchor) {return null;}

    @Override
    public void delete() {}

    @Override
    public void checkDelete() {}

    @Override
    public void deleteChildRange(PsiElement first, PsiElement last){}

    @Override
    public PsiElement replace(@NotNull PsiElement newElement) {return null;}

    @Override
    public boolean isValid() {
        return DBObjectRef.get(objectRef) != null;
    }

    @Override
    public boolean isWritable() {return false;}

    @Override
    public PsiReference getReference() {return null;}

    @Override
    @NotNull
    public PsiReference[] getReferences() {return new PsiReference[0];}

    @Override
    public PsiElement getContext() {return null;}

    @Override
    public boolean isPhysical() {return false;}

    @Override
    @NotNull
    public GlobalSearchScope getResolveScope() {return EmptySearchScope.INSTANCE;}

    @Override
    @NotNull
    public SearchScope getUseScope() {return new EverythingGlobalScope();}

    @Override
    public ASTNode getNode() {return null;}

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor psiScopeProcessor, @NotNull ResolveState resolveState, @Nullable PsiElement psiElement, @NotNull PsiElement psiElement1) {return false;}

    @Override
    public <T> T getCopyableUserData(Key<T> key) {return null;}

    @Override
    public <T> void putCopyableUserData(Key<T> key, T value) {}

    @Override
    public <T> T getUserData(@NotNull Key<T> key) {return null;}

    @Override
    public <T> void putUserData(@NotNull Key<T> key, T value) {}

    @Override
    public boolean isEquivalentTo(PsiElement psiElement) {return false;}

    @NotNull
    @Override
    public Project getProject() throws PsiInvalidElementAccessException {
        return getObject().getProject();
    }

    @Override
    @NotNull
    public Language getLanguage() {
        return SQLLanguage.INSTANCE;
    }

    @Override
    public TextRange getTextRange() {
        return new TextRange(0, getText().length());
    }

    @Override
    public int getTextLength() {
        return getText().length();
    }

    @Override
    @NonNls
    public String getText() {
        return getName();
    }

    @Override
    @NotNull
    public char[] textToCharArray() {
        return getText().toCharArray();
    }





    @Override
    public void navigate(boolean requestFocus) {
        getObject().navigate(requestFocus);
    }

    @Override
    public boolean canNavigate() {
        return true;
    }

    @Override
    public boolean canNavigateToSource() {
        return false;
    }

    @Override
    public Icon getIcon(int flags) {
        return getObject().getIcon();
    }

    @NotNull
    public DBObject getObject() {
        return DBObjectRef.getnn(objectRef);
    }

    @Nullable
    public DBObject getObjectLenient() {
        return DBObjectRef.get(objectRef);
    }

    public DBObjectType getObjectType() {
        return objectRef.objectType;
    }
}
