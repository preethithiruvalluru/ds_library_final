package com.mbu.dslibrary.chat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Finds the best ready-made answer for a student's question by comparing words (no AI, no internet).
 *
 * How it works:
 * 1. The question and every stored answer are reduced to meaningful word stems (see {@link TextNormalizer}).
 * 2. Rare words count more than common ones ("overfitting" tells us more than "data").
 * 3. An answer's score is how much of the question's meaning it covers, plus a bonus when a whole keyword phrase
 *    such as "inner join" appears in the question.
 * 4. Small spelling mistakes are forgiven by matching to the closest known word.
 * 5. Confident match: reply. Unsure: ask "did you mean...". No match: say so honestly and suggest topics.
 */
public final class ChatMatcher {

    /** The bot's reply. {@code entry} is null unless a stored answer was used. */
    public record Reply(String text, boolean matched, List<String> suggestions, KnowledgeEntry entry) {}

    static final double ACCEPT = 0.55;       // confident enough to answer
    static final double MAYBE = 0.30;        // worth asking "did you mean...?"
    private static final double PHRASE_BONUS = 0.30;
    private static final double EXACT_BONUS = 0.40;        // the question is exactly how this answer is worded
    private static final double KEYWORD_WEIGHT = 1.0, TITLE_WEIGHT = 0.9, QUESTION_WEIGHT = 0.8;
    private static final int MAX_SUGGESTIONS = 3;

    private static final String HELP =
            "Hi! I'm the **MBU Study Bot**. I answer from a ready-made list, so I work offline and need no internet.\n\n"
            + "You can ask me about:\n"
            + "- **Python** (lists, functions, errors, files...)\n"
            + "- **NumPy, Pandas and charts**\n"
            + "- **SQL and databases** (joins, keys, MongoDB...)\n"
            + "- **Statistics and machine learning** (overfitting, regression, accuracy...)\n"
            + "- **Tools** (Git, GitHub, Jupyter, Colab, Anaconda)\n"
            + "- **This website** and the **college**\n\n"
            + "Just type your question in your own words.";
    private static final String NOT_SURE = "I'm not completely sure what you mean. Did you mean one of these?";
    private static final String NOT_FOUND =
            "Sorry, I don't have an answer for that yet. Try asking in different words, for example *\"what is overfitting?\"*. "
            + "Here are some things I can help with:";

    private static final class Indexed {
        final KnowledgeEntry entry;
        final int order;
        final Map<String, Double> weights = new HashMap<>();
        final List<String> phrases = new ArrayList<>();       // multi-word keywords, stemmed and space-joined
        final Set<String> keywordStems = new HashSet<>();
        final Set<Set<String>> exactWordings = new HashSet<>();   // the word sets of the title and of each listed question
        Indexed(KnowledgeEntry entry, int order) { this.entry = entry; this.order = order; }
        boolean smalltalk() { return "smalltalk".equals(entry.category()); }
    }

    private record Scored(Indexed ix, double score, double raw) {}

    private final List<Indexed> main = new ArrayList<>();
    private final List<Indexed> smalltalk = new ArrayList<>();
    private final Map<String, Integer> docFreq = new HashMap<>();
    private final Set<String> smalltalkVocab = new HashSet<>();
    private final List<String> starters = new ArrayList<>();
    private final double unknownIdf;

    public ChatMatcher(List<KnowledgeEntry> entries) {
        int order = 0;
        for (KnowledgeEntry e : entries) {
            Indexed ix = new Indexed(e, order++);
            addWeights(ix, TextNormalizer.tokens(e.title()), TITLE_WEIGHT);
            ix.exactWordings.add(new HashSet<>(TextNormalizer.tokens(e.title())));
            for (String q : e.questions()) {
                addWeights(ix, TextNormalizer.tokens(q), QUESTION_WEIGHT);
                ix.exactWordings.add(new HashSet<>(TextNormalizer.tokens(q)));
            }
            for (String k : e.keywords()) {
                List<String> stems = TextNormalizer.tokens(k);
                addWeights(ix, stems, KEYWORD_WEIGHT);
                ix.keywordStems.addAll(stems);
                if (stems.size() >= 2) ix.phrases.add(String.join(" ", stems));
            }
            if (ix.smalltalk()) {
                smalltalk.add(ix);
                smalltalkVocab.addAll(ix.weights.keySet());
            } else {
                main.add(ix);
                for (String t : ix.weights.keySet()) docFreq.merge(t, 1, Integer::sum);
            }
            if (e.starter() && !ix.smalltalk()) starters.add(e.title());
        }
        this.unknownIdf = Math.log(1 + Math.max(1, main.size()));
    }

    private static void addWeights(Indexed ix, List<String> stems, double weight) {
        for (String s : stems) ix.weights.merge(s, weight, Math::max);
    }

    public List<String> starters() { return List.copyOf(starters); }

    public int size() { return main.size() + smalltalk.size(); }

    /** The topics shown when the bot can't help: a few starter questions. */
    private List<String> topicSuggestions() {
        return starters.subList(0, Math.min(6, starters.size()));
    }

