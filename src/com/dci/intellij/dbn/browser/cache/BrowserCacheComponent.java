package com.dci.intellij.dbn.browser.cache;

import com.dci.intellij.dbn.connection.ConnectionBundle;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.adapters.XML4JDOMAdapter;
import org.jdom.input.DOMBuilder;
import org.jdom.output.XMLOutputter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class BrowserCacheComponent {
    public static final String FILE_EXTENSION = ".dbi";
    private final File file;
    private final ConnectionBundle connectionBundle;

    public BrowserCacheComponent(File file, ConnectionBundle connectionBundle) {
        this.file = file;
        this.connectionBundle = connectionBundle;
    }

    public void read() {
        try {
            if (file.exists()) {

                InputStream inputStream = new FileInputStream(file);
                Document document = new DOMBuilder().build(new XML4JDOMAdapter().getDocument(inputStream, false));
                Element root = document.getRootElement();
                Element fileConnectionMappings = root.getChild("file-connection-mapings");

                //connectionBundle.jdomRead(root);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void write() {
        try {
            OutputStream outputStream = new FileOutputStream(file);
            Element root = new Element("Connections");
            //connectionBundle.jdomWrite(root);
            new XMLOutputter().output(root, outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
