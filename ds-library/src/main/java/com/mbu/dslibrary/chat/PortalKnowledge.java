package com.mbu.dslibrary.chat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mbu.dslibrary.model.Experiment;
import com.mbu.dslibrary.model.Module;
import com.mbu.dslibrary.model.Note;
import com.mbu.dslibrary.model.Resource;
import com.mbu.dslibrary.model.StoredFile;
import com.mbu.dslibrary.model.Tool;

/**
 * Turns what is on the website (modules and their PDFs, lab experiments, tools, resources, notes) into answers the
 * study bot can match. It only repeats what is stored, so the bot never invents anything, and it stays up to date
 * when content is added.
 */
public final class PortalKnowledge {

    private static final String CATEGORY = "portal";
    private static final String SOURCE = "website content";
    private static final int MAX_CODE = 2500;
    private static final int MAX_TEXT = 1500;
    /** Words that appear in many code samples and say nothing about the topic. */
    private static final Set<String> COMMON_CALLS = Set.of("print", "head", "show", "title", "xlabel", "ylabel", "figure", "legend",
            "grid", "append", "items", "format", "round", "len", "range", "tight_layout", "info", "loc", "plot", "dict", "list", "str", "int");
    private static final Pattern CALL = Pattern.compile("(?:\\b[A-Za-z][A-Za-z0-9_]*\\.)([A-Za-z_][A-Za-z0-9_]{2,})\\(");

    private PortalKnowledge() {}

    public static List<KnowledgeEntry> build(List<Module> modules, List<Tool> tools, List<Resource> resources,
                                             List<Experiment> experiments, List<Note> notes) {
        List<KnowledgeEntry> out = new ArrayList<>();
        modules = safe(modules); tools = safe(tools); resources = safe(resources); experiments = safe(experiments); notes = safe(notes);

        Map<String, Module> moduleById = new LinkedHashMap<>();
        for (Module m : modules) moduleById.put(m.getId(), m);

        overviews(out, modules, tools, resources, experiments, notes);
        for (Module m : modules) module(out, m, resources);
        for (Module m : modules) pdfs(out, m);
        for (Experiment e : experiments) experiment(out, e, moduleById.get(e.getModuleId()));
        for (Tool t : tools) tool(out, t);
        for (Resource r : resources) resource(out, r, moduleById.get(r.getModuleId()));
        for (Note n : notes) note(out, n);
        return out;
    }

    /* ---------- overviews: "what is on the website?" ---------- */

