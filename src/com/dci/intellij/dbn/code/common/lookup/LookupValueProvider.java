package com.dci.intellij.dbn.code.common.lookup;

import com.dci.intellij.dbn.language.common.DBLanguage;

public interface LookupValueProvider {

    LookupItemFactory getLookupItemFactory(DBLanguage language);

}
