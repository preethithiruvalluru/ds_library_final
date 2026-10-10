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
 * A marker document remembers that this happened, so tools you delete later are NOT added back.
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private final ToolRepository tools;
    private final MongoTemplate mongo;

    public DataSeeder(ToolRepository tools, MongoTemplate mongo) {
        this.tools = tools;
        this.mongo = mongo;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            if (mongo.getCollection("app_meta").countDocuments(new Document("_id", "toolsSeeded")) > 0) return;
            if (tools.count() == 0) {
                tools.saveAll(defaults());
                log.info("Added the starter list of Data Science tools.");
            }
            mongo.getCollection("app_meta").insertOne(new Document("_id", "toolsSeeded"));
        } catch (Exception e) {
            log.warn("Could not add starter tools (is MongoDB running?): {}", e.getMessage());
        }
    }

    private List<Tool> defaults() {
        String lang = "Programming Languages", analysis = "Data Analysis", ml = "Machine Learning", viz = "Data Visualization",
                db = "Databases", nb = "Notebook Environments", vc = "Version Control", dev = "Development Tools";
        return List.of(
            new Tool("Python", lang, "General-purpose language and the most popular choice for data science.", "https://www.python.org", "bi-filetype-py"),
            new Tool("R", lang, "Language and environment built for statistical computing and graphics.", "https://www.r-project.org", "bi-graph-up"),
            new Tool("Java", lang, "Widely used object-oriented language, also used for big-data tools and backends.", "https://dev.java", "bi-filetype-java"),
            new Tool("SQL", lang, "Standard language for querying relational data (link goes to the SQLite SQL reference).", "https://www.sqlite.org/lang.html", "bi-database"),
            new Tool("NumPy", analysis, "Fast N-dimensional arrays and numerical computing for Python.", "https://numpy.org", "bi-grid-3x3"),
            new Tool("Pandas", analysis, "DataFrames for loading, cleaning and analysing tabular data.", "https://pandas.pydata.org", "bi-table"),
            new Tool("SciPy", analysis, "Scientific computing: statistics, optimisation, signal processing and more.", "https://scipy.org", "bi-calculator"),
            new Tool("Scikit-learn", ml, "Classic machine-learning algorithms with a simple, consistent API.", "https://scikit-learn.org", "bi-cpu"),
            new Tool("TensorFlow", ml, "End-to-end platform for building and training neural networks.", "https://www.tensorflow.org", "bi-diagram-3"),
            new Tool("PyTorch", ml, "Deep-learning framework popular in research, with dynamic computation graphs.", "https://pytorch.org", "bi-lightning-charge"),
            new Tool("Matplotlib", viz, "The standard plotting library for static charts in Python.", "https://matplotlib.org", "bi-bar-chart-line"),
            new Tool("Seaborn", viz, "Statistical data visualisation built on top of Matplotlib.", "https://seaborn.pydata.org", "bi-bar-chart"),
            new Tool("Plotly", viz, "Interactive, web-ready charts and dashboards.", "https://plotly.com/python/", "bi-pie-chart"),
            new Tool("MongoDB", db, "Document database that stores data as flexible JSON-like documents.", "https://www.mongodb.com", "bi-database"),
            new Tool("PostgreSQL", db, "Powerful open-source relational database.", "https://www.postgresql.org", "bi-database-fill"),
            new Tool("SQLite", db, "Tiny serverless SQL database stored in a single file.", "https://www.sqlite.org", "bi-database"),
            new Tool("Jupyter Notebook", nb, "Interactive notebooks that mix code, output and notes.", "https://jupyter.org", "bi-journal-code"),
            new Tool("Google Colab", nb, "Free hosted Jupyter notebooks that run in your browser.", "https://colab.research.google.com", "bi-cloud"),
            new Tool("Kaggle Notebooks", nb, "Browser notebooks with datasets and competitions built in.", "https://www.kaggle.com/code", "bi-journal-richtext"),
            new Tool("Git", vc, "Distributed version control for tracking changes to your code.", "https://git-scm.com", "bi-git"),
            new Tool("GitHub", vc, "Hosting for Git repositories, collaboration and portfolios.", "https://github.com", "bi-github"),
            new Tool("Visual Studio Code", dev, "Lightweight, extensible code editor with great Python and Java support.", "https://code.visualstudio.com", "bi-code-square"),
            new Tool("Anaconda", dev, "Python distribution with package and environment management for data science.", "https://www.anaconda.com", "bi-box-seam")
        );
    }
}
