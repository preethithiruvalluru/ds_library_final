package com.mbu.dslibrary.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns a sentence into a list of meaningful word "stems" so that "What is overfitting?" and "overfit" look the same.
 * The very same steps are used for the stored answers and for the student's question, so they always agree.
 */
public final class TextNormalizer {

    private static final Pattern WORD = Pattern.compile("[a-z0-9]+");

    /** Words that carry no meaning for matching ("what", "the", "please"...). */
    private static final Set<String> STOPWORDS = Set.of(
            "a", "an", "the", "is", "are", "was", "were", "be", "been", "am", "do", "does", "did", "done",
            "what", "whats", "which", "who", "whom", "whose", "where", "when", "why", "how", "can", "could", "would", "should",
            "will", "shall", "may", "might", "must", "i", "me", "my", "mine", "you", "your", "we", "our", "us", "it", "its",
            "this", "that", "these", "those", "there", "here", "of", "in", "on", "at", "to", "for", "from", "by", "with", "about",
            "as", "into", "and", "or", "but", "if", "then", "so", "than", "also", "please", "pls", "plz", "tell", "explain",
            "give", "show", "define", "meaning", "mean", "means", "between", "example", "examples", "need", "want", "know",
            "get", "make", "use", "using", "used", "any", "some", "one", "two", "three", "just", "very", "much", "many",
            "vs", "versus", "ok", "okay", "kindly", "sir", "madam", "bro", "tutorial", "tutorials",
            "have", "has", "had", "doubt", "doubts", "regarding", "question", "questions", "ask", "asking", "clarify");

    /** Short forms students commonly type, spelled out. */
    private static final Map<String, List<String>> EXPANSIONS = Map.ofEntries(
            Map.entry("df", List.of("dataframe")), Map.entry("dfs", List.of("dataframe")),
            Map.entry("ml", List.of("machine", "learning")), Map.entry("dl", List.of("deep", "learning")),
            Map.entry("nn", List.of("neural", "network")), Map.entry("ai", List.of("artificial", "intelligence")),
            Map.entry("eda", List.of("exploratory", "data", "analysis")),
            Map.entry("oop", List.of("object", "oriented", "programming")), Map.entry("oops", List.of("object", "oriented", "programming")),
            Map.entry("sklearn", List.of("scikit", "learn")), Map.entry("mongo", List.of("mongodb")),
            Map.entry("postgres", List.of("postgresql")), Map.entry("js", List.of("javascript")),
            Map.entry("py", List.of("python")), Map.entry("pd", List.of("pandas")), Map.entry("np", List.of("numpy")),
            Map.entry("plt", List.of("matplotlib")), Map.entry("sns", List.of("seaborn")), Map.entry("tf", List.of("tensorflow")),
            Map.entry("ds", List.of("data", "science")), Map.entry("db", List.of("database")), Map.entry("dbms", List.of("database")),
            Map.entry("rdbms", List.of("database")), Map.entry("indices", List.of("index")), Map.entry("indexes", List.of("index")),
            Map.entry("colab", List.of("google", "colab")), Map.entry("vscode", List.of("visual", "studio", "code")),
            Map.entry("regex", List.of("regular", "expression")), Map.entry("cnn", List.of("convolutional", "neural", "network")));

    private TextNormalizer() {}

    /** The meaningful stems of a sentence, in order (duplicates kept). */
    public static List<String> tokens(String text) {
        List<String> result = new ArrayList<>();
        if (text == null) return result;
        String s = text.toLowerCase(Locale.ROOT)
                .replace("k-means", "kmeans").replace("k means", "kmeans")
                .replace("c++", "cpp").replace("c#", "csharp")
                .replace("==", " equal ").replace("!=", " notequal ")
                .replaceAll("\\bp[- ]values?\\b", "pvalue").replaceAll("\\bz[- ]scores?\\b", "zscore")
                .replaceAll("\\bt[- ]tests?\\b", "ttest")
                .replaceAll("\\b(?:program|experiment|exp|lab|practical)s?\\s*(?:no\\.?|number|#)?\\s*[-:]?\\s*(\\d+[a-z]?)(?:-(ii|i)\\b)?", " exp$1$2 ")
                .replaceAll("\\b(?:module|unit)s?\\s*[-:]?\\s*(\\d+)\\b", " module$1 ")
                .replaceAll("['’]s\\b", "")          // what's -> what, python's -> python
                .replaceAll("['’]", "");              // don't -> dont
        Matcher m = WORD.matcher(s);
        while (m.find()) {
            String raw = m.group();
            List<String> parts = EXPANSIONS.getOrDefault(raw, List.of(raw));
            for (String part : parts) {
                if (STOPWORDS.contains(part)) continue;
                if (part.length() == 1 && !part.equals("r") && !part.equals("c")) continue;   // keep the languages R and C
                result.add(stem(part));
            }
        }
        return result;
    }

    /** A very light stemmer: removes common endings so that "joins", "joined" and "joining" agree. */
    static String stem(String w) {
        if (w.length() <= 3) return w;
        if (w.endsWith("ies") && w.length() > 4) {
            w = w.substring(0, w.length() - 3) + "y";
        } else if (w.endsWith("sses")) {
            w = w.substring(0, w.length() - 2);
        } else if (w.endsWith("s") && !w.endsWith("ss") && !w.endsWith("us") && !w.endsWith("is") && w.length() > 3) {
            w = w.substring(0, w.length() - 1);
        }
        if (w.endsWith("ing") && w.length() > 5) {
            w = undouble(w.substring(0, w.length() - 3));
        } else if (w.endsWith("ed") && w.length() > 4) {
            w = undouble(w.substring(0, w.length() - 2));
        }
        return w;
    }

    private static String undouble(String w) {
        int n = w.length();
        if (n > 2 && w.charAt(n - 1) == w.charAt(n - 2) && "aeiou".indexOf(w.charAt(n - 1)) < 0 && w.charAt(n - 1) != 's' && w.charAt(n - 1) != 'l') {
            return w.substring(0, n - 1);
        }
        return w;
    }
}
