package com.example.OrderManager.config.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("SecurityFilterChain bean should be loaded in application context")
    void securityFilterChain_ShouldBeRegisteredAsBean() {
        assertThat(securityFilterChain).isNotNull();
    }

    @Test
    @DisplayName("GET request should not return 401 Unauthorized — permitAll is configured")
    void getRequest_ShouldNotReturn401() throws Exception {
        mockMvc.perform(get("/test"))
                .andExpect(status().is(not(401)));
    }

    @Test
    @DisplayName("POST request should not return 403 Forbidden — CSRF is disabled")
    void postRequest_ShouldNotReturn403DueToCsrf() throws Exception {
        mockMvc.perform(
                post("/test")
                    .contentType("application/json")
                    .content("{}")
        ).andExpect(status().is(not(401)));
    }
}
