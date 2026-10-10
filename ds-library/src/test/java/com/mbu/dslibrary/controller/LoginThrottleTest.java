package com.mbu.dslibrary.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.mbu.dslibrary.config.SecurityConfig;

/** Kept in its own class (its own app context) so its failed logins do not affect other tests. */
@WebMvcTest(controllers = AuthController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {"app.admin.username=admin", "app.admin.password=correct-horse-battery", "test.throttle=true"})
class LoginThrottleTest {

    @Autowired
    MockMvc mvc;

    private void attempt(String password, int expectedStatus) throws Exception {
        mvc.perform(post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"" + password + "\"}"))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    void afterFiveWrongPasswordsEvenTheRightOneIsBlockedForAWhile() throws Exception {
        for (int i = 0; i < 5; i++) attempt("wrong" + i, 401);
        attempt("correct-horse-battery", 429);
    }
}
