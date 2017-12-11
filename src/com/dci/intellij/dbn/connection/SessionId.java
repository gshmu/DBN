package com.dci.intellij.dbn.connection;

import java.util.UUID;

import com.dci.intellij.dbn.common.constant.PseudoConstant;

public final class SessionId extends PseudoConstant<SessionId> {

    public SessionId(String id) {
        super(id);
    }

    public static SessionId get(String id) {
        return get(SessionId.class, id);
    }

    public static SessionId create() {
        return SessionId.get(UUID.randomUUID().toString());
    }
}
