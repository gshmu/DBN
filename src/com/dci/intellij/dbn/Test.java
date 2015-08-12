package com.dci.intellij.dbn;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    public static void main(String[] args) {
        //Pattern pattern = Pattern.compile("^((?:\"[^\"]*\")|(?:[^.]*))(?:.((?:\"[^\"]*\")|(?:[^.]*)))*$");
        Pattern pattern = Pattern.compile("((?:\"[^\"]*\")|(?:[^.]*))");
        Matcher matcher = pattern.matcher("ae9_common_code.\"get_order_interfa.c.es\".\"test.function\"");
        while(matcher.find()) {
            System.out.println(matcher.group(0));
        }
    }
}
