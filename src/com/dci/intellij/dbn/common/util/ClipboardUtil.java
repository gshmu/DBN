package com.dci.intellij.dbn.common.util;

import com.intellij.openapi.ide.CopyPasteManager;
import org.jetbrains.annotations.Nullable;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Objects;

public class ClipboardUtil {

    public static XmlContent createXmlContent(String text) {
        return new XmlContent(text);
    }

    public static class XmlContent implements Transferable {
        private DataFlavor[] dataFlavors;
        private final String content;

        public XmlContent(String text) {
            content = text;
            try {
                dataFlavors = new DataFlavor[4];
                dataFlavors[0] = new DataFlavor("text/xml;class=java.lang.String");
                dataFlavors[1] = new DataFlavor("text/rtf;class=java.lang.String");
                dataFlavors[2] = new DataFlavor("text/plain;class=java.lang.String");
                dataFlavors[3] = DataFlavor.stringFlavor;

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return dataFlavors;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            String mimeType = flavor.getMimeType();
            return
                    Objects.equals(mimeType, "text/xml") ||
                    Objects.equals(mimeType, "text/rtf") ||
                    Objects.equals(mimeType, "text/plain");
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            return content;
        }
    }

    @Nullable
    public static String getStringContent() {
        try {
            CopyPasteManager copyPasteManager = CopyPasteManager.getInstance();
            Object data = copyPasteManager.getContents(DataFlavor.stringFlavor);;
            if (data instanceof String) {
                return (String) data;
            } else {
                return null;
            }

        } catch (Exception e) {
            return null;
        }
    }
}
