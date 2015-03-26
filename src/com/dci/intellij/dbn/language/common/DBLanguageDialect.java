package com.dci.intellij.dbn.language.common;

import com.dci.intellij.dbn.common.util.LazyValue;
import com.dci.intellij.dbn.language.common.element.ChameleonElementType;
import com.dci.intellij.dbn.language.common.element.TokenPairTemplate;
import com.dci.intellij.dbn.language.common.element.parser.TokenPairRangeMonitor;
import com.intellij.lang.LanguageDialect;
import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IFileElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class DBLanguageDialect extends LanguageDialect implements DBFileElementTypeProvider {
    private DBLanguageDialectIdentifier identifier;
    private LazyValue<DBLanguageSyntaxHighlighter> syntaxHighlighter = new LazyValue<DBLanguageSyntaxHighlighter>() {
        @Override
        protected DBLanguageSyntaxHighlighter load() {
            return createSyntaxHighlighter();
        }
    };

    private LazyValue<DBLanguageParserDefinition> parserDefinition = new LazyValue<DBLanguageParserDefinition>() {
        @Override
        protected DBLanguageParserDefinition load() {
            return createParserDefinition();
        }
    };

    private LazyValue<IFileElementType> fileElementType = new LazyValue<IFileElementType>() {
        @Override
        protected IFileElementType load() {
            return createFileElementType();
        }
    };
    private ChameleonElementType chameleonElementType;
    private Set<ChameleonTokenType> chameleonTokens;
    private static Map<DBLanguageDialectIdentifier, DBLanguageDialect> register = new EnumMap<DBLanguageDialectIdentifier, DBLanguageDialect>(DBLanguageDialectIdentifier.class);

    public DBLanguageDialect(@NonNls @NotNull DBLanguageDialectIdentifier identifier, @NotNull DBLanguage baseLanguage) {
        super(identifier.getValue(), baseLanguage);
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

    @NotNull
    public DBLanguage getBaseLanguage() {
        return (DBLanguage) super.getBaseLanguage();
    }

    public SharedTokenTypeBundle getSharedTokenTypes() {
        return getBaseLanguage().getSharedTokenTypes();
    }

    public DBLanguageSyntaxHighlighter getSyntaxHighlighter() {
        return syntaxHighlighter.get();
    }

    @NotNull
    public synchronized DBLanguageParserDefinition getParserDefinition() {
        return parserDefinition.get();
    }

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
            if (chameleonTokens == null) chameleonTokens = new HashSet<ChameleonTokenType>();
        }
        for (ChameleonTokenType chameleonToken : chameleonTokens) {
            if (chameleonToken.getInjectedLanguage().identifier == dialectIdentifier) {
                return chameleonToken;
            }
        }
        return null;
    }

    public synchronized ChameleonElementType getChameleonElementType(DBLanguageDialect parentLanguage) {
        if (chameleonElementType == null) {
            synchronized (this) {
                if (chameleonElementType == null) {
                    chameleonElementType = new ChameleonElementType(this, parentLanguage);
                }
            }
        }
        return chameleonElementType;
    }

    public Map<TokenPairTemplate,TokenPairRangeMonitor> createTokenPairRangeMonitors(PsiBuilder builder){
        Map<TokenPairTemplate,TokenPairRangeMonitor> tokenPairRangeMonitors = new EnumMap<TokenPairTemplate, TokenPairRangeMonitor>(TokenPairTemplate.class);
        tokenPairRangeMonitors.put(TokenPairTemplate.PARENTHESES, new TokenPairRangeMonitor(builder, this, TokenPairTemplate.PARENTHESES));
        return tokenPairRangeMonitors;
    }
}
