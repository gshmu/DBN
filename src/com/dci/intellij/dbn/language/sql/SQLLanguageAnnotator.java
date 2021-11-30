package com.dci.intellij.dbn.language.sql;

import com.dci.intellij.dbn.code.sql.color.SQLTextAttributesKeys;
import com.dci.intellij.dbn.common.thread.ThreadMonitor;
import com.dci.intellij.dbn.common.thread.ThreadProperty;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerStatus;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.execution.statement.StatementGutterRenderer;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.TokenTypeCategory;
import com.dci.intellij.dbn.language.common.psi.ChameleonPsiElement;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.language.common.psi.NamedPsiElement;
import com.dci.intellij.dbn.language.common.psi.TokenPsiElement;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class SQLLanguageAnnotator implements Annotator {
    public static final SQLLanguageAnnotator INSTANCE = new SQLLanguageAnnotator();

    @Override
    public void annotate(@NotNull final PsiElement psiElement, @NotNull final AnnotationHolder holder) {
        ThreadMonitor.run(null,
                ThreadProperty.CODE_ANNOTATING,
                () -> {
                    if (psiElement instanceof ExecutablePsiElement) {
                        annotateExecutable((ExecutablePsiElement) psiElement, holder);

                    } else if (psiElement instanceof ChameleonPsiElement) {
                        annotateChameleon(psiElement, holder);

                    } else if (psiElement instanceof TokenPsiElement) {
                        annotateToken((TokenPsiElement) psiElement, holder);

                    } else if (psiElement instanceof IdentifierPsiElement) {
                        IdentifierPsiElement identifierPsiElement = (IdentifierPsiElement) psiElement;
                        ConnectionHandler connectionHandler = identifierPsiElement.getConnectionHandler();
                        if (connectionHandler != null && !connectionHandler.isVirtual()) {
                            annotateIdentifier(identifierPsiElement, holder);
                        }
                    }


                    if (psiElement instanceof NamedPsiElement) {
                        NamedPsiElement namedPsiElement = (NamedPsiElement) psiElement;
                        if (namedPsiElement.hasErrors()) {
                            String message = "Invalid " + namedPsiElement.getElementType().getDescription();
                            holder.newAnnotation(HighlightSeverity.ERROR, message).create();
                        }
                    }
                });
    }

    private static void annotateToken(@NotNull TokenPsiElement tokenPsiElement, AnnotationHolder holder) {
        TokenTypeCategory flavor = tokenPsiElement.getElementType().getFlavor();
        if (flavor != null) {
            AnnotationBuilder builder = holder.newSilentAnnotation(HighlightSeverity.INFORMATION);
            switch (flavor) {
                case DATATYPE: builder = builder.textAttributes(SQLTextAttributesKeys.DATA_TYPE); break;
                case FUNCTION: builder = builder.textAttributes(SQLTextAttributesKeys.FUNCTION); break;
                case KEYWORD: builder = builder.textAttributes(SQLTextAttributesKeys.KEYWORD); break;
                case IDENTIFIER: builder = builder.textAttributes(SQLTextAttributesKeys.IDENTIFIER); break;
            }
            builder.create();
        }
    }

    private void annotateIdentifier(@NotNull IdentifierPsiElement identifierPsiElement, final AnnotationHolder holder) {
        if (identifierPsiElement.getLanguageDialect().isReservedWord(identifierPsiElement.getText())) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .textAttributes(SQLTextAttributesKeys.IDENTIFIER)
                    .create();
        }
        if (identifierPsiElement.isObject()) {
            annotateObject(identifierPsiElement, holder);
        } else if (identifierPsiElement.isAlias()) {
            if (identifierPsiElement.isReference())
                annotateAliasRef(identifierPsiElement, holder); else
                annotateAliasDef(identifierPsiElement, holder);
        }
    }

    private static void annotateAliasRef(@NotNull IdentifierPsiElement aliasReference, AnnotationHolder holder) {
        if (aliasReference.resolve() == null &&  aliasReference.getResolveAttempts() > 3) {
            holder.newAnnotation(HighlightSeverity.WARNING, "Unknown identifier")
                    .textAttributes(SQLTextAttributesKeys.UNKNOWN_IDENTIFIER)
                    .create();
        } else {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .textAttributes(SQLTextAttributesKeys.ALIAS)
                    .create();
        }
    }

    private void annotateAliasDef(IdentifierPsiElement aliasDefinition, @NotNull AnnotationHolder holder) {
        /*Set<BasePsiElement> aliasDefinitions = new HashSet<BasePsiElement>();
        BasePsiElement scope = aliasDefinition.getEnclosingScopePsiElement();
        scope.collectAliasDefinitionPsiElements(aliasDefinitions, aliasDefinition.getUnquotedText(), DBObjectType.ANY);
        if (aliasDefinitions.size() > 1) {
            holder.createWarningAnnotation(aliasDefinition, "Duplicate alias definition: " + aliasDefinition.getUnquotedText());
        }*/

        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .textAttributes(SQLTextAttributesKeys.ALIAS)
                .create();
    }

    private static void annotateObject(@NotNull IdentifierPsiElement objectReference, AnnotationHolder holder) {
        if (!objectReference.isResolving() && !objectReference.isDefinition()) {
            PsiElement reference = objectReference.resolve();
            if (reference == null && objectReference.getResolveAttempts() > 3 && checkConnection(objectReference)) {
                if (!objectReference.getLanguageDialect().getParserTokenTypes().isFunction(objectReference.getText())) {
                    holder.newAnnotation(HighlightSeverity.WARNING, "Unknown identifier")
                            .textAttributes(SQLTextAttributesKeys.UNKNOWN_IDENTIFIER)
                            .create();
                }
            }
        }
    }

    private static boolean checkConnection(@NotNull IdentifierPsiElement objectReference) {
        ConnectionHandler connectionHandler = objectReference.getConnectionHandler();
        return connectionHandler != null &&
                !connectionHandler.isVirtual() &&
                connectionHandler.canConnect() &&
                connectionHandler.isValid() &&
                !connectionHandler.getConnectionStatus().is(ConnectionHandlerStatus.LOADING);
    }

    private static void annotateExecutable(@NotNull ExecutablePsiElement executablePsiElement, AnnotationHolder holder) {
        if (executablePsiElement.isInjectedContext()) return;

        if (executablePsiElement.isValid() && !executablePsiElement.isNestedExecutable()) {
            DBLanguagePsiFile psiFile = executablePsiElement.getFile();
            VirtualFile virtualFile = psiFile.getVirtualFile();
            if (!DatabaseDebuggerManager.isDebugConsole(virtualFile)) {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                        .gutterIconRenderer(new StatementGutterRenderer(executablePsiElement))
                        .create();
            }
        }
    }

    private static void annotateChameleon(PsiElement psiElement, AnnotationHolder holder) {
        ChameleonPsiElement executable = (ChameleonPsiElement) psiElement;
/*
        if (!executable.isNestedExecutable()) {
            StatementExecutionProcessor executionProcessor = executable.getExecutionProcessor();
            if (executionProcessor != null) {
                Annotation annotation = holder.createInfoAnnotation(psiElement, null);
                annotation.setGutterIconRenderer(new StatementGutterRenderer(executionProcessor));
            }
        }
*/
    }
}
