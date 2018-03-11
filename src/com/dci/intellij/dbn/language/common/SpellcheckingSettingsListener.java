package com.dci.intellij.dbn.language.common;

import com.intellij.util.messages.Topic;

import java.util.EventListener;

public interface SpellcheckingSettingsListener extends EventListener {
    Topic<SpellcheckingSettingsListener> TOPIC = Topic.create("spellchecking settings change event", SpellcheckingSettingsListener.class);
    void settingsChanged();
}
