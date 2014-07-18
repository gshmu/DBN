package com.dci.intellij.dbn.common.options;

import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;

public interface PersistentConfiguration {
    void readConfiguration(Element element);
    void writeConfiguration(Element element);
}