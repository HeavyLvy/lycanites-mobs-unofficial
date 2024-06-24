package com.lycanitesmobs.core.util;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class CustomGLImportProcessor {
    private static final String MULTI_LINE_COMMENT_PATTERN = "/\\*(?:[^*]|\\*+[^*/])*\\*+/";
    private static final String SINGLE_LINE_COMMENT_PATTERN = "//[^\\v]*";
    private static final Pattern MOJ_IMPORT_PATTERN = Pattern.compile("(#(?:/\\*(?:[^*]|\\*+[^*/])*\\*+/|\\h)*moj_import(?:/\\*(?:[^*]|\\*+[^*/])*\\*+/|\\h)*(?:\"(.*)\"|<(.*)>))");
    private static final Pattern IMPORT_VERSION_PATTERN = Pattern.compile("(#(?:/\\*(?:[^*]|\\*+[^*/])*\\*+/|\\h)*version(?:/\\*(?:[^*]|\\*+[^*/])*\\*+/|\\h)*(\\d+))\\b");
    private static final Pattern TRAILING_WHITESPACE_PATTERN = Pattern.compile("(?:^|\\v)(?:\\s|/\\*(?:[^*]|\\*+[^*/])*\\*+/|(//[^\\v]*))*\\z");

    public CustomGLImportProcessor() {
    }

    public List<String> readSource(String source) throws IOException {
        Context context = new Context();
        List<String> list = this.parseImports(source, context, "");
        list.set(0, this.readImport(list.get(0), context.column));
        return list;
    }

    private List<String> parseImports(String source, Context context, String path) throws IOException {
        int i = context.line;
        int j = 0;
        String string = "";
        List<String> list = Lists.newArrayList();
        Matcher matcher = MOJ_IMPORT_PATTERN.matcher(source);

        String string2;
        while (matcher.find()) {
            if (!hasBogusString(source, matcher, j)) {
                string2 = matcher.group(2);
                boolean bl = string2 != null;
                if (!bl) {
                    string2 = matcher.group(3);
                }

                if (string2 != null) {
                    String string3 = source.substring(j, matcher.start(1));
                    String string4 = path + string2;
                    String string5 = this.loadImport(bl, string4);
                    int k;
                    if (!Strings.isNullOrEmpty(string5)) {
                        if (!endsWithLineBreak(string5)) {
                            string5 = string5 + System.lineSeparator();
                        }

                        ++context.line;
                        k = context.line;
                        List<String> list2 = this.parseImports(string5, context, bl ? string4 : "");
                        list2.set(0, String.format(Locale.ROOT, "#line %d %d\n%s", 0, k, this.extractVersion(list2.get(0), context)));
                        if (!StringUtils.isBlank(string3)) {
                            list.add(string3);
                        }

                        list.addAll(list2);
                    } else {
                        String string6 = bl ? String.format(Locale.ROOT, "/*#moj_import \"%s\"*/", string2) : String.format(Locale.ROOT, "/*#moj_import <%s>*/", string2);
                        list.add(string + string3 + string6);
                    }

                    k = countLines(source.substring(0, matcher.end(1)));
                    string = String.format(Locale.ROOT, "#line %d %d", k, i);
                    j = matcher.end(1);
                }
            }
        }

        string2 = source.substring(j);
        if (!StringUtils.isBlank(string2)) {
            list.add(string + string2);
        }

        return list;
    }
    private static final Pattern ENDS_WITH_LINE_BREAK = Pattern.compile("(?:\\r\\n|\\v)$");
    public static boolean endsWithLineBreak(String text) {
        return ENDS_WITH_LINE_BREAK.matcher(text).find();
    }
    private static final Pattern LINE_BREAK = Pattern.compile("\\r\\n|\\v");
    public static int countLines(String text) {
        if (text.isEmpty()) {
            return 0;
        } else {
            Matcher matcher = LINE_BREAK.matcher(text);

            int i;
            for(i = 1; matcher.find(); ++i) {
            }

            return i;
        }
    }
    private String extractVersion(String line, Context context) {
        Matcher matcher = IMPORT_VERSION_PATTERN.matcher(line);
        if (matcher.find() && isLineValid(line, matcher)) {
            context.column = Math.max(context.column, Integer.parseInt(matcher.group(2)));
            return line.substring(0, matcher.start(1)) + "/*" + line.substring(matcher.start(1), matcher.end(1)) + "*/" + line.substring(matcher.end(1));
        } else {
            return line;
        }
    }

    private String readImport(String line, int start) {
        Matcher matcher = IMPORT_VERSION_PATTERN.matcher(line);
        if (matcher.find() && isLineValid(line, matcher)) {
            return line.substring(0, matcher.start(2)) + Math.max(start, Integer.parseInt(matcher.group(2))) + line.substring(matcher.end(2));
        } else {
            return line;
        }
    }

    private static boolean isLineValid(String line, Matcher matcher) {
        return !hasBogusString(line, matcher, 0);
    }

    private static boolean hasBogusString(String string, Matcher matcher, int matchEnd) {
        int i = matcher.start() - matchEnd;
        if (i == 0) {
            return false;
        } else {
            Matcher matcher2 = TRAILING_WHITESPACE_PATTERN.matcher(string.substring(matchEnd, matcher.start()));
            return !matcher2.find() || matcher2.end(1) != matcher.start();
        }
    }

    @Nullable
    public abstract String loadImport(boolean inline, String name) throws IOException;

    private static final class Context {
        int column;
        int line;

        Context() {
        }
    }
}