    private static void overviews(List<KnowledgeEntry> out, List<Module> modules, List<Tool> tools, List<Resource> resources,
                                  List<Experiment> experiments, List<Note> notes) {
        if (!modules.isEmpty()) {
            StringBuilder a = new StringBuilder("These modules are on the website:\n\n");
            for (Module m : modules) {
                int pdfs = pdfCount(m);
                a.append("- **Module ").append(m.getModuleNumber()).append(": ").append(m.getName()).append("**")
                        .append(pdfs > 0 ? " (" + pdfs + " PDF" + (pdfs == 1 ? "" : "s") + ")" : " (no PDFs yet)").append("\n");
            }
            a.append("\nOpen **Modules** in the menu, click a module, then press **View PDF** to read a PDF like a book.");
            add(out, "What modules are available on this website?", "modules, module list, list of modules, all modules, syllabus, units, subjects, course content, study material",
                    List.of("which modules are there", "how many modules are there", "show all modules", "what are the modules", "what is the syllabus"), true, a.toString());
        }
        int pdfTotal = modules.stream().mapToInt(PortalKnowledge::pdfCount).sum();
        if (pdfTotal > 0) {
            StringBuilder a = new StringBuilder("There are **" + pdfTotal + " PDFs** of study material:\n\n");
            for (Module m : modules) {
                if (pdfCount(m) == 0) continue;
                a.append("**Module ").append(m.getModuleNumber()).append(": ").append(m.getName()).append("**\n");
                if (m.getSyllabus() != null) a.append("- Syllabus\n");
                for (StoredFile f : safe(m.getMaterials())) a.append("- ").append(f.getTitle() == null || f.getTitle().isBlank() ? f.getFilename() : f.getTitle()).append("\n");
                a.append("\n");
            }
            a.append("Open **Modules**, choose a module and press **View PDF**.");
            add(out, "What PDFs and study material are available?", "pdf, pdfs, study material, study materials, slides, ppt, notes pdf, books, material, download pdf, which pdfs",
                    List.of("list all pdfs", "which pdfs are available", "where is the study material", "show the slides", "how many pdfs are there"), true, a.toString());
        }
        if (!experiments.isEmpty()) {
            StringBuilder a = new StringBuilder("These lab experiments are on the website:\n\n");
            for (Experiment e : experiments) a.append("- **").append(label(e)).append("** ").append(nz(e.getTitle())).append("\n");
            a.append("\nOpen **Lab Experiments** in the menu and click one to see its aim, code, output and result. Or ask me, for example, *\"experiment 4a\"*.");
            add(out, "What lab experiments are available?", "lab experiments, lab programs, programs, practicals, practical programs, lab manual, experiments list, list of experiments, lab",
                    List.of("which lab programs are there", "list all experiments", "show the lab programs", "what are the lab experiments", "how many experiments are there"), true, a.toString());
        }
        if (!tools.isEmpty()) {
            Map<String, List<String>> byCat = new LinkedHashMap<>();
            for (Tool t : tools) byCat.computeIfAbsent(nz(t.getCategory()).isBlank() ? "Other" : t.getCategory(), k -> new ArrayList<>()).add(t.getName());
            StringBuilder a = new StringBuilder("The **Tools** section lists " + tools.size() + " data science tools:\n\n");
            byCat.forEach((cat, names) -> a.append("- **").append(cat).append(":** ").append(String.join(", ", names)).append("\n"));
            a.append("\nEach tool has an **Official website** button and a **YouTube playlist** button.");
            add(out, "What tools are listed on this website?", "tools, tool list, list of tools, software, which tools, data science tools, official website, youtube playlist for tools",
                    List.of("which tools are there", "show all the tools", "what are the tools", "what tools can i learn"), true, a.toString());
        }
        if (!resources.isEmpty()) {
            Map<String, Long> byType = new LinkedHashMap<>();
            for (Resource r : resources) byType.merge(typeLabel(r.getType()), 1L, Long::sum);
            StringBuilder a = new StringBuilder("The **Resources** section has " + resources.size() + " items:\n\n");
            byType.forEach((t, n) -> a.append("- ").append(n).append(" × ").append(t).append("\n"));
            a.append("\nYou can search it, and filter by type, subject, module or tag. Ask me for *free courses*, *paid courses*, *W3Schools tutorials* or *YouTube videos* to see those lists.");
            add(out, "What resources are available on this website?", "resources, resource list, links, learning resources, study resources, useful links, websites to learn",
                    List.of("which resources are there", "show all resources", "what are the resources", "where can i find learning links"), false, a.toString());
            listResources(out, resources, "free courses", "Which free data science courses are available?", "free course, free courses, free learning, free classes, no cost, free certificate",
                    List.of("free courses", "list the free courses", "any free course", "where can i learn for free"), r -> hasTag(r, "free") && hasTag(r, "course"));
            listResources(out, resources, "paid courses", "Which paid data science courses are available?", "paid course, paid courses, premium course, certificate course, coursera, udemy, datacamp, buy a course",
                    List.of("paid courses", "list the paid courses", "any paid course", "which courses cost money"), r -> hasTag(r, "paid") && hasTag(r, "course"));
            listResources(out, resources, "W3Schools tutorials", "Which W3Schools tutorials are available?", "w3schools, w3 schools, w3schools links, w3 school, w3school",
                    List.of("w3schools links", "list the w3schools tutorials", "any w3schools tutorial", "w3 school links"), r -> nz(r.getUrl()).contains("w3schools.com"));
            listResources(out, resources, "YouTube videos and playlists", "Which YouTube videos and playlists are available?", "youtube, youtube links, youtube videos, video, videos, video lectures, playlists, watch",
                    List.of("youtube links", "list the youtube videos", "any youtube video to learn", "video lectures"), r -> nz(r.getUrl()).contains("youtube.com") || nz(r.getUrl()).contains("youtu.be"));
        }
        if (!notes.isEmpty()) {
            StringBuilder a = new StringBuilder("These notes are on the website:\n\n");
            for (Note n : notes) a.append("- ").append(nz(n.getTitle())).append("\n");
            a.append("\nOpen **Running Notes** to read them.");
            add(out, "What notes are available?", "notes, running notes, class notes, my notes, note list",
                    List.of("show the notes", "list all notes", "which notes are there"), false, a.toString());
        }
    }

