package vlinh;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthApplicationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    private String url = "http://localhost:8080";
    private String testUsername = "test_username";
    private String testPassword = "test_password";

    private MvcResult registerTestUser() throws Exception {
        JSONObject requestBody = new JSONObject()
                .put("username", testUsername)
                .put("password", testPassword);
        return mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user/register")
                        .contentType("application/json")
                        .content(requestBody.toString()))
                .andDo(print())
                .andReturn();
    }

    private MvcResult loginTestUser() throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user/login")
                        .contentType("application/json")
                        .header("authorization", "Basic " + testUsername + ":" + testPassword))
                .andDo(print())
                .andReturn();
    }

    @Test
    void testNormalUserRegister() throws Exception {
        MvcResult result = registerTestUser();
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

        Optional<User> optionalUser = userRepository.findByUsername(testUsername);
        assertTrue(optionalUser.isPresent());
        User user = optionalUser.get();
        assertEquals(testUsername, user.getUsername());
        assertEquals(testPassword, user.getPassword());
        userRepository.delete(userRepository.findByUsername(testUsername).get());
    }

    @Test
    void testExistedUserRegister() throws Exception {
        registerTestUser();
        MvcResult result = registerTestUser();
        assertEquals(HttpStatus.CONFLICT.value(), result.getResponse().getStatus());
        userRepository.delete(userRepository.findByUsername(testUsername).get());
    }

    @Test
    void testNormalLogin() throws Exception {
        registerTestUser();
        MvcResult result = loginTestUser();
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        userRepository.delete(userRepository.findByUsername(testUsername).get());
    }

    @Test
    void testUnauthorizedLogin() throws Exception {
        registerTestUser();

        // test missing authorization
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user/login")
                        .contentType("application/json"))
                .andDo(print())
                .andReturn();
        assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());

        // test wrong username
        result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user/login")
                        .contentType("application/json")
                        .header("authorization", "Basic illegal_username:illegal_password"))
                .andDo(print())
                .andReturn();
        assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());

        // test wrong password
        result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user/login")
                        .contentType("application/json")
                        .header("authorization", "Basic " + testUsername + ":illegal_password"))
                .andDo(print())
                .andReturn();
        assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());

        userRepository.delete(userRepository.findByUsername(testUsername).get());
    }

    @Test
    void testNormalJwtValidate() throws Exception {
        registerTestUser();
        JSONObject responseBody = new JSONObject(loginTestUser().getResponse().getContentAsString());
        assertTrue(responseBody.has("token"));

        String token = responseBody.get("token").toString();
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user/validate")
                        .contentType("application/json")
                        .header("authorization", "Bearer " + token))
                .andDo(print())
                .andReturn();
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());

        userRepository.delete(userRepository.findByUsername(testUsername).get());
    }

    @Test
    void testAbnormalJwtValidate() throws Exception {
        registerTestUser();

        // test missing authorization
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user/validate")
                        .contentType("application/json"))
                .andDo(print())
                .andReturn();
        assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());

        // test invalid token
        result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user/validate")
                        .contentType("application/json")
                        .header("authorization", "Bearer " + "illegal_token"))
                .andDo(print())
                .andReturn();
        assertEquals(HttpStatus.FORBIDDEN.value(), result.getResponse().getStatus());

        userRepository.delete(userRepository.findByUsername(testUsername).get());
    }
}
