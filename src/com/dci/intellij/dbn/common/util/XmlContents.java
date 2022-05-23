package com.dci.intellij.dbn.common.util;

import com.intellij.openapi.util.JDOMUtil;
import lombok.extern.slf4j.Slf4j;
import org.jdom.Element;

import java.io.InputStream;

@Slf4j
public final class XmlContents {
    private XmlContents() {}

    public static Element loadXmlContent(Class clazz, String fileName) throws Exception {
        try (InputStream inputStream = clazz.getResourceAsStream(fileName)){
            return loadXmlContent(inputStream);
        }
    }

    public static Element loadXmlContent(InputStream inputStream) throws Exception{
        //return new DOMBuilder().build(new XML4JDOMAdapter().getDocument(inputStream, false)).getRootElement();
        return JDOMUtil.load(inputStream);
    }

}
