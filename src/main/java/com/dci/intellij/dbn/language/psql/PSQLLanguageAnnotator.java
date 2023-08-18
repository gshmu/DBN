package com.dci.intellij.dbn.language.psql;

import com.dci.intellij.dbn.code.psql.color.PSQLTextAttributesKeys;
import com.dci.intellij.dbn.code.sql.color.SQLTextAttributesKeys;
import com.dci.intellij.dbn.common.thread.ThreadMonitor;
import com.dci.intellij.dbn.common.thread.ThreadProperty;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.code.SourceCodeManager;
import com.dci.intellij.dbn.editor.code.options.CodeEditorGeneralSettings;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.TokenTypeCategory;
import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.navigation.*;
import com.dci.intellij.dbn.language.common.psi.*;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.dci.intellij.dbn.options.ProjectSettings;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.debugger.DatabaseDebuggerManager.isDebugConsole;
import static com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute.*;

public class PSQLLanguageAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder holder) {
        if (!isSupported(psiElement)) return;

        Project project = psiElement.getProject();
        ThreadMonitor.surround(project, null,
                ThreadProperty.CODE_ANNOTATING,
                () -> {
                    if (psiElement instanceof BasePsiElement) {
                        BasePsiElement basePsiElement = (BasePsiElement) psiElement;

                        ElementType elementType = basePsiElement.getElementType();
                        if (elementType.is(OBJECT_SPECIFICATION) || elementType.is(OBJECT_DECLARATION)) {
                            annotateSpecDeclarationNavigable(basePsiElement, holder);
                        }

                        if (basePsiElement instanceof TokenPsiElement) {
                            annotateToken((TokenPsiElement) basePsiElement, holder);
                        } else if (basePsiElement instanceof IdentifierPsiElement) {
                            if (!basePsiElement.isInjectedContext()) {
                                IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) basePsiElement;
                                ConnectionHandler connection = identifierPsiElement.getConnection();
                                if (connection != null) {
                                    annotateIdentifier(identifierPsiElement, holder);
                                }
                            }
                        } else if (basePsiElement instanceof NamedPsiElement) {
                            NamedPsiElement namedPsiElement = (NamedPsiElement) basePsiElement;
                            if (namedPsiElement.hasErrors()) {
                                String message = "Invalid " + namedPsiElement.getElementType().getDescription();
                                holder.newAnnotation(HighlightSeverity.ERROR, message)
                                        .range(basePsiElement)
                                        .needsUpdateOnTyping(true)
                                        .create();
                            }
                        }

                        if (basePsiElement instanceof ExecutablePsiElement) {
                            ExecutablePsiElement executablePsiElement = (ExecutablePsiElement) basePsiElement;
                            annotateExecutable(executablePsiElement, holder);
                        }
                    } else if (psiElement instanceof ChameleonPsiElement) {
                        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                                .textAttributes(SQLTextAttributesKeys.CHAMELEON)
                                .range(psiElement)
                                .create();
                    }
                });
    }

    private boolean isSupported(PsiElement psiElement) {
        return psiElement instanceof ChameleonPsiElement ||
                psiElement instanceof TokenPsiElement ||
                psiElement instanceof IdentifierPsiElement ||
                psiElement instanceof NamedPsiElement;
    }

    private static void annotateToken(@NotNull TokenPsiElement tokenPsiElement, AnnotationHolder holder) {
        TokenTypeCategory flavor = tokenPsiElement.getElementType().getFlavor();
        if (flavor != null) {
            AnnotationBuilder builder = holder.newSilentAnnotation(HighlightSeverity.INFORMATION);
            switch (flavor) {
                case DATATYPE:   builder = builder.textAttributes(SQLTextAttributesKeys.DATA_TYPE); break;
                case FUNCTION:   builder = builder.textAttributes(SQLTextAttributesKeys.FUNCTION); break;
                case KEYWORD:    builder = builder.textAttributes(SQLTextAttributesKeys.KEYWORD); break;
                case IDENTIFIER: builder = builder.textAttributes(SQLTextAttributesKeys.IDENTIFIER); break;
            }
            builder.range(tokenPsiElement).create();
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

        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .textAttributes(PSQLTextAttributesKeys.ALIAS)
                .create();
    }

    private static void annotateAliasDef(IdentifierPsiElement aliasDefinition, @NotNull AnnotationHolder holder) {
        /*Set<PsiElement> aliasDefinitions = new HashSet<PsiElement>();
        SequencePsiElement sourceScope = aliasDefinition.getEnclosingScopePsiElement();
        sourceScope.collectAliasDefinitionPsiElements(aliasDefinitions, aliasDefinition.getUnquotedText(), DBObjectType.ANY, null);
        if (aliasDefinitions.size() > 1) {
            holder.createWarningAnnotation(aliasDefinition, "Duplicate alias definition: " + aliasDefinition.getUnquotedText());
        }*/

        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .textAttributes(SQLTextAttributesKeys.ALIAS)
                .create();
    }

    private static void annotateObject(@NotNull IdentifierPsiElement objectReference, AnnotationHolder holder) {
        PsiElement reference = objectReference.resolve();
        /*ConnectionHandler connection = objectReference.getCache();
        if (reference == null && connection != null && connection.getConnectionStatus().isValid()) {
            Annotation annotation = holder.createErrorAnnotation(objectReference.getAstNode(),
                    "Unknown " + objectReference.getObjectTypeName());
            annotation.setTextAttributes(PSQLTextAttributesKeys.UNKNOWN_IDENTIFIER);
        }*/
    }

    private static void annotateSpecDeclarationNavigable(@NotNull BasePsiElement basePsiElement, AnnotationHolder holder) {
        if (basePsiElement.isInjectedContext()) return;

        BasePsiElement subjectPsiElement = basePsiElement.findFirstPsiElement(SUBJECT);
        if (subjectPsiElement instanceof IdentifierPsiElement) {
            IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) subjectPsiElement;
            DBObjectType objectType = identifierPsiElement.getObjectType();
            ElementType elementType = basePsiElement.getElementType();

            if (identifierPsiElement.isObject() && objectType.getGenericType() == DBObjectType.METHOD) {

                DBContentType targetContentType =
                        elementType.is(OBJECT_DECLARATION) ? DBContentType.CODE_SPEC :
                        elementType.is(OBJECT_SPECIFICATION) ? DBContentType.CODE_BODY : null;

                if (targetContentType != null && identifierPsiElement.getFile() instanceof PSQLFile) {
                    PSQLFile file = (PSQLFile) identifierPsiElement.getFile();
                    DBSchemaObject object = (DBSchemaObject) file.getUnderlyingObject();
                    VirtualFile virtualFile = file.getVirtualFile();

                    ProjectSettings projectSettings = ProjectSettingsManager.getSettings(basePsiElement.getProject());
                    CodeEditorGeneralSettings codeEditorGeneralSettings = projectSettings.getCodeEditorSettings().getGeneralSettings();

                    if (codeEditorGeneralSettings.isShowSpecDeclarationNavigationGutter()) {
                        if (object == null || (virtualFile != null && virtualFile.isInLocalFileSystem())) {
                            ElementTypeAttribute targetAttribute =
                                    elementType.is(OBJECT_DECLARATION) ? OBJECT_SPECIFICATION :
                                    elementType.is(OBJECT_SPECIFICATION) ? OBJECT_DECLARATION : null;

                            if (targetAttribute != null) {
                                BasePsiElement rootPsiElement = identifierPsiElement.findEnclosingElement(ROOT);

                                BasePsiElement targetElement = rootPsiElement == null ? null :
                                        rootPsiElement.findPsiElementBySubject(targetAttribute,
                                                identifierPsiElement.getChars(),
                                                identifierPsiElement.getObjectType());

                                if (targetElement != null && targetElement.isValid()) {
                                    NavigationAction navigationAction = targetContentType == DBContentType.CODE_BODY ?
                                            new NavigateToDefinitionAction(null, targetElement, objectType) :
                                            new NavigateToSpecificationAction(null, targetElement, objectType);
                                    NavigationGutterRenderer gutterIconRenderer = new NavigationGutterRenderer(navigationAction, GutterIconRenderer.Alignment.RIGHT);

                                    holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                                            .gutterIconRenderer(gutterIconRenderer)
                                            .create();
                                }
                            }
                        } else if (object.getContentType() == DBContentType.CODE_SPEC_AND_BODY) {
                            SourceCodeManager codeEditorManager = SourceCodeManager.getInstance(object.getProject());


                            BasePsiElement targetElement = codeEditorManager.getObjectNavigationElement(object, targetContentType, identifierPsiElement.getObjectType(), identifierPsiElement.getChars());
                            if (targetElement != null && targetElement.isValid()) {
                                NavigationAction navigationAction = targetContentType == DBContentType.CODE_BODY ?
                                        new NavigateToDefinitionAction(object, targetElement, objectType) :
                                        new NavigateToSpecificationAction(object, targetElement, objectType);
                                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                                        .gutterIconRenderer(new NavigationGutterRenderer(navigationAction, GutterIconRenderer.Alignment.RIGHT))
                                        .create();
                            }
                        }
                    }

                    if (codeEditorGeneralSettings.isShowObjectsNavigationGutter()) {
                        NavigateToObjectAction navigateToObjectAction = new NavigateToObjectAction(identifierPsiElement.getUnderlyingObject(), objectType);
                        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                                .gutterIconRenderer(new NavigationGutterRenderer(navigateToObjectAction, GutterIconRenderer.Alignment.LEFT))
                                .create();
                    }
                }
            }
        }
    }

    private static void annotateExecutable(@NotNull ExecutablePsiElement executablePsiElement, AnnotationHolder holder) {
        if (executablePsiElement.isInjectedContext()) return;
        if (executablePsiElement.isNestedExecutable()) return;
        if (!executablePsiElement.isValid()) return;

        DBLanguagePsiFile psiFile = executablePsiElement.getFile();
        VirtualFile file = psiFile.getVirtualFile();
        if (file instanceof LightVirtualFile) return;
        if (isDebugConsole(file)) return;

        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .gutterIconRenderer(executablePsiElement.getStatementGutterRenderer())
                .create();
    }
}