    public Reply answer(String question) {
        List<String> raw = TextNormalizer.tokens(question);
        if (raw.isEmpty()) {
            return new Reply(HELP, true, topicSuggestions(), null);        // "who are you", "what can you do", empty text
        }

        // Greetings, thanks and goodbyes: only when the whole message is small talk.
        if (smalltalkVocab.containsAll(raw)) {
            Indexed best = null;
            int bestHits = 0;
            for (Indexed ix : smalltalk) {
                int hits = 0;
                for (String t : raw) if (ix.weights.containsKey(t)) hits++;
                if (hits > bestHits) { best = ix; bestHits = hits; }
            }
            if (best != null) return new Reply(best.entry.answer(), true, topicSuggestions(), best.entry);
        }

        // Real questions: drop greeting words, then repair small typos using the words we know.
        List<String> query = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String t : raw) {
            if (smalltalkVocab.contains(t) && !docFreq.containsKey(t)) continue;
            String fixed = docFreq.containsKey(t) ? t : closestKnownWord(t);
            if (seen.add(fixed)) query.add(fixed);
        }
        if (query.isEmpty()) return new Reply(HELP, true, topicSuggestions(), null);

        double denominator = 0;
        for (String t : query) denominator += idf(t);
        String queryText = " " + String.join(" ", query) + " ";
        Set<String> querySet = new HashSet<>(query);

        List<Scored> scored = new ArrayList<>();
        for (Indexed ix : main) {
            double num = 0;
            for (String t : query) {
                Double w = ix.weights.get(t);
                if (w != null) num += idf(t) * w;
            }
            if (num == 0) continue;
            double bonus = 0;
            for (String phrase : ix.phrases) if (queryText.contains(" " + phrase + " ")) bonus += PHRASE_BONUS;
            bonus = Math.min(bonus, 2 * PHRASE_BONUS);
            if (ix.exactWordings.contains(querySet)) bonus += EXACT_BONUS;
            scored.add(new Scored(ix, num / denominator + bonus, num + bonus));
        }
        scored.sort(Comparator.<Scored>comparingDouble(s -> -s.score())
                .thenComparingDouble(s -> -s.raw())
                .thenComparingInt(s -> s.ix().order));

        if (scored.isEmpty()) return new Reply(NOT_FOUND, false, topicSuggestions(), null);

        Scored best = scored.get(0);
        double accept = best.ix().entry.threshold() != null ? best.ix().entry.threshold() : ACCEPT;
        if (best.score() >= accept) {
            return new Reply(best.ix().entry.answer(), true, related(scored, best.ix()), best.ix().entry);
        }
        if (best.score() >= MAYBE) {
            return new Reply(NOT_SURE, false, related(scored, null), null);
        }
        return new Reply(NOT_FOUND, false, topicSuggestions(), null);
    }

    /** Up to three other close answers, shown as "you may also ask". */
    private List<String> related(List<Scored> scored, Indexed exclude) {
        List<String> out = new ArrayList<>();
        Set<String> titles = new LinkedHashSet<>();
        for (Scored s : scored) {
            if (s.ix() == exclude || s.score() < MAYBE) continue;
            if (titles.add(s.ix().entry.title())) out.add(s.ix().entry.title());
            if (out.size() >= MAX_SUGGESTIONS) break;
        }
        return out;
    }

    private double idf(String token) {
        Integer df = docFreq.get(token);
        return df == null ? unknownIdf : Math.log(1 + (double) main.size() / df);
    }

    /** The known word closest to a misspelled one (one slip for words of 5+ letters, two for 9+), or the word itself. */
    private String closestKnownWord(String word) {
        if (word.length() < 5) return word;
        int allowed = word.length() >= 9 ? 2 : 1;
        String best = word;
        int bestDistance = allowed + 1;
        int bestFreq = -1;
        for (Map.Entry<String, Integer> e : docFreq.entrySet()) {
            String candidate = e.getKey();
            if (Math.abs(candidate.length() - word.length()) > allowed) continue;
            int d = editDistance(word, candidate, allowed);
            if (d < bestDistance || (d == bestDistance && e.getValue() > bestFreq)) {
                best = candidate;
                bestDistance = d;
                bestFreq = e.getValue();
            }
        }
        return bestDistance <= allowed ? best : word;
    }

    /** Classic edit distance (insert, delete, change, or swap two neighbouring letters), giving up above {@code limit}. */
    static int editDistance(String a, String b, int limit) {
        int n = a.length(), m = b.length();
        int[][] d = new int[n + 1][m + 1];
        for (int i = 0; i <= n; i++) d[i][0] = i;
        for (int j = 0; j <= m; j++) d[0][j] = j;
        for (int i = 1; i <= n; i++) {
            int rowMin = Integer.MAX_VALUE;
            for (int j = 1; j <= m; j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                d[i][j] = Math.min(Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1), d[i - 1][j - 1] + cost);
                if (i > 1 && j > 1 && a.charAt(i - 1) == b.charAt(j - 2) && a.charAt(i - 2) == b.charAt(j - 1)) {
                    d[i][j] = Math.min(d[i][j], d[i - 2][j - 2] + 1);
                }
                rowMin = Math.min(rowMin, d[i][j]);
            }
            if (rowMin > limit) return limit + 1;
        }
        return d[n][m];
    }
}
