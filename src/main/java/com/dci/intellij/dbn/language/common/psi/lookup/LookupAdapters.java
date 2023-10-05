package com.dci.intellij.dbn.language.common.psi.lookup;

import com.dci.intellij.dbn.object.type.DBObjectType;
import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class LookupAdapters {
    private static final Map<DBObjectType, SimpleObjectLookupAdapter> OBJECT = new ConcurrentHashMap<>();
    private static final Map<DBObjectType, SimpleAliasLookupAdapter> ALIAS = new ConcurrentHashMap<>();
    private static final Map<DBObjectType, ObjectDefinitionLookupAdapter> OBJECT_DEFINITION = new ConcurrentHashMap<>();
    private static final Map<DBObjectType, AliasDefinitionLookupAdapter> ALIAS_DEFINITION = new ConcurrentHashMap<>();
    private static final Map<DBObjectType, VariableDefinitionLookupAdapter> VARIABLE_DEFINITION = new ConcurrentHashMap<>();
    private static final Map<DBObjectType, Map<DBObjectType, VirtualObjectLookupAdapter>> VIRTUAL_OBJECT = new ConcurrentHashMap<>();

    public static PsiLookupAdapter object(DBObjectType type)  {
        return OBJECT.computeIfAbsent(type, t -> new SimpleObjectLookupAdapter(null, t));
    }

    public static PsiLookupAdapter alias(DBObjectType type)  {
        return ALIAS.computeIfAbsent(type, t -> new SimpleAliasLookupAdapter(null, t));
    }

    public static PsiLookupAdapter virtualObject(DBObjectType parent, DBObjectType child)  {
        return VIRTUAL_OBJECT.computeIfAbsent(parent, p -> new ConcurrentHashMap<>()).computeIfAbsent(child, c -> new VirtualObjectLookupAdapter(parent, child));
    }

    public static PsiLookupAdapter aliasDefinition(DBObjectType objectType) {
        return ALIAS_DEFINITION.computeIfAbsent(objectType, t -> new AliasDefinitionLookupAdapter(null, t));
    }

    public static PsiLookupAdapter objectDefinition(DBObjectType objectType) {
        return OBJECT_DEFINITION.computeIfAbsent(objectType, t -> new ObjectDefinitionLookupAdapter(null, t, null));
    }

    public static PsiLookupAdapter variableDefinition(DBObjectType objectType) {
        return VARIABLE_DEFINITION.computeIfAbsent(objectType, t -> new VariableDefinitionLookupAdapter(null, t, null));
    }
}
