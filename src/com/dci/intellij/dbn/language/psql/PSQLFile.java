package com.dci.intellij.dbn.language.psql;

import com.dci.intellij.dbn.language.common.DBLanguageFile;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.language.common.psi.lookup.IdentifierDefinitionLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.ObjectDefinitionLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.PsiLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.VariableDefinitionLookupAdapter;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class PSQLFile extends DBLanguageFile {

    public PSQLFile(FileViewProvider fileViewProvider, @NotNull PSQLLanguage language) {
        super(fileViewProvider, PSQLFileType.INSTANCE, language);
    }

    public BasePsiElement lookupObjectSpecification(DBObjectType objectType, CharSequence objectName) {
        for (PsiElement psiElement : getChildren()) {
            if (psiElement instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) psiElement;
                PsiLookupAdapter lookupAdapter = new ObjectDefinitionLookupAdapter(null, objectType, objectName, ElementTypeAttribute.SUBJECT);
                BasePsiElement specObject = lookupAdapter.findInScope(basePsiElement);
                if (specObject != null) {
                    return specObject.lookupEnclosingPsiElement(ElementTypeAttribute.OBJECT_SPECIFICATION);
                }
            }
        }
        return null;
    }

    public BasePsiElement lookupObjectDeclaration(DBObjectType objectType, CharSequence objectName) {
        for (PsiElement psiElement : getChildren()) {
            if (psiElement instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) psiElement;
                PsiLookupAdapter lookupAdapter = new ObjectDefinitionLookupAdapter(null, objectType, objectName, ElementTypeAttribute.SUBJECT);
                BasePsiElement specObject = lookupAdapter.findInScope(basePsiElement);
                if (specObject != null) {
                    return specObject.lookupEnclosingPsiElement(ElementTypeAttribute.OBJECT_DECLARATION);
                }
            }
        }
        return null;
    }

    public Set<BasePsiElement> lookupVariableDefinition(int offset) {
        BasePsiElement scope = PsiUtil.lookupElementAtOffset(this, ElementTypeAttribute.SCOPE_DEMARCATION, offset);
        Set<BasePsiElement> variableDefinitions = new THashSet<BasePsiElement>();
        while (scope != null) {
            PsiLookupAdapter lookupAdapter = new IdentifierDefinitionLookupAdapter(null, DBObjectType.ARGUMENT, null);
            variableDefinitions = scope.collectPsiElements(lookupAdapter, variableDefinitions, 0);

            lookupAdapter = new VariableDefinitionLookupAdapter(null, DBObjectType.ANY, null);
            variableDefinitions = scope.collectPsiElements(lookupAdapter, variableDefinitions, 0);

            PsiElement parent = scope.getParent();
            if (parent instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) parent;
                scope = basePsiElement.lookupEnclosingPsiElement(ElementTypeAttribute.SCOPE_DEMARCATION);
                if (scope == null) scope = basePsiElement.lookupEnclosingPsiElement(ElementTypeAttribute.SCOPE_ISOLATION);
            } else {
                scope = null;
            }
        }
        return variableDefinitions;
    }
}
