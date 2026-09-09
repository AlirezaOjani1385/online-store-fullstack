package org.store.store.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RateLimitingFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldBlockRequestsWhenRateLimitExceeded() throws Exception {
        String testIp = "192.168.1.100";

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/store/login")
                            .header("X-Forwarded-For", testIp)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"number\":\"09123456789\", \"password\":\"wrong-password\"}"))
                    .andExpect(status().is4xxClientError());
        }

        mockMvc.perform(post("/store/login")
                        .header("X-Forwarded-For", testIp)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"number\":\"09123456789\", \"password\":\"wrong-password\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldAllowRequestsFromDifferentIpWhenOneIpIsBlocked() throws Exception {
        String blockedIp = "10.0.0.1";
        String newIp = "10.0.0.2";

        for (int i = 0; i < 6; i++) {
            mockMvc.perform(post("/store/login")
                    .header("X-Forwarded-For", blockedIp)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"number\":\"09123456789\", \"password\":\"wrong-password\"}"));
        }

        mockMvc.perform(post("/store/login")
                        .header("X-Forwarded-For", newIp)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"number\":\"09123456789\", \"password\":\"wrong-password\"}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void shouldNotApplyRateLimitOnUnrestrictedPaths() throws Exception {
        String testIp = "172.16.0.1";

        for (int i = 0; i < 10; i++) {
            mockMvc.perform(post("/store/login")
                            .header("X-Forwarded-For", testIp)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"number\":\"09123456789\", \"password\":\"wrong-password\"}"))
                    .andExpect(status().is4xxClientError());
        }
    }
}