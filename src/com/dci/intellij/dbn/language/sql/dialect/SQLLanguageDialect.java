package com.dci.intellij.dbn.language.sql.dialect;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.util.LazyValue;
import com.dci.intellij.dbn.common.util.SimpleLazyValue;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.DBLanguageDialectIdentifier;
import com.dci.intellij.dbn.language.common.element.ChameleonElementType;
import com.dci.intellij.dbn.language.sql.SQLFileElementType;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.intellij.psi.tree.IFileElementType;

public abstract class SQLLanguageDialect extends DBLanguageDialect {
    private LazyValue<ChameleonElementType> psqlChameleonElementType = new SimpleLazyValue<ChameleonElementType>() {
        @Override
        protected ChameleonElementType load() {
            DBLanguageDialectIdentifier chameleonDialectIdentifier = getChameleonDialectIdentifier();
            if (chameleonDialectIdentifier != null) {
                DBLanguageDialect plsqlDialect = DBLanguageDialect.getLanguageDialect(chameleonDialectIdentifier);
                return new ChameleonElementType(plsqlDialect, SQLLanguageDialect.this);
            }

            return null;
        }
    };

    public SQLLanguageDialect(@NonNls @NotNull DBLanguageDialectIdentifier identifier) {
        super(identifier, SQLLanguage.INSTANCE);
    }

    public IFileElementType createFileElementType() {
        return new SQLFileElementType(this);
    }

    @Override
    public final ChameleonElementType getChameleonTokenType(DBLanguageDialectIdentifier dialectIdentifier) {
        if (dialectIdentifier == getChameleonDialectIdentifier()) {
            return psqlChameleonElementType.get();
        }
        return super.getChameleonTokenType(dialectIdentifier);
    }

    @Nullable
    protected DBLanguageDialectIdentifier getChameleonDialectIdentifier() {
        return null;
    }

}
