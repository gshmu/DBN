package com.dci.intellij.dbn.language.common.psi.lookup;

import com.dci.intellij.dbn.object.type.DBObjectType;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

public class LookupAdapterCache {
    public static Cache<DBObjectType, ObjectDefinitionLookupAdapter> OBJECT_DEFINITION = new Cache<>(DBObjectType.class, key ->
            new ObjectDefinitionLookupAdapter(null, key, null));

    public static Cache<DBObjectType, AliasDefinitionLookupAdapter> ALIAS_DEFINITION = new Cache<>(DBObjectType.class, key ->
            new AliasDefinitionLookupAdapter(null, key));

    public static Cache<DBObjectType, VariableDefinitionLookupAdapter> VARIABLE_DEFINITION = new Cache<>(DBObjectType.class, key ->
            new VariableDefinitionLookupAdapter(null, key, null));


    public static class Cache <K extends Enum<K>, V extends IdentifierLookupAdapter> {
        private final Map<K, V> cache;
        private final Function<K, V> provider;

        public Cache(Class<K> keyClass, Function<K, V> provider) {
            cache = new EnumMap<>(keyClass);
            this.provider = provider;
        }

        public V get(K key) {
            return cache.computeIfAbsent(key, provider);
        }
    }
}
