package com.davivienda.global.invoice;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.davivienda.global.invoice.client.MetricsEventClient;
import com.davivienda.global.invoice.soap.NumberConversionClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
class InvoiceApiIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private NumberConversionClient numberConversionClient;

    @MockitoBean
    private MetricsEventClient metricsEventClient;

    @Test
    void healthIsPublic() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void swaggerDocsArePublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Global-Invoice Core"));
        mockMvc.perform(get("/v3/api-docs/swagger-config"))
                .andExpect(status().isOk());
    }

    @Test
    void loginFailsWithBadCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"operador","password":"wrong"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void operadorAndAuditorCanBothCreateInvoices() throws Exception {
        String operadorToken = login("operador", "Operador123!");
        String auditorToken = login("auditor", "Auditor123!");

        mockMvc.perform(post("/api/invoices")
                        .header("Authorization", "Bearer " + operadorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "NACIONAL",
                                  "subtotal": 100,
                                  "clientId": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(119.00))
                .andExpect(jsonPath("$.iva").value(19.00));

        mockMvc.perform(post("/api/invoices")
                        .header("Authorization", "Bearer " + auditorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "NACIONAL",
                                  "subtotal": 10,
                                  "clientId": 1
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/invoices")
                        .header("Authorization", "Bearer " + auditorToken))
                .andExpect(status().isOk());
    }

    @Test
    void exportWithoutCustomsCodeIsRejected() throws Exception {
        String token = login("operador", "Operador123!");
        mockMvc.perform(post("/api/invoices")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "EXPORTACION",
                                  "subtotal": 100,
                                  "clientId": 1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("aduanero")));
    }

    @Test
    void detailUsesSoapConversionAndAcceptsQueryToken() throws Exception {
        String token = login("operador", "Operador123!");
        MvcResult created = mockMvc.perform(post("/api/invoices")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "GUBERNAMENTAL",
                                  "subtotal": 100,
                                  "clientId": 3
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        long id = jsonMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();
        when(numberConversionClient.toWords(any())).thenReturn("one hundred and fourteen");

        mockMvc.perform(get("/api/invoices/" + id).param("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalInWords").value("one hundred and fourteen"));
    }

    @Test
    void eventsStreamRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/invoices/events"))
                .andExpect(result -> {
                    int code = result.getResponse().getStatus();
                    if (code != 401 && code != 403) {
                        throw new AssertionError("Expected 401 or 403 but was " + code);
                    }
                });
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = jsonMapper.readTree(result.getResponse().getContentAsString());
        return body.get("token").asText();
    }
}