    private static void listResources(List<KnowledgeEntry> out, List<Resource> all, String what, String title, String keywords,
                                      List<String> questions, java.util.function.Predicate<Resource> filter) {
        List<Resource> list = all.stream().filter(filter).toList();
        if (list.isEmpty()) return;
        StringBuilder a = new StringBuilder("Here are the " + what + " in **Resources** (" + list.size() + "):\n\n");
        for (Resource r : list) {
            a.append("- **").append(r.getTitle()).append("**");
            if (r.getUrl() != null && !r.getUrl().isBlank()) a.append("\n  ").append(r.getUrl());
            a.append("\n");
        }
        add(out, title, keywords, questions, false, a.toString());
    }

    /* ---------- one module ---------- */

    private static void module(List<KnowledgeEntry> out, Module m, List<Resource> resources) {
        String n = String.valueOf(m.getModuleNumber());
        StringBuilder a = new StringBuilder("**Module " + n + ": " + nz(m.getName()) + "**\n\n");
        if (!nz(m.getDescription()).isBlank()) a.append(m.getDescription()).append("\n\n");
        int pdfs = pdfCount(m);
        if (pdfs == 0) {
            a.append("There is no PDF study material in this module yet.");
        } else {
            a.append("It has **").append(pdfs).append(" PDF").append(pdfs == 1 ? "" : "s").append("**:\n");
            if (m.getSyllabus() != null) a.append("- Syllabus\n");
            for (StoredFile f : safe(m.getMaterials())) a.append("- ").append(f.getTitle() == null || f.getTitle().isBlank() ? f.getFilename() : f.getTitle()).append("\n");
            a.append("\nOpen **Modules**, click this module and press **View PDF**.");
        }
        List<Resource> vids = resources.stream().filter(r -> m.getId() != null && m.getId().equals(r.getModuleId())).toList();
        if (!vids.isEmpty()) {
            a.append("\n\nLinked resources:\n");
            for (Resource r : vids) a.append("- ").append(r.getTitle()).append("\n  ").append(nz(r.getUrl())).append("\n");
        }
        add(out, "What is in Module " + n + " (" + nz(m.getName()) + ")?",
                "module " + n + ", module " + n + " topics, module " + n + " pdfs, " + nz(m.getName()),
                List.of("module " + n, "what is module " + n, "tell me about module " + n, "show module " + n, nz(m.getName())), false, a.toString());
    }

    /* ---------- one PDF ---------- */

