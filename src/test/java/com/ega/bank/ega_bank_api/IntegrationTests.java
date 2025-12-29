package com.ega.bank.ega_bank_api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationTests {


    @LocalServerPort
    private int port;

    private RestTemplate restTemplate = new RestTemplate();

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void authRegisterAndLogin_andCreateClientAndAccountAndDeposit() throws Exception {
        String base = "http://localhost:" + port;

        // register
        String registerJson = "{\"username\":\"ituser\",\"password\":\"itpass\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> registerReq = new HttpEntity<>(registerJson, headers);
        ResponseEntity<String> regResp = restTemplate.postForEntity(base + "/api/auth/register", registerReq, String.class);
        assertThat(regResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // login
        String loginJson = "{\"username\":\"ituser\",\"password\":\"itpass\"}";
        HttpEntity<String> loginReq = new HttpEntity<>(loginJson, headers);
        ResponseEntity<String> loginResp = restTemplate.postForEntity(base + "/api/auth/login", loginReq, String.class);
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        String body = loginResp.getBody();
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"").matcher(body == null ? "" : body);
        assertThat(m.find()).isTrue();
        String token = m.group(1);
        assertThat(token).isNotBlank();

        // create client
        String clientJson = "{\"firstName\":\"Test\",\"lastName\":\"Client\",\"birthDate\":\"1990-01-01\",\"gender\":\"M\",\"address\":\"Here\",\"phone\":\"+33111111111\",\"email\":\"t@example.com\",\"nationality\":\"FR\"}";
        headers.setBearerAuth(token);
        HttpEntity<String> clientReq = new HttpEntity<>(clientJson, headers);
        ResponseEntity<String> clientResp = restTemplate.postForEntity(base + "/api/clients", clientReq, String.class);
        assertThat(clientResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        String clientBody = clientResp.getBody();
        java.util.regex.Matcher cm = java.util.regex.Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(clientBody == null ? "" : clientBody);
        assertThat(cm.find()).isTrue();
        long clientId = Long.parseLong(cm.group(1));
        assertThat(clientId).isGreaterThan(0);

        // create account
        String accJson = String.format("{\"clientId\": %d, \"type\": \"SAVINGS\"}", clientId);
        HttpEntity<String> accReq = new HttpEntity<>(accJson, headers);
        ResponseEntity<String> accResp = restTemplate.postForEntity(base + "/api/accounts", accReq, String.class);
        assertThat(accResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        String accBody = accResp.getBody();
        java.util.regex.Matcher am = java.util.regex.Pattern.compile("\"accountNumber\"\\s*:\\s*\"([^\"]+)\"").matcher(accBody == null ? "" : accBody);
        assertThat(am.find()).isTrue();
        String accountNumber = am.group(1);
        assertThat(accountNumber).isNotBlank();

        // deposit
        String depositJson = "{\"amount\": 150.0 }";
        HttpEntity<String> depReq = new HttpEntity<>(depositJson, headers);
        ResponseEntity<String> depResp = restTemplate.postForEntity(base + "/api/accounts/" + accountNumber + "/deposit", depReq, String.class);
        assertThat(depResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // get account and check balance
        ResponseEntity<String> getAccResp = restTemplate.exchange(base + "/api/accounts/" + accountNumber, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(getAccResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode getAccNode = objectMapper.readTree(getAccResp.getBody());
        String balance = getAccNode.get("balance").asText();
        assertThat(balance).isNotBlank();
    }

    @Test
    void transactionsPaginationAndPdfExport() throws Exception {
        String base = "http://localhost:" + port;

        // register/login
        String registerJson = "{\"username\":\"ituser2\",\"password\":\"itpass2\"}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.postForEntity(base + "/api/auth/register", new HttpEntity<>(registerJson, headers), String.class);

        String loginJson = "{\"username\":\"ituser2\",\"password\":\"itpass2\"}";
        ResponseEntity<String> loginResp = restTemplate.postForEntity(base + "/api/auth/login", new HttpEntity<>(loginJson, headers), String.class);
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"").matcher(loginResp.getBody() == null ? "" : loginResp.getBody());
        assertThat(m.find()).isTrue();
        String token = m.group(1);

        // create client
        String clientJson = "{\"firstName\":\"Pag\",\"lastName\":\"Tester\",\"birthDate\":\"1990-01-01\",\"gender\":\"M\",\"address\":\"Here\",\"phone\":\"+33100000000\",\"email\":\"p@example.com\",\"nationality\":\"FR\"}";
        headers.setBearerAuth(token);
        HttpEntity<String> clientReq = new HttpEntity<>(clientJson, headers);
        ResponseEntity<String> clientResp = restTemplate.postForEntity(base + "/api/clients", clientReq, String.class);
        java.util.regex.Matcher cm = java.util.regex.Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(clientResp.getBody() == null ? "" : clientResp.getBody());
        assertThat(cm.find()).isTrue();
        long clientId = Long.parseLong(cm.group(1));

        // create account
        String accJson = String.format("{\"clientId\": %d, \"type\": \"CHECKING\"}", clientId);
        ResponseEntity<String> accResp = restTemplate.postForEntity(base + "/api/accounts", new HttpEntity<>(accJson, headers), String.class);
        java.util.regex.Matcher am = java.util.regex.Pattern.compile("\"accountNumber\"\\s*:\\s*\"([^\"]+)\"").matcher(accResp.getBody() == null ? "" : accResp.getBody());
        assertThat(am.find()).isTrue();
        String accountNumber = am.group(1);

        // create 25 deposits
        for (int i = 0; i < 25; i++) {
            String depositJson = String.format("{\"amount\": %d }", 10 + i);
            ResponseEntity<String> depResp = restTemplate.postForEntity(base + "/api/accounts/" + accountNumber + "/deposit", new HttpEntity<>(depositJson, headers), String.class);
            assertThat(depResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        // request first page (size 10)
        String start = "2000-01-01T00:00:00";
        String end = "2100-01-01T00:00:00";
        ResponseEntity<String> pageResp = restTemplate.exchange(base + "/api/accounts/" + accountNumber + "/transactions?start=" + start + "&end=" + end + "&page=0&size=10", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(pageResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode pageNode = objectMapper.readTree(pageResp.getBody());
        // Page serialized contains 'content' and 'totalElements'
        assertThat(pageNode.has("content")).isTrue();
        assertThat(pageNode.get("content").isArray()).isTrue();
        assertThat(pageNode.get("content")).hasSize(10);
        assertThat(pageNode.has("totalElements")).isTrue();
        assertThat(pageNode.get("totalElements").asInt()).isGreaterThanOrEqualTo(25);

        // request PDF statement
        ResponseEntity<byte[]> pdfResp = restTemplate.exchange(base + "/api/accounts/" + accountNumber + "/statement.pdf?start=" + start + "&end=" + end, HttpMethod.GET, new HttpEntity<>(headers), byte[].class);
        assertThat(pdfResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(pdfResp.getHeaders().getContentType()).isNotNull();
        assertThat(pdfResp.getHeaders().getContentType().toString()).contains("pdf");
        assertThat(pdfResp.getBody()).isNotNull();
        assertThat(pdfResp.getBody().length).isGreaterThan(100);
    }
}
