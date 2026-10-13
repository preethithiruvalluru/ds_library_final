package com.mbu.dslibrary.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.mbu.dslibrary.model.Experiment;
import com.mbu.dslibrary.model.Module;
import com.mbu.dslibrary.model.Resource;
import com.mbu.dslibrary.model.StoredFile;
import com.mbu.dslibrary.model.Tool;

/** The study bot must be able to answer from what is on the website: modules, PDFs, lab experiments, tools, resources. */
class PortalKnowledgeTest {

    static ChatMatcher bot;

    @BeforeAll
    static void build() {
        Module m1 = module("m1", 1, "Introduction & Domain Applications", "Unit 1 slides.", "Descriptive Analysis", "Data Processing");
        Module m2 = module("m2", 2, "Sensors & Connectivity", "Unit 2 slides.", "Decision Tree");
        Module m3 = module("m3", 3, "Prototyping & Hardware", "");

        Experiment e1 = new Experiment();
        e1.setId("e1");
        e1.setExperimentNumber("4B");
        e1.setTitle("Rearranging data with stack and unstack");
        e1.setAim("To rearrange tabular data with hierarchical indexing using the Pandas unstack() and stack() methods.");
        e1.setSourceCode("data = pd.DataFrame({'a': [1, 2]})\nprint(data.unstack())\nprint(data.stack())");
        e1.setOutputText("Original Tabular Data");
        e1.setResult("unstack() converts row-level indexes into columns.");
        Experiment e2 = new Experiment();
        e2.setId("e2");
        e2.setExperimentNumber("5B-ii");
        e2.setTitle("Stacked bar plot from a DataFrame");
        e2.setAim("To create a stacked bar plot.");
        e2.setSourceCode("df.plot(kind='bar', stacked=True)");

        Tool tool = new Tool();
        tool.setId("t1");
        tool.setName("Jupyter Notebook");
        tool.setCategory("IDE");
        tool.setDescription("Interactive notebooks for code and notes.");
        tool.setOfficialUrl("https://jupyter.org/");
        tool.setPlaylistUrl("https://www.youtube.com/playlist?list=PLabc");

        Resource free = resource("r1", "Kaggle Learn: free micro-courses", "LINK", "https://www.kaggle.com/learn", "free", "course");
        Resource paid = resource("r2", "Udemy: Python Bootcamp (paid)", "LINK", "https://www.udemy.com/course/x/", "paid", "course");
        Resource w3 = resource("r3", "W3Schools: Pandas tutorial", "TUTORIAL", "https://www.w3schools.com/python/pandas/default.asp", "w3schools");
        Resource yt = resource("r4", "SQL Tutorial full course", "YOUTUBE", "https://www.youtube.com/watch?v=HXV3zeQKqGY", "youtube");

        List<KnowledgeEntry> all = new ArrayList<>(KnowledgeLoader.load(null));
        all.addAll(PortalKnowledge.build(List.of(m1, m2, m3), List.of(tool), List.of(free, paid, w3, yt), List.of(e1, e2), List.of()));
        bot = new ChatMatcher(all);
    }

    private static Module module(String id, int n, String name, String description, String... pdfTitles) {
        Module m = new Module();
        m.setId(id);
        m.setModuleNumber(n);
        m.setName(name);
        m.setSubject("Data Science");
        m.setDescription(description);
        List<StoredFile> files = new ArrayList<>();
        for (String t : pdfTitles) {
            StoredFile f = new StoredFile();
            f.setFileId("f-" + t);
            f.setTitle(t);
            f.setFilename(t + ".pdf");
            f.setSize(1000);
            files.add(f);
        }
        m.setMaterials(files);
        return m;
    }

    private static Resource resource(String id, String title, String type, String url, String... tags) {
        Resource r = new Resource();
        r.setId(id);
        r.setTitle(title);
        r.setType(type);
        r.setUrl(url);
        r.setSubject("Data Science");
        r.setTags(List.of(tags));
        return r;
    }

    private static String reply(String q) {
        ChatMatcher.Reply r = bot.answer(q);
        assertTrue(r.matched(), "should have an answer for: " + q + " but got: " + r.text());
        return r.text();
    }

    @Test
    void listsTheModulesOnTheWebsite() {
        String a = reply("what modules are available on this website");
        assertTrue(a.contains("Module 1: Introduction & Domain Applications"));
        assertTrue(a.contains("Module 3: Prototyping & Hardware"));
    }

    @Test
    void eachModuleNumberGetsItsOwnAnswer() {
        assertTrue(reply("what is in module 1").contains("Introduction & Domain Applications"));
        assertTrue(reply("tell me about module 2").contains("Sensors & Connectivity"));
        assertTrue(reply("module 3").contains("no PDF"));
        assertTrue(reply("module 1").contains("Descriptive Analysis"));
    }

    @Test
    void knowsWhichModuleAPdfIsIn() {
        String a = reply("where is the Descriptive Analysis pdf");
        assertTrue(a.contains("Module 1"));
        assertTrue(reply("which pdfs are available").contains("Decision Tree"));
    }

    @Test
    void answersAboutLabExperimentsByNumberAndByTopic() {
        assertTrue(reply("experiment 4b").contains("stack"));
        assertTrue(reply("program 4B").contains("Aim"));
        assertTrue(reply("exp 5b-ii").contains("stacked"));
        assertTrue(reply("what is the code for stack and unstack").contains("unstack"));
        assertTrue(reply("lab experiments list").contains("Experiment 4B"));
        assertFalse(reply("experiment 4b").contains("Stacked bar plot"), "4B must not be confused with 5B-ii");
    }

    @Test
    void answersAboutToolsAndResources() {
        String t = reply("jupyter notebook official website");
        assertTrue(t.contains("https://jupyter.org/"));
        assertTrue(reply("which free courses are available").contains("Kaggle"));
        assertFalse(reply("which free courses are available").contains("Udemy"));
        assertTrue(reply("paid courses").contains("Udemy"));
        assertTrue(reply("w3schools links").contains("w3schools.com"));
        assertTrue(reply("youtube videos").contains("youtube.com"));
    }

    @Test
    void emptyWebsiteContentAddsNothing() {
        assertTrue(PortalKnowledge.build(null, null, null, null, null).isEmpty());
        assertTrue(PortalKnowledge.build(List.of(), List.of(), List.of(), List.of(), List.of()).isEmpty());
        KnowledgeEntry any = PortalKnowledge.build(List.of(new Module()), List.of(), List.of(), List.of(), List.of()).stream().findFirst().orElse(null);
        assertNotNull(any);
        assertEquals("portal", any.category());
    }
}
