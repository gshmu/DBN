package com.dci.intellij.dbn.connection.session;

import com.intellij.util.messages.Topic;

import java.util.EventListener;

public interface SessionManagerListener extends EventListener{
    Topic<SessionManagerListener> TOPIC = Topic.create("Session manager event", SessionManagerListener.class);

    void sessionCreated(DatabaseSession session);

    void sessionDeleted(DatabaseSession session);

    void sessionChanged(DatabaseSession session);
}
