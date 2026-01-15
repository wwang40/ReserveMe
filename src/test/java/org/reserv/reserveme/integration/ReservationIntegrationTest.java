package org.reserv.reserveme.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReservationIntegrationTest {

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @LocalServerPort
    private int port;

    private RestTemplate restTemplate = new RestTemplate();

    private String baseUrl() { return "http://localhost:" + port; }

    private Map<String, Object> registerAndLogin(String email, String displayName) {
        // register
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("password", "pass");
        payload.put("displayName", displayName);
        ResponseEntity<Map> reg = restTemplate.postForEntity(baseUrl() + "/api/auth/register", payload, Map.class);
        assertThat(reg.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // login
        Map<String, Object> loginReq = new HashMap<>();
        loginReq.put("email", email);
        loginReq.put("password", "pass");
        ResponseEntity<Map> login = restTemplate.postForEntity(baseUrl() + "/api/auth/login", loginReq, Map.class);
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        String token = (String) login.getBody().get("accessToken");

        // get me
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> ent = new HttpEntity<>(headers);
        ResponseEntity<Map> meResp = restTemplate.exchange(baseUrl() + "/api/users/me", HttpMethod.GET, ent, Map.class);
        assertThat(meResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map me = meResp.getBody();
        Map<String, Object> out = new HashMap<>();
        out.put("token", token);
        out.put("me", me);
        return out;
    }

    @Test
    public void reservationFlow_ownerAndRequesterSeeConfirmed() throws Exception {
        String email1 = "it-user1+" + UUID.randomUUID() + "@example.com";
        String email2 = "it-user2+" + UUID.randomUUID() + "@example.com";

        var u1 = registerAndLogin(email1, "IT User1");
        var u2 = registerAndLogin(email2, "IT User2");

        String token1 = (String) u1.get("token");
        String token2 = (String) u2.get("token");
        Map me2 = (Map) u2.get("me");
        String u2id = me2.get("id").toString();
        Map me1 = (Map) u1.get("me");
        String u1id = me1.get("id").toString();

        // user2 creates a slot
        Map<String,Object> slotPayload = new HashMap<>();
        slotPayload.put("startTime", Instant.now().plusSeconds(3600).toString());
        slotPayload.put("endTime", Instant.now().plusSeconds(7200).toString());
        HttpHeaders h2 = new HttpHeaders(); h2.setBearerAuth(token2); h2.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String,Object>> slotReq = new HttpEntity<>(slotPayload, h2);
        ResponseEntity<Map> slotRes = restTemplate.postForEntity(baseUrl() + "/api/slots", slotReq, Map.class);
        assertThat(slotRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String slotId = slotRes.getBody().get("id").toString();

        // user1 requests that slot (create reservation)
        Map<String,Object> resPayload = new HashMap<>();
        resPayload.put("slotId", slotId);
        HttpHeaders h1 = new HttpHeaders(); h1.setBearerAuth(token1); h1.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String,Object>> resReq = new HttpEntity<>(resPayload, h1);
        ResponseEntity<Map> resCreate = restTemplate.postForEntity(baseUrl() + "/api/reservations", resReq, Map.class);
        assertThat(resCreate.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Map created = resCreate.getBody();
        assertThat(created.get("status")).isEqualTo("ACTIVE");
        String reservationId = created.get("id").toString();

        // user2 lists reservations for themselves, should see incoming ACTIVE
        HttpHeaders h2Auth = new HttpHeaders(); h2Auth.setBearerAuth(token2);
        HttpEntity<Void> ent2 = new HttpEntity<>(h2Auth);
        ResponseEntity<List> list2 = restTemplate.exchange(baseUrl() + "/api/reservations?userId="+u2id, HttpMethod.GET, ent2, List.class);
        assertThat(list2.getStatusCode()).isEqualTo(HttpStatus.OK);
        List items2 = list2.getBody();
        assertThat(items2).isNotEmpty();
        // find the created reservation
        boolean found = items2.stream().anyMatch(it -> ((Map) it).get("id").toString().equals(reservationId) && ((Map)it).get("status").equals("ACTIVE"));
        assertThat(found).isTrue();

        // user2 confirms reservation
        HttpEntity<Void> confirmReq = new HttpEntity<>(h2Auth);
        ResponseEntity<Map> confirmRes = restTemplate.exchange(baseUrl() + "/api/reservations/"+reservationId+"/confirm", HttpMethod.PUT, confirmReq, Map.class);
        assertThat(confirmRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(confirmRes.getBody().get("status")).isEqualTo("CONFIRMED");

        // both users should see the confirmed reservation
        ResponseEntity<List> list2after = restTemplate.exchange(baseUrl() + "/api/reservations?userId="+u2id, HttpMethod.GET, ent2, List.class);
        assertThat(list2after.getStatusCode()).isEqualTo(HttpStatus.OK);
        boolean found2 = list2after.getBody().stream().anyMatch(it -> ((Map)it).get("id").toString().equals(reservationId) && ((Map)it).get("status").equals("CONFIRMED"));
        assertThat(found2).isTrue();

        HttpHeaders h1Auth = new HttpHeaders(); h1Auth.setBearerAuth(token1);
        ResponseEntity<List> list1after = restTemplate.exchange(baseUrl() + "/api/reservations?userId="+u1id, HttpMethod.GET, new HttpEntity<>(h1Auth), List.class);
        assertThat(list1after.getStatusCode()).isEqualTo(HttpStatus.OK);
        boolean found1 = list1after.getBody().stream().anyMatch(it -> ((Map)it).get("id").toString().equals(reservationId) && ((Map)it).get("status").equals("CONFIRMED"));
        assertThat(found1).isTrue();
    }
}
