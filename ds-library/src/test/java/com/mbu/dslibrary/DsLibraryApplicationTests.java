package com.mbu.dslibrary;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * The whole application starts. The database address below points nowhere on purpose,
 * so running the tests can never read or change your real data.
 */
@SpringBootTest
@TestPropertySource(properties = "spring.data.mongodb.uri=mongodb://localhost:1/test_unreachable?serverSelectionTimeoutMS=300")
class DsLibraryApplicationTests {
    @Test
    void contextLoads() {
    }
}
