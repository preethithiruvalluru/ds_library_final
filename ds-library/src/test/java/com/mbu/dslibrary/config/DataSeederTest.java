package com.mbu.dslibrary.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import com.mbu.dslibrary.model.Tool;

/** The starter tools must all be complete, and each must come with its own well-formed YouTube playlist. */
class DataSeederTest {

    private static final Pattern PLAYLIST = Pattern.compile("^https://www\\.youtube\\.com/playlist\\?list=[A-Za-z0-9_-]{10,60}$");

    @Test
    void everyStarterToolHasAnOfficialSiteAndItsOwnPlaylist() {
        List<Tool> tools = DataSeeder.defaults();
        assertEquals(23, tools.size());

        Set<String> names = new HashSet<>();
        Set<String> playlists = new HashSet<>();
        for (Tool t : tools) {
            assertTrue(names.add(t.getName()), "duplicate tool name: " + t.getName());
            assertTrue(t.getOfficialUrl().startsWith("https://"), t.getName() + " needs an https official website");
            assertTrue(PLAYLIST.matcher(t.getPlaylistUrl()).matches(), t.getName() + " has a bad playlist link: " + t.getPlaylistUrl());
            assertTrue(playlists.add(t.getPlaylistUrl()), t.getName() + " shares its playlist with another tool: " + t.getPlaylistUrl());
        }
    }

    @Test
    void theToolsTheCourseNeedsAreAllThere() {
        Set<String> names = new HashSet<>();
        DataSeeder.defaults().forEach(t -> names.add(t.getName()));
        for (String needed : List.of("Python", "R", "Java", "SQL", "Jupyter Notebook", "Google Colab", "NumPy", "Pandas",
                "Matplotlib", "Seaborn", "Scikit-learn", "TensorFlow", "PyTorch", "MongoDB", "Git", "GitHub")) {
            assertTrue(names.contains(needed), "missing starter tool: " + needed);
        }
    }
}
