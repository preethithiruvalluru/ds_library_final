package com.mbu.dslibrary.config;

import java.util.List;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.mbu.dslibrary.model.Tool;
import com.mbu.dslibrary.repository.ToolRepository;

/**
 * Adds a starter set of Data Science tools the very first time the app runs.
 * Marker documents remember what was done, so tools you delete later are NOT added back and
 * playlists you change or clear are NOT overwritten.
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final String YT = "https://www.youtube.com/playlist?list=";

    private final ToolRepository tools;
    private final MongoTemplate mongo;

    public DataSeeder(ToolRepository tools, MongoTemplate mongo) {
        this.tools = tools;
        this.mongo = mongo;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            if (!marked("toolsSeeded")) {
                if (tools.count() == 0) {
                    tools.saveAll(defaults());
                    log.info("Added the starter list of Data Science tools (with YouTube playlists).");
                }
                mark("toolsSeeded");
                mark("toolPlaylistsAdded");   // new installs already have the playlists
            } else if (!marked("toolPlaylistsAdded")) {
                addMissingPlaylists();
                mark("toolPlaylistsAdded");
            }
        } catch (Exception e) {
            log.warn("Could not add starter tools (is MongoDB running?): {}", e.getMessage());
        }
    }

    /** One-time upgrade for databases created before playlists existed: fills in only the EMPTY playlist fields. */
    private void addMissingPlaylists() {
        int filled = 0;
        for (Tool d : defaults()) {
            var existing = tools.findFirstByName(d.getName());
            if (existing.isPresent() && (existing.get().getPlaylistUrl() == null || existing.get().getPlaylistUrl().isBlank())) {
                Tool t = existing.get();
                t.setPlaylistUrl(d.getPlaylistUrl());
                tools.save(t);
                filled++;
            }
        }
        log.info("Added YouTube playlists to {} existing tools.", filled);
    }

    private boolean marked(String id) {
        return mongo.getCollection("app_meta").countDocuments(new Document("_id", id)) > 0;
    }

    private void mark(String id) {
        mongo.getCollection("app_meta").insertOne(new Document("_id", id));
    }

    /**
     * The starter tools. Every playlist was checked with YouTube (it exists, and the channel and title match)
     * and chosen for being a complete, beginner-friendly course from the tool's own channel or a well-known teacher.
     */
    static List<Tool> defaults() {
        String lang = "Programming Languages", analysis = "Data Analysis", ml = "Machine Learning", viz = "Data Visualization",
                db = "Databases", nb = "Notebook Environments", vc = "Version Control", dev = "Development Tools";
        return List.of(
            new Tool("Python", lang, "General-purpose language and the most popular choice for data science.", "https://www.python.org",
                YT + "PL-osiE80TeTt2d9bfVyTiXJA-UTHn6WwU", "bi-filetype-py"),                       // Corey Schafer, 158 videos
            new Tool("R", lang, "Language and environment built for statistical computing and graphics.", "https://www.r-project.org",
                YT + "PLqzoL9-eJTNDw71zWePXyHx3_cm_fMP8S", "bi-graph-up"),                            // MarinStatsLectures, 56 videos
            new Tool("Java", lang, "Widely used object-oriented language, also used for big-data tools and backends.", "https://dev.java",
                YT + "PLIY8eNdw5tW_uaJgi-FL9QwINS9JxKKg2", "bi-filetype-java"),                      // Simple Snippets, 55 videos
            new Tool("SQL", lang, "Standard language for querying relational data (link goes to the SQLite SQL reference).", "https://www.sqlite.org/lang.html",
                YT + "PLavw5C92dz9Ef4E-1Zi9KfCTXS_IN8gXZ", "bi-database"),                           // techTFQ, 30 videos
            new Tool("NumPy", analysis, "Fast N-dimensional arrays and numerical computing for Python.", "https://numpy.org",
                YT + "PLzgPDYo_3xukqLLjNeuCxj4CwvkJin03Z", "bi-grid-3x3"),                           // Amulya's Academy, 30 videos
            new Tool("Pandas", analysis, "DataFrames for loading, cleaning and analysing tabular data.", "https://pandas.pydata.org",
                YT + "PL5-da3qGB5ICCsgW1MxlZ0Hq8LL5U3u9y", "bi-table"),                             // Data School, 38 videos
            new Tool("SciPy", analysis, "Scientific computing: statistics, optimisation, signal processing and more.", "https://scipy.org",
                YT + "PL1A5nGiCuuctkzARw3rfKIKUJEmby3X14", "bi-calculator"),                         // CodeAltus, 11 videos
            new Tool("Scikit-learn", ml, "Classic machine-learning algorithms with a simple, consistent API.", "https://scikit-learn.org",
                YT + "PL5-da3qGB5ICeMbQuqbbCOQWcS6OYBr5A", "bi-cpu"),                               // Data School, 11 videos
            new Tool("TensorFlow", ml, "End-to-end platform for building and training neural networks.", "https://www.tensorflow.org",
                YT + "PLeo1K3hjS3uu7CxAacxVndI4bE_o3BDtO", "bi-diagram-3"),                          // codebasics, 58 videos
            new Tool("PyTorch", ml, "Deep-learning framework popular in research, with dynamic computation graphs.", "https://pytorch.org",
                YT + "PL_lsbAsL_o2CTlGHgMxNrKhzP97BaG9ZN", "bi-lightning-charge"),                  // official PyTorch channel, 8 videos
            new Tool("Matplotlib", viz, "The standard plotting library for static charts in Python.", "https://matplotlib.org",
                YT + "PL-osiE80TeTvipOqomVEeZ1HRrcEvtZB_", "bi-bar-chart-line"),                    // Corey Schafer, 10 videos
            new Tool("Seaborn", viz, "Statistical data visualisation built on top of Matplotlib.", "https://seaborn.pydata.org",
                YT + "PLtPIclEQf-3cG31dxSMZ8KTcDG7zYng1j", "bi-bar-chart"),                         // Kimberly Fessel, 26 videos
            new Tool("Plotly", viz, "Interactive, web-ready charts and dashboards.", "https://plotly.com/python/",
                YT + "PLYD54mj9I2JevdabetHsJ3RLCeMyBNKYV", "bi-pie-chart"),                         // official Plotly channel, 7 videos
            new Tool("MongoDB", db, "Document database that stores data as flexible JSON-like documents.", "https://www.mongodb.com",
                YT + "PLRTey0Iqj9jh3WTvoupGK0aizk-Rmbz7u", "bi-database"),                          // Studio 3T, 16 videos
            new Tool("PostgreSQL", db, "Powerful open-source relational database.", "https://www.postgresql.org",
                YT + "PLL_LQvNX4xKxq57Vs1PYCfH6FI4socDWS", "bi-database-fill"),                     // Manish Sharma, 164 videos
            new Tool("SQLite", db, "Tiny serverless SQL database stored in a single file.", "https://www.sqlite.org",
                YT + "PLsNbVrUGd0KJMAydPNRDvYwyai-nJTyX7", "bi-database"),                          // Tenfingers, 41 videos
            new Tool("Jupyter Notebook", nb, "Interactive notebooks that mix code, output and notes.", "https://jupyter.org",
                YT + "PLS1QulWo1RIb_iOwGc7uddfLLPpMIREBn", "bi-journal-code"),                      // ProgrammingKnowledge, 31 videos
            new Tool("Google Colab", nb, "Free hosted Jupyter notebooks that run in your browser.", "https://colab.research.google.com",
                YT + "PLA83b1JHN4ly56Y7o6vDAT8Szxc3_EdRH", "bi-cloud"),                             // MyStudy, 9 videos
            new Tool("Kaggle Notebooks", nb, "Browser notebooks with datasets and competitions built in.", "https://www.kaggle.com/code",
                YT + "PLqFaTIg4myu9-T-fat2zjC5HmTpSybNfa", "bi-journal-richtext"),                  // official Kaggle channel, 8 videos
            new Tool("Git", vc, "Distributed version control for tracking changes to your code.", "https://git-scm.com",
                YT + "PL4cUxeGkcC9goXbgTDQ0n_4TBzOO0ocPR", "bi-git"),                               // The Net Ninja, 12 videos
            new Tool("GitHub", vc, "Hosting for Git repositories, collaboration and portfolios.", "https://github.com",
                YT + "PL0lo9MOBetEFcp4SCWinBdpml9B2U25-f", "bi-github"),                            // official GitHub channel, 16 videos
            new Tool("Visual Studio Code", dev, "Lightweight, extensible code editor with great Python and Java support.", "https://code.visualstudio.com",
                YT + "PLj6YeMhvp2S5UgiQnBfvD7XgOMKs3O_G6", "bi-code-square"),                       // official VS Code channel, 19 videos
            new Tool("Anaconda", dev, "Python distribution with package and environment management for data science.", "https://www.anaconda.com",
                YT + "PLGB9meziqbzoabRQLwvpC4zBEqJ-O8MLu", "bi-box-seam")                           // official Anaconda channel, 15 videos
        );
    }
}
