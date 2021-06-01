package com.dci.intellij.dbn.generator;

import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.object.common.DBObject;

import java.util.HashMap;
import java.util.Map;

public class AliasBundle {
    private final Map<DBObject, String> aliases = new HashMap<>();

    public String getAlias(DBObject object) {
        String alias = aliases.get(object);
        if (alias == null) {
            alias = NamingUtil.createAliasName(object.getName());
            alias = getNextAvailable(alias);
            aliases.put(object, alias);
        }
        return alias;
    }

    private String getNextAvailable(String alias) {
        for (String availableAlias : aliases.values()) {
            if (alias.equals(availableAlias)) {
                alias = NamingUtil.getNextNumberedName(alias, false);
            }
        }
        return alias;
    }

}
