package com.mbu.dslibrary.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.mbu.dslibrary.config.SecurityConfig;
import com.mbu.dslibrary.model.Note;
import com.mbu.dslibrary.service.NoteService;

/** Anyone may read; only the logged-in admin may change things. No database is needed. */
@WebMvcTest(controllers = {NoteController.class, AuthController.class})
@Import(SecurityConfig.class)
@TestPropertySource(properties = {"app.admin.username=admin", "app.admin.password=correct-horse-battery"})
class SecurityWebTest {

    private static final String NOTE_JSON = "{\"title\":\"T\",\"content\":\"C\",\"tags\":[]}";

    @Autowired
    MockMvc mvc;

    @MockitoBean
    NoteService notes;

    @Test
    void anonymousVisitorsCanRead() throws Exception {
        when(notes.search(any(), any(), any(), any())).thenReturn(List.of());
        mvc.perform(get("/api/notes")).andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void writingWithoutTheCsrfTokenIsRefused() throws Exception {
        mvc.perform(post("/api/notes").contentType(MediaType.APPLICATION_JSON).content(NOTE_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void writingWhileLoggedOutIsRefusedWith401() throws Exception {
        mvc.perform(post("/api/notes").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(NOTE_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void wrongPasswordIsRefused() throws Exception {
        mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"nope\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Wrong username or password."));
    }

    @Test
    void loginUnlocksWritingAndLogoutLocksItAgain() throws Exception {
        MvcResult login = mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"correct-horse-battery\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);

        Note saved = new Note();
        saved.setTitle("T");
        when(notes.create(any())).thenReturn(saved);
        mvc.perform(post("/api/notes").session(session).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(NOTE_JSON))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/auth/me").session(session)).andExpect(jsonPath("$.authenticated").value(true));
        mvc.perform(post("/api/auth/logout").session(session).with(csrf())).andExpect(status().isOk());
        mvc.perform(post("/api/notes").session(session).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(NOTE_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void whoAmIForAVisitorSaysNotLoggedIn() throws Exception {
        mvc.perform(get("/api/auth/me")).andExpect(status().isOk()).andExpect(jsonPath("$.authenticated").value(false));
    }

    @Test
    void invalidFormsGetClearFieldErrors() throws Exception {
        mvc.perform(post("/api/notes").with(user("admin")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"content\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.title").exists())
                .andExpect(jsonPath("$.fieldErrors.content").exists());
    }
}
