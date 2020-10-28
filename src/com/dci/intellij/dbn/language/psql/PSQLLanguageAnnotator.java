package com.dci.intellij.dbn.language.psql;

import com.dci.intellij.dbn.code.psql.color.PSQLTextAttributesKeys;
import com.dci.intellij.dbn.code.sql.color.SQLTextAttributesKeys;
import com.dci.intellij.dbn.common.thread.ThreadMonitor;
import com.dci.intellij.dbn.common.thread.ThreadProperty;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.code.SourceCodeManager;
import com.dci.intellij.dbn.editor.code.options.CodeEditorGeneralSettings;
import com.dci.intellij.dbn.execution.statement.StatementGutterRenderer;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.TokenTypeCategory;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.navigation.NavigateToDefinitionAction;
import com.dci.intellij.dbn.language.common.navigation.NavigateToObjectAction;
import com.dci.intellij.dbn.language.common.navigation.NavigateToSpecificationAction;
import com.dci.intellij.dbn.language.common.navigation.NavigationAction;
import com.dci.intellij.dbn.language.common.navigation.NavigationGutterRenderer;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.ChameleonPsiElement;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.language.common.psi.NamedPsiElement;
import com.dci.intellij.dbn.language.common.psi.TokenPsiElement;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.dci.intellij.dbn.options.ProjectSettings;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class PSQLLanguageAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull final PsiElement psiElement, @NotNull final AnnotationHolder holder) {
        ThreadMonitor.run(null,
                ThreadProperty.CODE_ANNOTATING,
                () -> {
                    if (psiElement instanceof BasePsiElement) {
                        BasePsiElement basePsiElement = (BasePsiElement) psiElement;

                        ElementType elementType = basePsiElement.elementType;
                        if (elementType.is(ElementTypeAttribute.OBJECT_SPECIFICATION) || elementType.is(ElementTypeAttribute.OBJECT_DECLARATION)) {
                            annotateSpecDeclarationNavigable(basePsiElement, holder);
                        }

                        if (basePsiElement instanceof TokenPsiElement) {
                            annotateToken((TokenPsiElement) basePsiElement, holder);
                        } else if (basePsiElement instanceof IdentifierPsiElement) {
                            if (!basePsiElement.isInjectedContext()) {
                                IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) basePsiElement;
                                ConnectionHandler connectionHandler = identifierPsiElement.getConnectionHandler();
                                if (connectionHandler != null) {
                                    annotateIdentifier(identifierPsiElement, holder);
                                }
                            }
                        } else if (basePsiElement instanceof NamedPsiElement) {
                            NamedPsiElement namedPsiElement = (NamedPsiElement) basePsiElement;
                            if (namedPsiElement.hasErrors()) {
                                holder.createErrorAnnotation(namedPsiElement, "Invalid " + namedPsiElement.elementType.getDescription());
                            }
                        }

                        if (basePsiElement instanceof ExecutablePsiElement) {
                            ExecutablePsiElement executablePsiElement = (ExecutablePsiElement) basePsiElement;
                            annotateExecutable(executablePsiElement, holder);
                        }
                    } else if (psiElement instanceof ChameleonPsiElement) {
                        Annotation annotation = holder.createInfoAnnotation(psiElement, null);
                        annotation.setTextAttributes(SQLTextAttributesKeys.CHAMELEON);
                    }
                });
    }

    private static void annotateToken(@NotNull TokenPsiElement tokenPsiElement, AnnotationHolder holder) {
        TokenTypeCategory flavor = tokenPsiElement.elementType.getFlavor();
        if (flavor != null) {
            Annotation annotation = holder.createInfoAnnotation(tokenPsiElement, null);
            switch (flavor) {
                case DATATYPE: annotation.setTextAttributes(SQLTextAttributesKeys.DATA_TYPE); break;
                case FUNCTION: annotation.setTextAttributes(SQLTextAttributesKeys.FUNCTION); break;
                case KEYWORD: annotation.setTextAttributes(SQLTextAttributesKeys.KEYWORD); break;
                case IDENTIFIER: annotation.setTextAttributes(SQLTextAttributesKeys.IDENTIFIER); break;
            }
        }
    }

     private static void annotateIdentifier(@NotNull final IdentifierPsiElement identifierPsiElement, final AnnotationHolder holder) {
        if (identifierPsiElement.isInjectedContext()) return;

        if (identifierPsiElement.isReference()) {
            identifierPsiElement.resolve();
        }

         if (identifierPsiElement.isAlias()) {
             if (identifierPsiElement.isReference())
                 annotateAliasRef(identifierPsiElement, holder); else
                 annotateAliasDef(identifierPsiElement, holder);
         }

/*
        if (identifierPsiElement.isObject() && identifierPsiElement.isReference()) {
            annotateObject(identifierPsiElement, holder);
        } else

*/
    }

    private static void annotateAliasRef(IdentifierPsiElement aliasReference, @NotNull AnnotationHolder holder) {
        /*if (aliasReference.resolve() == null) {
            Annotation annotation = holder.createWarningAnnotation(aliasReference, "Unknown identifier");
            annotation.setTextAttributes(PSQLTextAttributesKeys.UNKNOWN_IDENTIFIER);
        } else {
            Annotation annotation = holder.createInfoAnnotation(aliasReference, null);
            annotation.setTextAttributes(PSQLTextAttributesKeys.ALIAS);
        }*/

        Annotation annotation = holder.createInfoAnnotation(aliasReference, null);
        annotation.setTextAttributes(PSQLTextAttributesKeys.ALIAS);
    }

    private static void annotateAliasDef(IdentifierPsiElement aliasDefinition, @NotNull AnnotationHolder holder) {
        /*Set<PsiElement> aliasDefinitions = new HashSet<PsiElement>();
        SequencePsiElement sourceScope = aliasDefinition.getEnclosingScopePsiElement();
        sourceScope.collectAliasDefinitionPsiElements(aliasDefinitions, aliasDefinition.getUnquotedText(), DBObjectType.ANY, null);
        if (aliasDefinitions.size() > 1) {
            holder.createWarningAnnotation(aliasDefinition, "Duplicate alias definition: " + aliasDefinition.getUnquotedText());
        }*/
        Annotation annotation = holder.createInfoAnnotation(aliasDefinition, null);
        annotation.setTextAttributes(SQLTextAttributesKeys.ALIAS);
    }

    private static void annotateObject(@NotNull IdentifierPsiElement objectReference, AnnotationHolder holder) {
        PsiElement reference = objectReference.resolve();
        /*ConnectionHandler connectionHandler = objectReference.getCache();
        if (reference == null && connectionHandler != null && connectionHandler.getConnectionStatus().isValid()) {
            Annotation annotation = holder.createErrorAnnotation(objectReference.getAstNode(),
                    "Unknown " + objectReference.getObjectTypeName());
            annotation.setTextAttributes(PSQLTextAttributesKeys.UNKNOWN_IDENTIFIER);
        }*/
    }

    private static void annotateSpecDeclarationNavigable(@NotNull BasePsiElement basePsiElement, AnnotationHolder holder) {
        if (basePsiElement.isInjectedContext()) return;

        BasePsiElement subjectPsiElement = basePsiElement.findFirstPsiElement(ElementTypeAttribute.SUBJECT);
        if (subjectPsiElement instanceof IdentifierPsiElement) {
            IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) subjectPsiElement;
            DBObjectType objectType = identifierPsiElement.getObjectType();
            ElementType elementType = basePsiElement.elementType;

            if (identifierPsiElement.isObject() && objectType.getGenericType() == DBObjectType.METHOD) {

                DBContentType targetContentType =
                        elementType.is(ElementTypeAttribute.OBJECT_DECLARATION) ? DBContentType.CODE_SPEC :
                        elementType.is(ElementTypeAttribute.OBJECT_SPECIFICATION) ? DBContentType.CODE_BODY : null;

                if (targetContentType != null && identifierPsiElement.getFile() instanceof PSQLFile) {
                    PSQLFile file = (PSQLFile) identifierPsiElement.getFile();
                    DBSchemaObject object = (DBSchemaObject) file.getUnderlyingObject();
                    VirtualFile virtualFile = file.getVirtualFile();

                    ProjectSettings projectSettings = ProjectSettingsManager.getSettings(basePsiElement.getProject());
                    CodeEditorGeneralSettings codeEditorGeneralSettings = projectSettings.getCodeEditorSettings().getGeneralSettings();

                    if (codeEditorGeneralSettings.isShowSpecDeclarationNavigationGutter()) {
                        if (object == null || (virtualFile != null && virtualFile.isInLocalFileSystem())) {
                            ElementTypeAttribute targetAttribute =
                                    elementType.is(ElementTypeAttribute.OBJECT_DECLARATION) ? ElementTypeAttribute.OBJECT_SPECIFICATION :
                                            elementType.is(ElementTypeAttribute.OBJECT_SPECIFICATION) ? ElementTypeAttribute.OBJECT_DECLARATION : null;

                            if (targetAttribute != null) {
                                BasePsiElement rootPsiElement = identifierPsiElement.findEnclosingPsiElement(ElementTypeAttribute.ROOT);

                                BasePsiElement targetElement = rootPsiElement == null ? null :
                                        rootPsiElement.findPsiElementBySubject(targetAttribute,
                                                identifierPsiElement.getChars(),
                                                identifierPsiElement.getObjectType());

                                if (targetElement != null && targetElement.isValid()) {
                                    NavigationAction navigationAction = targetContentType == DBContentType.CODE_BODY ?
                                            new NavigateToDefinitionAction(null, targetElement, objectType) :
                                            new NavigateToSpecificationAction(null, targetElement, objectType);
                                    Annotation annotation = holder.createInfoAnnotation(basePsiElement, null);
                                    NavigationGutterRenderer gutterIconRenderer = new NavigationGutterRenderer(navigationAction, GutterIconRenderer.Alignment.RIGHT);
                                    annotation.setGutterIconRenderer(gutterIconRenderer);
                                }
                            }
                        } else if (object.getContentType() == DBContentType.CODE_SPEC_AND_BODY) {
                            SourceCodeManager codeEditorManager = SourceCodeManager.getInstance(object.getProject());


                            BasePsiElement targetElement = codeEditorManager.getObjectNavigationElement(object, targetContentType, identifierPsiElement.getObjectType(), identifierPsiElement.getChars());
                            if (targetElement != null && targetElement.isValid()) {
                                NavigationAction navigationAction = targetContentType == DBContentType.CODE_BODY ?
                                        new NavigateToDefinitionAction(object, targetElement, objectType) :
                                        new NavigateToSpecificationAction(object, targetElement, objectType);
                                Annotation annotation = holder.createInfoAnnotation(basePsiElement, null);
                                annotation.setGutterIconRenderer(new NavigationGutterRenderer(navigationAction, GutterIconRenderer.Alignment.RIGHT));
                            }
                        }
                    }

                    if (codeEditorGeneralSettings.isShowObjectsNavigationGutter()) {
                        NavigateToObjectAction navigateToObjectAction = new NavigateToObjectAction(identifierPsiElement.getUnderlyingObject(), objectType);
                        Annotation annotation = holder.createInfoAnnotation(basePsiElement, null);
                        annotation.setGutterIconRenderer(new NavigationGutterRenderer(navigateToObjectAction, GutterIconRenderer.Alignment.LEFT));
                    }
                }
            }
        }
    }

    private static void annotateExecutable(@NotNull ExecutablePsiElement executablePsiElement, AnnotationHolder holder) {
        if (executablePsiElement.isInjectedContext()) return;

        if (executablePsiElement.isValid() && !executablePsiElement.isNestedExecutable()) {
            DBLanguagePsiFile psiFile = executablePsiElement.getFile();
            VirtualFile virtualFile = psiFile.getVirtualFile();
            if (!DatabaseDebuggerManager.isDebugConsole(virtualFile)) {
                Annotation annotation = holder.createInfoAnnotation(executablePsiElement, null);
                annotation.setGutterIconRenderer(new StatementGutterRenderer(executablePsiElement));
            }
        }
    }
}
