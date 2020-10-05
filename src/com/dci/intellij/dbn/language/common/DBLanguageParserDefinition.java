package com.dci.intellij.dbn.language.common;

import com.dci.intellij.dbn.language.common.element.ElementType;
import com.dci.intellij.dbn.vfs.DatabaseFileViewProvider;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

public abstract class DBLanguageParserDefinition implements ParserDefinition {
    private final DBLanguageParser parser;

    public DBLanguageParserDefinition(DBLanguageParser parser) {
        this.parser = parser;
    }

    public DBLanguageParser getParser() {
        return parser;
    }

    @Override
    @NotNull
    public PsiElement createElement(ASTNode astNode) {
        IElementType et = astNode.getElementType();
        if(et instanceof ElementType) {
            ElementType elementType = (ElementType) et;
            //SQLFile file = lookupFile(astNode);
            return elementType.createPsiElement(astNode);
        }
        return new ASTWrapperPsiElement(astNode);
    }

    @Override
    public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return null;
    }

    @Override
    public IFileElementType getFileNodeType() {
        return parser.getLanguageDialect().getBaseLanguage().getFileElementType();
        /*DBLanguageDialect languageDialect = parser.getLanguageDialect();
        return languageDialect.getFileElementType();*/
    }

    @Override
    @NotNull
    public TokenSet getWhitespaceTokens() {
        return parser.getTokenTypes().getSharedTokenTypes().getWhitespaceTokens();
    }

    @Override
    @NotNull
    public TokenSet getCommentTokens() {
        return parser.getTokenTypes().getSharedTokenTypes().getCommentTokens();
    }

    @Override
    @NotNull
    public TokenSet getStringLiteralElements() {
        return parser.getTokenTypes().getSharedTokenTypes().getStringTokens();
    }

    @Override
    public final PsiFile createFile(FileViewProvider viewProvider) {
        if (viewProvider instanceof DatabaseFileViewProvider) {
            // ensure the document is initialized
            // TODO cleanup - causes SOE (may not be required any more)
            //FileDocumentManager.getInstance().getDocument(viewProvider.getVirtualFile());
        }
        return createPsiFile(viewProvider);
    }

    protected abstract PsiFile createPsiFile(FileViewProvider viewProvider);
}
