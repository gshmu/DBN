package com.dci.intellij.dbn.language.common;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.language.common.element.ChameleonElementType;
import com.dci.intellij.dbn.language.common.element.TokenPairTemplate;
import com.dci.intellij.dbn.language.common.element.parser.TokenPairRangeMonitor;
import com.intellij.lang.Language;
import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IFileElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class DBLanguageDialect extends Language implements DBFileElementTypeProvider {
    private DBLanguageDialectIdentifier identifier;

    private Latent<DBLanguageSyntaxHighlighter> syntaxHighlighter = Latent.basic(this::createSyntaxHighlighter);
    private Latent<DBLanguageParserDefinition> parserDefinition = Latent.basic(this::createParserDefinition);
    private Latent<IFileElementType> fileElementType = Latent.basic(this::createFileElementType);

    private Set<ChameleonTokenType> chameleonTokens;
    private static Map<DBLanguageDialectIdentifier, DBLanguageDialect> register = new EnumMap<>(DBLanguageDialectIdentifier.class);

    public DBLanguageDialect(@NonNls @NotNull DBLanguageDialectIdentifier identifier, @NotNull DBLanguage baseLanguage) {
        super(baseLanguage, identifier.getValue());
        this.identifier = identifier;
        register.put(identifier, this);
    }

    protected abstract Set<ChameleonTokenType> createChameleonTokenTypes();
    protected abstract DBLanguageSyntaxHighlighter createSyntaxHighlighter() ;
    protected abstract DBLanguageParserDefinition createParserDefinition();
    protected abstract IFileElementType createFileElementType();
    public ChameleonElementType getChameleonTokenType(DBLanguageDialectIdentifier dialectIdentifier) {
        throw new IllegalArgumentException("Language " + getID() + " does not support chameleons of type " + dialectIdentifier.getValue() );
    }

    public static DBLanguageDialect getLanguageDialect(DBLanguageDialectIdentifier identifier) {
        return register.get(identifier);
    }

    public DBLanguageDialectIdentifier getIdentifier() {
        return identifier;
    }

    public boolean isReservedWord(String identifier) {
        return getParserTokenTypes().isReservedWord(identifier);
    }

    @Override
    @NotNull
    public DBLanguage getBaseLanguage() {
        return Failsafe.nn((DBLanguage) super.getBaseLanguage());
    }

    public SharedTokenTypeBundle getSharedTokenTypes() {
        return getBaseLanguage().getSharedTokenTypes();
    }

    public DBLanguageSyntaxHighlighter getSyntaxHighlighter() {
        return syntaxHighlighter.get();
    }

    @NotNull
    public DBLanguageParserDefinition getParserDefinition() {
        return parserDefinition.get();
    }

    @Override
    public IFileElementType getFileElementType() {
        return fileElementType.get();
    }

    public TokenTypeBundle getParserTokenTypes() {
        return getParserDefinition().getParser().getTokenTypes();
    }

    public TokenTypeBundle getHighlighterTokenTypes() {
        return getSyntaxHighlighter().getTokenTypes();
    }

    public TokenType getInjectedLanguageToken(DBLanguageDialectIdentifier dialectIdentifier) {
        if (chameleonTokens == null) {
            chameleonTokens = createChameleonTokenTypes();
            if (chameleonTokens == null) chameleonTokens = new HashSet<>();
        }
        for (ChameleonTokenType chameleonToken : chameleonTokens) {
            if (chameleonToken.getInjectedLanguage().identifier == dialectIdentifier) {
                return chameleonToken;
            }
        }
        return null;
    }

    public Map<TokenPairTemplate,TokenPairRangeMonitor> createTokenPairRangeMonitors(PsiBuilder builder){
        Map<TokenPairTemplate,TokenPairRangeMonitor> tokenPairRangeMonitors = new EnumMap<>(TokenPairTemplate.class);
        tokenPairRangeMonitors.put(TokenPairTemplate.PARENTHESES, new TokenPairRangeMonitor(builder, this, TokenPairTemplate.PARENTHESES));
        return tokenPairRangeMonitors;
    }
}
