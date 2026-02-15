package com.erenalyoruk.cashgrid.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.erenalyoruk.cashgrid.BaseIntegrationTest;
import com.erenalyoruk.cashgrid.TestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class AuthIntegrationTest extends BaseIntegrationTest {

    private TestHelper helper;

    @BeforeEach
    void setUp() {
        helper = new TestHelper(mockMvc, objectMapper);
    }

    @Test
    @DisplayName("Register — should create user and return tokens")
    void register_success() throws Exception {
        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"username\":\"authtest1\",\"email\":\"authtest1@test.com\","
                                            + "\"password\":\"Test1234!\",\"role\":\"MAKER\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value("authtest1"))
                .andExpect(jsonPath("$.role").value("MAKER"));
    }

    @Test
    @DisplayName("Register — duplicate username should return 409")
    void register_duplicateUsername() throws Exception {
        helper.registerAndGetToken("authdup1", "authdup1@test.com", "MAKER");

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"username\":\"authdup1\",\"email\":\"other@test.com\","
                                                + "\"password\":\"Test1234!\",\"role\":\"MAKER\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("USERNAME_TAKEN"));
    }

    @Test
    @DisplayName("Register — invalid email should return 400")
    void register_invalidEmail() throws Exception {
        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"username\":\"bademail\",\"email\":\"not-an-email\","
                                                + "\"password\":\"Test1234!\",\"role\":\"MAKER\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Login — should return tokens")
    void login_success() throws Exception {
        helper.registerAndGetToken("logintest1", "logintest1@test.com", "MAKER");

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"username\":\"logintest1\",\"password\":\"Test1234!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.username").value("logintest1"));
    }

    @Test
    @DisplayName("Login — wrong password should return 401")
    void login_wrongPassword() throws Exception {
        helper.registerAndGetToken("loginbad1", "loginbad1@test.com", "MAKER");

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"username\":\"loginbad1\",\"password\":\"WrongPass!\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Me — should return current user info")
    void me_success() throws Exception {
        String token = helper.registerAndGetToken("metest1", "metest1@test.com", "CHECKER");

        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("metest1"))
                .andExpect(jsonPath("$.role").value("CHECKER"))
                .andExpect(jsonPath("$.email").value("metest1@test.com"));
    }

    @Test
    @DisplayName("Me — no token should return 401")
    void me_noToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Refresh — should return new tokens")
    void refresh_success() throws Exception {
        String body =
                "{\"username\":\"refreshtest1\",\"email\":\"refreshtest1@test.com\","
                        + "\"password\":\"Test1234!\",\"role\":\"MAKER\"}";

        String registerResponse =
                mockMvc.perform(
                                post("/api/v1/auth/register")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String refreshToken = objectMapper.readTree(registerResponse).get("refreshToken").asText();

        mockMvc.perform(
                        post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(String.format("{\"refreshToken\":\"%s\"}", refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    @DisplayName("Update Username — success")
    void updateUsername_success() throws Exception {
        String token = helper.registerAndGetToken("user1", "user1@test.com", "MAKER");

        mockMvc.perform(
                        patch("/api/v1/auth/me/username")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"newUsername\":\"user1updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.username").value("user1updated"));
    }

    @Test
    @DisplayName("Update Email — success")
    void updateEmail_success() throws Exception {
        String token = helper.registerAndGetToken("user2", "user2@test.com", "MAKER");

        mockMvc.perform(
                        patch("/api/v1/auth/me/email")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"newEmail\":\"user2updated@test.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user2updated@test.com"));
    }

    @Test
    @DisplayName("Update Password — success")
    void updatePassword_success() throws Exception {
        String token = helper.registerAndGetToken("user3", "user3@test.com", "MAKER");

        mockMvc.perform(
                        put("/api/v1/auth/me/password")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"currentPassword\":\"Test1234!\",\"newPassword\":\"NewPass123!\"}"))
                .andExpect(status().isNoContent());

        // Verify login with new password
        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\":\"user3\",\"password\":\"NewPass123!\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Update Password — wrong current password should return 422")
    void updatePassword_wrongCurrent() throws Exception {
        String token = helper.registerAndGetToken("user4", "user4@test.com", "MAKER");

        mockMvc.perform(
                        put("/api/v1/auth/me/password")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"currentPassword\":\"Wrong!\",\"newPassword\":\"NewPass123!\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorCode").value("INVALID_PASSWORD"));
    }
}
