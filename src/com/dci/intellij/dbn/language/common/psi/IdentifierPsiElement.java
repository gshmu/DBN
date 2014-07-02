package com.dci.intellij.dbn.language.common.psi;

import com.dci.intellij.dbn.code.common.style.formatting.FormattingAttributes;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.language.common.element.IdentifierElementType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.QualifiedIdentifierElementType;
import com.dci.intellij.dbn.language.common.element.impl.QualifiedIdentifierVariant;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.lookup.AliasDefinitionLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.IdentifierLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.ObjectDefinitionLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.PsiLookupAdapter;
import com.dci.intellij.dbn.language.common.psi.lookup.VariableDefinitionLookupAdapter;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBSynonym;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBVirtualObject;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.util.IncorrectOperationException;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.util.Set;

public class IdentifierPsiElement extends LeafPsiElement implements PsiNamedElement {
    public IdentifierPsiElement(ASTNode astNode, IdentifierElementType elementType) {
        super(astNode, elementType);

    }

    public IdentifierElementType getElementType() {
        return (IdentifierElementType) super.getElementType();
    }

    public ItemPresentation getPresentation() {
        return this;
    }

    public boolean isQuoted() {
        CharSequence charSequence = getChars();

        char firstChar = charSequence.charAt(0);
        char lastChar = charSequence.charAt(charSequence.length() - 1);

        if (!Character.isLetterOrDigit(firstChar) && !Character.isLetterOrDigit(lastChar)) {
            char quotesChar = getIdentifierQuotesChar();
            return (firstChar == quotesChar && lastChar == quotesChar);
        }
        return false;
    }

    @Override
    public String getName() {
        return getText();
    }

    @Override
    public FormattingAttributes getFormattingAttributes() {
        return super.getFormattingAttributes();
    }

    /**
     * ******************************************************
     * ItemPresentation                *
     * *******************************************************
     */
    public String getPresentableText() {
        StringBuilder builder = new StringBuilder();
        StringUtil.appendToUpperCase(builder,  getUnquotedText());
        builder.append(" (");
        builder.append(getObjectType());
        builder.append(")");
        return builder.toString();
    }

    @Nullable
    public String getLocationString() {
        return null;
    }

    @Nullable
    public Icon getIcon(boolean open) {
        DBObjectType type = getObjectType();
        return type.getIcon();
    }

    @Nullable
    public TextAttributesKey getTextAttributesKey() {
        return null;
    }


    /**
     * ******************************************************
     * Lookup routines                 *
     * *******************************************************
     */
    public BasePsiElement lookupPsiElement(PsiLookupAdapter lookupAdapter, int scopeCrossCount) {
        if (lookupAdapter instanceof IdentifierLookupAdapter) {
            IdentifierLookupAdapter identifierLookupAdapter = (IdentifierLookupAdapter) lookupAdapter;
            if (identifierLookupAdapter.matchesName(this)) {
                PsiElement parentPsiElement = getParent();
                if (parentPsiElement instanceof QualifiedIdentifierPsiElement) {
                    QualifiedIdentifierPsiElement qualifiedIdentifierPsiElement = (QualifiedIdentifierPsiElement) parentPsiElement;
                    QualifiedIdentifierElementType qualifiedIdentifierElementType = qualifiedIdentifierPsiElement.getElementType();
                    if (!qualifiedIdentifierElementType.containsObjectType(identifierLookupAdapter.getObjectType())) {
                        return null;
                    }
                }
                return lookupAdapter.matches(this) ? this : null;
            }
        }
        return null;

    }

    public Set<BasePsiElement> collectPsiElements(PsiLookupAdapter lookupAdapter, Set<BasePsiElement> bucket, int scopeCrossCount) {
        if (lookupAdapter instanceof IdentifierLookupAdapter) {
            IdentifierLookupAdapter identifierLookupAdapter = (IdentifierLookupAdapter) lookupAdapter;
            if (identifierLookupAdapter.matchesName(this)) {
                if (lookupAdapter.matches(this)) {
                    if (bucket == null) bucket = new THashSet<BasePsiElement>();
                    bucket.add(this);
                }
            }
        }

        return bucket;
    }

