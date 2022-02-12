package com.dci.intellij.dbn.language.common;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.intellij.lang.*;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.tree.IFileElementType;

public class DBLanguageFileElementType extends IFileElementType {
    public DBLanguageFileElementType(Language language) {
        super("FILE_ELEMENT_TYPE", language, false);
    }

    @Override
    public ASTNode parseContents(ASTNode chameleon) {
        DBLanguagePsiFile file = (DBLanguagePsiFile) chameleon.getPsi();
        Project project = file.getProject();
        DBLanguageDialect languageDialect = file.getLanguageDialect();
        if (languageDialect == null) {
            return super.parseContents(chameleon);
        }

        /*DBLanguageFile originalFile = (DBLanguageFile) file.getViewProvider().getAllFiles().get(0).getOriginalFile();
        if (originalFile != null)  file = originalFile;*/

        String text = chameleon.getText();
        ParserDefinition parserDefinition = languageDialect.getParserDefinition();
        Lexer lexer = parserDefinition.createLexer(project);

        DBLanguageParser parser = (DBLanguageParser) parserDefinition.createParser(project);
        double databaseVersion = 9999;
        ConnectionHandler activeConnection = file.getConnection();
        if (activeConnection != null) {
            databaseVersion = activeConnection.getDatabaseVersion();
        }

        PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, lexer, languageDialect, text);
        ASTNode node = parser.parse(this, builder, file.getParseRootId(), databaseVersion);
        return node.getFirstChildNode();
    }
}