    private static void pdfs(List<KnowledgeEntry> out, Module m) {
        List<StoredFile> files = new ArrayList<>();
        if (m.getSyllabus() != null) {
            StoredFile s = m.getSyllabus();
            files.add(s);
        }
        files.addAll(safe(m.getMaterials()));
        for (StoredFile f : files) {
            String title = f.getTitle() == null || f.getTitle().isBlank() ? nz(f.getFilename()).replaceAll("\\.pdf$", "") : f.getTitle();
            if (title.isBlank() || "Syllabus".equals(title)) continue;
            String a = "**" + title + "** is a PDF in **Module " + m.getModuleNumber() + ": " + nz(m.getName()) + "**.\n\n"
                    + "Open **Modules**, click that module and press **View PDF** next to it to read it like a book.";
            add(out, "Where is the PDF \"" + title + "\"?", title + ", " + title + " pdf, " + title + " slides",
                    List.of(title, title + " pdf", title + " slides", "where is " + title), false, a);
        }
    }

    /* ---------- one lab experiment ---------- */

    private static void experiment(List<KnowledgeEntry> out, Experiment e, Module module) {
        String lab = label(e);
        String num = nz(e.getExperimentNumber());
        StringBuilder a = new StringBuilder("**" + lab + ": " + nz(e.getTitle()) + "**");
        if (module != null) a.append(" (Module ").append(module.getModuleNumber()).append(")");
        a.append("\n\n");
        if (!nz(e.getAim()).isBlank()) a.append("**Aim:** ").append(cut(e.getAim(), MAX_TEXT)).append("\n\n");
        if (!nz(e.getRequirements()).isBlank()) a.append("**Requirements:** ").append(cut(e.getRequirements(), MAX_TEXT)).append("\n\n");
        if (!nz(e.getProcedure()).isBlank()) a.append("**Procedure / syntax:**\n").append(fence(cut(e.getProcedure(), MAX_TEXT), "")).append("\n\n");
        if (!nz(e.getSourceCode()).isBlank()) a.append("**Code:**\n").append(fence(cut(e.getSourceCode(), MAX_CODE), "")).append("\n\n");
        if (!nz(e.getOutputText()).isBlank()) a.append("**Output:**\n").append(fence(cut(e.getOutputText(), MAX_TEXT), "")).append("\n\n");
        if (!nz(e.getResult()).isBlank()) a.append("**Result:** ").append(cut(e.getResult(), MAX_TEXT)).append("\n\n");
        a.append("Open **Lab Experiments** to see it in full with highlighted code.");

        Set<String> calls = new LinkedHashSet<>();
        Matcher m = CALL.matcher(nz(e.getSourceCode()));
        while (m.find() && calls.size() < 10) {
            String c = m.group(1).toLowerCase(Locale.ROOT);
            if (!COMMON_CALLS.contains(c)) calls.add(c);
        }
        String keywords = "experiment " + num + ", exp " + num + ", program " + num + ", lab " + num + ", " + nz(e.getTitle())
                + (calls.isEmpty() ? "" : ", " + String.join(", ", calls));
        add(out, lab + ": " + nz(e.getTitle()), keywords,
                List.of("experiment " + num, "program " + num, "lab program " + num, nz(e.getTitle()), "code for " + nz(e.getTitle()), "output of " + nz(e.getTitle())), false, a.toString());
    }

    /* ---------- one tool ---------- */

    private static void tool(List<KnowledgeEntry> out, Tool t) {
        StringBuilder a = new StringBuilder("**" + nz(t.getName()) + "**");
        if (!nz(t.getCategory()).isBlank()) a.append(" (").append(t.getCategory()).append(")");
        a.append("\n\n");
        if (!nz(t.getDescription()).isBlank()) a.append(t.getDescription()).append("\n\n");
        if (!nz(t.getOfficialUrl()).isBlank()) a.append("Official website: ").append(t.getOfficialUrl()).append("\n");
        if (!nz(t.getPlaylistUrl()).isBlank()) a.append("YouTube playlist: ").append(t.getPlaylistUrl()).append("\n");
        a.append("\nFind it in the **Tools** section.");
        String name = nz(t.getName());
        add(out, "About the tool " + name, name + ", " + name + " tool, " + name + " website, " + name + " playlist, " + name + " official site",
                List.of("tell me about the " + name + " tool", name + " official website", name + " youtube playlist", name + " link", "where is the " + name + " tool"), false, a.toString());
    }

