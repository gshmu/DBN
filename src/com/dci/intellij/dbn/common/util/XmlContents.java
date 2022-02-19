package com.dci.intellij.dbn.common.util;

import org.jdom.Document;
import org.jdom.adapters.XML4JDOMAdapter;
import org.jdom.input.DOMBuilder;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;

public final class XmlContents {
    private XmlContents() {}

    public static Document loadXmlFile(Class clazz, String fileName) throws Exception {
        try (InputStream inputStream = clazz.getResourceAsStream(fileName)){
            return createXmlDocument(inputStream);
        }
    }

    @Nullable
    public static Document createXmlDocument(InputStream inputStream) throws Exception {
        return new DOMBuilder().build(new XML4JDOMAdapter().getDocument(inputStream, false));
    }
}
