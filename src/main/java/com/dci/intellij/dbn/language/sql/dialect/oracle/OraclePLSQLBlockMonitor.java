package com.dci.intellij.dbn.language.sql.dialect.oracle;

import com.dci.intellij.dbn.language.common.lexer.DBLanguageFlexLexer;

import java.util.Deque;
import java.util.LinkedList;

public abstract class OraclePLSQLBlockMonitor {
    public enum Marker {
        CASE,
        BEGIN,
        CREATE,
        DECLARE,
        PROGRAM}

    private final DBLanguageFlexLexer lexer;
    private final Deque<Marker> stack = new LinkedList<>();

    public OraclePLSQLBlockMonitor(DBLanguageFlexLexer lexer) {
        this.lexer = lexer;
    }

    public void ignore() {
        //System.out.println("ignore:    " + lexer.getCurrentToken());
    }


    public void start(Marker marker) {
        //System.out.println("start:     " + lexer.getCurrentToken());
        if (!stack.isEmpty()) {
            stack.clear();
        }
        stack.push(marker);
        lexerStart();
    }

    public void mark(Marker marker) {
        //System.out.println("mark:      " + lexer.getCurrentToken());
        stack.push(marker);
    }

    public boolean end(boolean force) {
        if (force) {
            //System.out.println("end force: " + lexer.getCurrentToken());
            stack.clear();
        } else {
            //System.out.println("end:       " + lexer.getCurrentToken());
            Marker marker = stack.poll();
            if (marker == Marker.BEGIN) {
                if (!stack.isEmpty()) {
                    Marker previousMarker = stack.peek();
                    if (previousMarker == Marker.DECLARE || previousMarker == Marker.CREATE) {
                        stack.poll();
                    }
                }
            } else {
                while (marker == Marker.PROGRAM) {
                    marker = stack.poll();
                }
            }
        }

        if (stack.isEmpty()) {
            lexerEnd();
            return true;
        }

        return false;
    }


    protected abstract void lexerStart();
    protected abstract void lexerEnd();

    public void reset() {
        stack.clear();
    }

}