    public void collectSubjectPsiElements(Set<BasePsiElement> bucket) {
        if (getElementType().is(ElementTypeAttribute.SUBJECT)) {
            bucket.add(this);
        }
    }

    public void collectExecVariablePsiElements(Set<ExecVariablePsiElement> bucket) {
    }

    /**
     * ******************************************************
     * Miscellaneous                     *
     * *******************************************************
     */
    public boolean isObject() {
        return getElementType().isObject();
    }

    public boolean isAlias() {
        return getElementType().isAlias();
    }

    public boolean isVariable() {
        return getElementType().isVariable();
    }


    public boolean isDefinition() {
        return getElementType().isDefinition();
    }

    public boolean isReference() {
        return getElementType().isReference();
    }
    
    public boolean isReferenceable() {
        return getElementType().isReferenceable();
    }

    public boolean isObjectOfType(DBObjectType objectType) {
        return getElementType().isObjectOfType(objectType);
    }

    public boolean isLocalReference() {
        return getElementType().isLocalReference();
    }

    public DBObjectType getObjectType() {
        return getElementType().getObjectType();
    }

    public String getObjectTypeName() {
        return getElementType().getObjectTypeName();
    }

    /**
     * Looks-up whatever underlying database object may be referenced from this identifier.
     * - if this references to a synonym, the DBObject behind the synonym is returned.
     * - if this is an alias reference or definition, it returns the underlying DBObject of the aliased identifier.
     *
     * @return real underlying database object behind the identifier.
     */
    @Nullable
    public synchronized DBObject resolveUnderlyingObject() {
        DBObject underlyingObject = null;
        PsiElement psiElement = resolve();
        if (isObject()) {
            if (psiElement instanceof DBObject) {
                DBObject object = (DBObject) psiElement;
                underlyingObject = object.getUndisposedElement();
            } else if (psiElement instanceof IdentifierPsiElement) {
                IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) psiElement;
                PsiElement underlyingPsiElement = identifierPsiElement.resolve();
                if (underlyingPsiElement instanceof DBObject) {
                    underlyingObject = (DBObject) underlyingPsiElement;
                }
            }
        } else if (isAlias()) {
            BasePsiElement aliasDefinition;
            if (isDefinition()) {
                aliasDefinition = this;
            } else {
                BasePsiElement resolveScope = getEnclosingScopePsiElement();

                PsiLookupAdapter lookupAdapter = new AliasDefinitionLookupAdapter(this, getObjectType(), getUnquotedText());
                aliasDefinition = lookupAdapter.findInScope(resolveScope);
            }

            if (aliasDefinition != null && aliasDefinition instanceof IdentifierPsiElement) {
                BasePsiElement aliasedObject = PsiUtil.resolveAliasedEntityElement((IdentifierPsiElement) aliasDefinition);
                if (aliasedObject != null) {
                    if (aliasedObject.isVirtualObject()) {
                        return aliasedObject.resolveUnderlyingObject();
                    } else if (aliasedObject instanceof IdentifierPsiElement) {
                        IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) aliasedObject;
                        PsiElement underlyingPsiElement = identifierPsiElement.resolve();
                        if (underlyingPsiElement != null && underlyingPsiElement instanceof DBObject) {
                            underlyingObject = (DBObject) underlyingPsiElement;
                        }
                    }
                }
            }
        }

        while (underlyingObject != null && underlyingObject instanceof DBSynonym) {
            DBSynonym synonym = (DBSynonym) underlyingObject;
            underlyingObject = synonym.getUnderlyingObject();
        }

        return underlyingObject;
    }

    public NamedPsiElement lookupNamedPsiElement(String id) {
        return null;
    }

    public BasePsiElement lookupPsiElementBySubject(ElementTypeAttribute attribute, CharSequence subjectName, DBObjectType subjectType) {
        if (getElementType().is(attribute) && getElementType().is(ElementTypeAttribute.SUBJECT)) {
            if (subjectType == getObjectType() && StringUtil.equalsIgnoreCase(subjectName, this.getChars())) {
                return this;
            }
        }
        return null;
    }

    /********************************************************
     *                      Variant builders                *
     *******************************************************/

    private Object[] buildAliasRefVariants() {
        SequencePsiElement statement = (SequencePsiElement) lookupEnclosingPsiElement(ElementTypeAttribute.STATEMENT);
        BasePsiElement sourceScope = getEnclosingScopePsiElement();
        PsiLookupAdapter lookupAdapter = new AliasDefinitionLookupAdapter(this, getObjectType(), null);
        Set<BasePsiElement> aliasDefinitions = lookupAdapter.collectInScope(statement, null);
        return aliasDefinitions == null ? new Object[0] : aliasDefinitions.toArray();
    }

    /********************************************************
     *                      Rersolvers                      *
     ********************************************************/

    private void resolveWithinQualifiedIdentifierElement(QualifiedIdentifierPsiElement qualifiedIdentifier) {
        int index = qualifiedIdentifier.getIndexOf(this);

        BasePsiElement parentObjectElement = null;
        DBObject parentObject = null;
        if (index > 0) {
            IdentifierPsiElement parentElement = qualifiedIdentifier.getLeafAtIndex(index - 1);
            if (parentElement.resolve() != null) {
                parentObjectElement = parentElement.isObject() ? parentElement : PsiUtil.resolveAliasedEntityElement(parentElement);
                parentObject = parentObjectElement != null ? parentElement.resolveUnderlyingObject() : null;
            } else {
                return;
            }
        }

        for (QualifiedIdentifierVariant parseVariant : qualifiedIdentifier.getParseVariants()) {
            LeafElementType parseVariantElementType = parseVariant.getLeaf(index);

            if (parseVariantElementType instanceof IdentifierElementType) {
                IdentifierElementType substitutionCandidate = (IdentifierElementType) parseVariantElementType;
                DBObjectType objectType = substitutionCandidate.getObjectType();

                if (parentObject == null) {  // index == 0
                    if (substitutionCandidate.isObject()) {
                        resolveWithScopeParentLookup(objectType, substitutionCandidate);
                    } else if (substitutionCandidate.isAlias()) {
                        PsiLookupAdapter lookupAdapter = new AliasDefinitionLookupAdapter(this, objectType, ref.getText());
                        BasePsiElement referencedElement = lookupAdapter.findInParentScopeOf(this);
                        if (referencedElement != this && isValidReference(referencedElement)) {
                            setElementType(parseVariantElementType);
                            ref.setReferencedElement(referencedElement);
                            ref.setParent(null);
                        }

                    } else if (substitutionCandidate.isVariable()) {
                        PsiLookupAdapter lookupAdapter = new VariableDefinitionLookupAdapter(this, DBObjectType.ANY, ref.getText());
                        BasePsiElement referencedElement = lookupAdapter.findInParentScopeOf(this);
                        if (referencedElement != this && isValidReference(referencedElement)) {
                            setElementType(parseVariantElementType);
                            ref.setReferencedElement(referencedElement);
                            ref.setParent(null);
                        }

                    }
                } else { // index > 0
                    IdentifierElementType parentElementType = (IdentifierElementType) parseVariant.getLeaf(index - 1);
                    if (parentObject.isOfType(parentElementType.getObjectType())) {
                        DBObject referencedElement = parentObject.getChildObject(objectType, ref.getText().toString(), false);
                        if (isValidReference(referencedElement)) {
                            setElementType(parseVariantElementType);
                            ref.setReferencedElement(referencedElement);
                            ref.setParent(parentObjectElement);
                        }
                    }

                }
                if (ref.getReferencedElement() != null) {
                    return;
                }
            }
        }
    }

    private void resolveWithScopeParentLookup(DBObjectType objectType, IdentifierElementType substitutionCandidate) {
        if (substitutionCandidate.isObject()) {
            ConnectionHandler activeConnection = ref.getActiveConnection();
            if (!substitutionCandidate.isLocalReference() && activeConnection != null && !activeConnection.isVirtual()) {
                Set<DBObject> parentObjects = identifyPotentialParentObjects(objectType, null, this, this);
                if (parentObjects != null && parentObjects.size() > 0) {
                    for (DBObject parentObject : parentObjects) {
                        /*BasePsiElement probableParentObjectElement = null;
                        if (parentObject instanceof IdentifierPsiElement) {
                           IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) parentObject;
                           probableParentObjectElement =
                                   identifierPsiElement.isObject() ?
                                           identifierPsiElement :
                                           PsiUtil.resolveAliasedEntityElement(identifierPsiElement);

                           parentObject = identifierPsiElement.resolveUnderlyingObject();
                       } */

                        PsiElement referencedElement = parentObject.getChildObject(objectType, ref.getText().toString(), false);
                        if (isValidReference(referencedElement)) {
                            ref.setParent(null);
                            ref.setReferencedElement(referencedElement);
                            setElementType(substitutionCandidate);
                            return;
                        }
                    }
                }

                DBObjectBundle objectBundle = activeConnection.getObjectBundle();
                PsiElement referencedElement = objectBundle.getObject(objectType, ref.getText().toString());
                if (isValidReference(referencedElement)) {
                    ref.setParent(null);
                    ref.setReferencedElement(referencedElement);
                    setElementType(substitutionCandidate);
                    return;
                }

                DBSchema schema = getCurrentSchema();
                if (schema != null && objectType.isSchemaObject()) {
                    referencedElement = schema.getChildObject(objectType, ref.getText().toString(), false);
                    if (isValidReference(referencedElement)) {
                        ref.setParent(null);
                        ref.setReferencedElement(referencedElement);
                        setElementType(substitutionCandidate);
                        return;
                    }
                }
            }
            if (!substitutionCandidate.isDefinition()){
                PsiLookupAdapter lookupAdapter = new ObjectDefinitionLookupAdapter(this, objectType, ref.getText());
                PsiElement referencedElement = lookupAdapter.findInParentScopeOf(this);
                if (referencedElement != this && isValidReference(referencedElement)) {
                    ref.setParent(null);
                    ref.setReferencedElement(referencedElement);
                    setElementType(substitutionCandidate);
                    return;
                }
            }
        } else if (substitutionCandidate.isAlias()) {
            PsiLookupAdapter lookupAdapter = new AliasDefinitionLookupAdapter(this, objectType, ref.getText());
            BasePsiElement referencedElement = lookupAdapter.findInParentScopeOf(this);
            if (referencedElement != null && referencedElement != this) {
                ref.setParent(null);
                ref.setReferencedElement(referencedElement);
            }
        } else if (substitutionCandidate.isVariable()) {
            PsiLookupAdapter lookupAdapter = new VariableDefinitionLookupAdapter(this, DBObjectType.ANY, ref.getText());
            BasePsiElement referencedElement = lookupAdapter.findInParentScopeOf(this);
            if (referencedElement != null && referencedElement != this) {
                ref.setParent(null);
                ref.setReferencedElement(referencedElement);
            }
        }
    }

    private boolean isValidReference(PsiElement referencedElement) {
        if (referencedElement != null && referencedElement != this) {
            if (referencedElement instanceof DBVirtualObject) {
                DBVirtualObject object = (DBVirtualObject) referencedElement;
                if (object.getUnderlyingPsiElement().containsPsiElement(this)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * ******************************************************
     * PsiReference                    *
     * *******************************************************
     */
    private PsiResolveResult ref;

    @Nullable
    public PsiElement resolve() {
        if (isResolving()) {
            return ref.getReferencedElement();
        }
        if (isDefinition() && (isAlias() || isVariable())) {
            // alias definitions do not have references.
            // underlying object is determined on runtime
            return null;
        }

        ConnectionHandler connectionHandler = getActiveConnection();
        if ((connectionHandler == null || connectionHandler.isVirtual()) && isObject() && isDefinition()) {
            return null;
        }
        if (ref == null) ref = new PsiResolveResult(this);
        if (ref.isDirty()) {
            //System.out.println("resolving " + getTextRange() + " " + getText());
            try {
                //DatabaseLoadMonitor.setEnsureDataLoaded(false);

                ref.preResolve(this);
                if (getParent() instanceof QualifiedIdentifierPsiElement) {
                    QualifiedIdentifierPsiElement qualifiedIdentifier = (QualifiedIdentifierPsiElement) getParent();
                    resolveWithinQualifiedIdentifierElement(qualifiedIdentifier);
                } else {
                    resolveWithScopeParentLookup(getObjectType(), getElementType());
                }
            } finally {
                ref.postResolve();
                //DatabaseLoadMonitor.setEnsureDataLoaded(false);
            }
        }
        return ref.getReferencedElement();
    }

    @Override
    public boolean isReferenceTo(PsiElement element) {
        return element != this && element == resolve();

/*
        if (element instanceof IdentifierPsiElement) {
            IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) element;
            if (StringUtil.equalsIgnoreCase(getChars(), identifierPsiElement.getChars())) {
                if (isReference() && identifierPsiElement.isDefinition() && getObjectType().matches(identifierPsiElement.getObjectType())) {
                    return true;
                }
            }

        } else if (element instanceof DBObject) {
            DBObject object = (DBObject) element;
            if (StringUtil.equalsIgnoreCase(getChars(), object.getName())) {
                if (getObjectType().matches(object.getObjectType())) {
                    return true;
                }
            }
        }

        return false;
*/
    }

    public CharSequence getUnquotedText() {
        CharSequence text = getChars();
        if (isQuoted() && text.length() > 1) {
            return text.subSequence(1, text.length() - 1);
        }
        return text;
    }

    public boolean textMatches(@NotNull CharSequence text) {
        CharSequence chars = getChars();
        if (isQuoted())  {
            return chars.length() == text.length() + 2 && StringUtil.indexOfIgnoreCase(chars, text, 0) == 1;
        } else {
            return StringUtil.equalsIgnoreCase(chars, text);
        }
    }

    public boolean isSoft() {
        return isDefinition();
    }

    public boolean hasErrors() {
        return false;
    }

    @Override
    public boolean equals(BasePsiElement basePsiElement) {
        if (this == basePsiElement) {
            return true;
        } else {
            if (basePsiElement instanceof IdentifierPsiElement) {
                IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) basePsiElement;
                if (identifierPsiElement.getElementType().isSameAs(getElementType())) {
                    CharSequence localText = getChars();
                    CharSequence remoteText = identifierPsiElement.getChars();
                    return StringUtil.equalsIgnoreCase(localText, remoteText);
                }
            }

            return false;
        }
    }

    @Override
    public boolean matches(BasePsiElement basePsiElement) {
        if (this == basePsiElement) {
            return true;
        } else {
            if (basePsiElement instanceof IdentifierPsiElement) {
                IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) basePsiElement;
                return identifierPsiElement.getElementType().isSameAs(getElementType()) &&
                        StringUtil.equalsIgnoreCase(identifierPsiElement.getChars(), getChars());
            }

            return false;
        }
    }

    public boolean isResolved() {
        return ref != null && !ref.isDirty();
    }

    public boolean isResolving() {
        return ref != null && ref.isResolving();
    }

    public PsiElement setName(@NotNull @NonNls String name) throws IncorrectOperationException {
        return null;
    }

    public int getResolveTrialsCount() {
        return ref == null ? 0 : ref.getOverallResolveTrials();
    }
}
