package com.mbu.dslibrary.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mbu.dslibrary.config.SecurityHeadersFilter;
import com.mbu.dslibrary.model.Note;
import com.mbu.dslibrary.service.NoteService;

/** There is no login: every visitor can read and write. Browser security headers are still sent on every response. */
@WebMvcTest(controllers = NoteController.class)
@Import(SecurityHeadersFilter.class)
class OpenAccessAndHeadersTest {

    private static final String NOTE_JSON = "{\"title\":\"T\",\"content\":\"C\",\"tags\":[]}";

    @Autowired
    MockMvc mvc;

    @MockitoBean
    NoteService notes;

    @Test
    void anyoneCanReadAndWriteWithoutLoggingIn() throws Exception {
        when(notes.search(any(), any(), any(), any())).thenReturn(List.of());
        Note saved = new Note();
        saved.setTitle("T");
        when(notes.create(any())).thenReturn(saved);

        mvc.perform(get("/api/notes")).andExpect(status().isOk());
        mvc.perform(post("/api/notes").contentType(MediaType.APPLICATION_JSON).content(NOTE_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void invalidFormsGetClearFieldErrors() throws Exception {
        mvc.perform(post("/api/notes").contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"\",\"content\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.title").exists())
                .andExpect(jsonPath("$.fieldErrors.content").exists());
    }

    @Test
    void everyResponseCarriesTheBrowserSecurityHeaders() throws Exception {
        when(notes.search(any(), any(), any(), any())).thenReturn(List.of());
        mvc.perform(get("/api/notes"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"))
                .andExpect(header().string("Content-Security-Policy", containsString("default-src 'self'")))
                .andExpect(header().string("Content-Security-Policy", containsString("object-src 'none'")))
                .andExpect(header().string("Content-Security-Policy", containsString("frame-ancestors 'none'")))
                .andExpect(header().exists("Permissions-Policy"));
    }
}