    /* ---------- one resource ---------- */

    private static void resource(List<KnowledgeEntry> out, Resource r, Module module) {
        StringBuilder a = new StringBuilder("**" + nz(r.getTitle()) + "**\n\n");
        a.append("Type: ").append(typeLabel(r.getType()));
        if (hasTag(r, "free")) a.append(" · Free");
        if (hasTag(r, "paid")) a.append(" · Paid");
        if (module != null) a.append(" · Module ").append(module.getModuleNumber());
        a.append("\n\n");
        if (!nz(r.getDescription()).isBlank()) a.append(r.getDescription()).append("\n\n");
        if (!nz(r.getUrl()).isBlank()) a.append(r.getUrl()).append("\n\n");
        a.append("Find it in the **Resources** section");
        if ("YOUTUBE".equals(r.getType())) a.append(" (press **Watch here** to play it on the page)");
        a.append(".");
        String title = nz(r.getTitle());
        add(out, "Resource: " + title, title + wordPairs(title), List.of(title, title + " link", "where is " + title), false, a.toString());
    }

    /* ---------- one note ---------- */

    private static void note(List<KnowledgeEntry> out, Note n) {
        String title = nz(n.getTitle());
        if (title.isBlank()) return;
        String a = "**" + title + "**\n\n" + cut(nz(n.getContent()), MAX_TEXT) + "\n\nOpen **Running Notes** to read the full note.";
        String tags = n.getTags() == null || n.getTags().isEmpty() ? "" : ", " + String.join(", ", n.getTags());
        add(out, "Note: " + title, title + tags, List.of(title), false, a);
    }

    /* ---------- helpers ---------- */

    private static void add(List<KnowledgeEntry> out, String title, String keywords, List<String> questions, boolean starter, String answer) {
        List<String> kw = new ArrayList<>();
        for (String k : keywords.split(",")) if (!k.isBlank()) kw.add(k.trim());
        out.add(new KnowledgeEntry(title, CATEGORY, "Website", kw, new ArrayList<>(questions), starter, null, answer, SOURCE));
    }

    /** ", word1 word2, word2 word3, ..." for a title, so partial wordings of the title still match. */
    private static String wordPairs(String title) {
        String[] w = title.replaceAll("[^A-Za-z0-9+# ]", " ").trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i + 1 < w.length && i < 12; i++) sb.append(", ").append(w[i]).append(' ').append(w[i + 1]);
        return sb.toString();
    }

    static String label(Experiment e) {
        String n = nz(e.getExperimentNumber());
        return n.isBlank() ? "Experiment" : "Experiment " + n;
    }

    private static int pdfCount(Module m) {
        return (m.getSyllabus() != null ? 1 : 0) + safe(m.getMaterials()).size();
    }

    private static boolean hasTag(Resource r, String tag) {
        return r.getTags() != null && r.getTags().stream().anyMatch(t -> t != null && t.equalsIgnoreCase(tag));
    }

    private static String typeLabel(String type) {
        if (type == null) return "Link";
        return switch (type) {
            case "PDF" -> "PDF";
            case "DOCUMENTATION" -> "Documentation";
            case "TUTORIAL" -> "Tutorial";
            case "YOUTUBE" -> "YouTube video";
            case "MATERIAL" -> "Learning material";
            default -> "Website link";
        };
    }

    private static String fence(String text, String lang) {
        return "```" + lang + "\n" + text.replace("```", "'''") + "\n```";
    }

    private static String cut(String s, int max) {
        String t = nz(s).strip();
        return t.length() <= max ? t : t.substring(0, max).stripTrailing() + "\n... (open it on the website to see the rest)";
    }

    private static String nz(String s) { return s == null ? "" : s; }

    private static <T> List<T> safe(List<T> l) { return l == null ? List.of() : l; }
}
