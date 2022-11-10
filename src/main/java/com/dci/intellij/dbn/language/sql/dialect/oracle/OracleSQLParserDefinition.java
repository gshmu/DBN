package com.dci.intellij.dbn.language.sql.dialect.oracle;

import com.dci.intellij.dbn.language.sql.SQLParserDefinition;
import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;


public class OracleSQLParserDefinition extends SQLParserDefinition {

    OracleSQLParserDefinition(OracleSQLParser parser) {
        super(parser);
    }

    @Override
    @NotNull
    public Lexer createLexer(Project project) {
        return new FlexAdapter(new OracleSQLParserFlexLexer(getTokenTypes()));
    }
}