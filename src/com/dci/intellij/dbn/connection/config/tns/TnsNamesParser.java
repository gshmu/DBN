package com.dci.intellij.dbn.connection.config.tns;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

public class TnsNamesParser {
    public static final FileChooserDescriptor FILE_CHOOSER_DESCRIPTOR = new FileChooserDescriptor(true, false, false, false, false, false).
            withTitle("Select TNS Names File").
            withDescription("Select a valid Oracle tnsnames.ora file").
            withFileFilter(new Condition<VirtualFile>() {
                @Override
                public boolean value(VirtualFile virtualFile) {
                    String extension = virtualFile.getExtension();
                    return extension != null && extension.equals("ora");
                }
            });

    public static TnsName[] parse(File file) throws Exception {
        // Begin by treating the file as separate lines to throw out the comments
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        StringBuilder tnsText = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (!line.startsWith("#") && !line.equals("")) {
                tnsText.append(line);
            }
        }

        // Now switch to a streaming parser to get the actual data
        Map tnsNamesMap = new HashMap();

        // used to ascertain whether we are awaiting the RHS of an =
        boolean parsingValue = false;
        // used to indicate that we have finished a block and should either start
        // a new sibling block, or start a new tns block
        boolean endBlock = false;
        StringBuilder currentTnsKey = new StringBuilder();
        StringBuilder currentTnsValue = new StringBuilder();
        Map currentMap = tnsNamesMap;
        char[] tnsChars = tnsText.toString().toCharArray();
        Stack<Map> mapStack = new Stack<Map>();
        for (char ch : tnsChars) {
            switch (ch) {
                case ' ': {
                    break;
                }
                case '=': {
                    parsingValue = true;
                    break;
                }
                case '(': {
                    if (endBlock) {
                        endBlock = false;
                    }
                    if (parsingValue) {
                        Map newMap = new HashMap();
                        currentMap.put(currentTnsKey.toString().toUpperCase(), newMap);
                        currentTnsKey.setLength(0);
                        mapStack.push(currentMap);
                        currentMap = newMap;
                        parsingValue = false;
                    }
                    break;
                }
                case ')': {
                    if (parsingValue) {
                        currentMap.put(currentTnsKey.toString().toUpperCase(), currentTnsValue.toString());
                        currentTnsKey.setLength(0);
                        currentTnsValue.setLength(0);
                        parsingValue = false;
                        endBlock = true;
                    } else {
                        currentMap = mapStack.pop();
                    }
                    break;
                }
                default: {
                    if (parsingValue) {
                        currentTnsValue.append(ch);
                    } else {
                        if (endBlock) {
                            currentMap = mapStack.pop();
                            endBlock = false;
                        }
                        currentTnsKey.append(ch);
                    }
                    break;
                }
            }
        }

        TnsName[] tnsNames = new TnsName[tnsNamesMap.size()];

        Iterator iterator = tnsNamesMap.keySet().iterator();
        int i = 0;
        while (iterator.hasNext()) {
            String name = (String) iterator.next();
            Map details = (Map) tnsNamesMap.get(name);
            tnsNames[i] = TnsName.createTnsName(name, details);
            i++;
        }

        return tnsNames;
    }
}
