package com.davivienda.global.invoice.soap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.davivienda.global.invoice.exception.SoapIntegrationException;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class NumberConversionClientTest {

    private NumberConversionClient client;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new NumberConversionClient(builder.build(), "http://soap.test/NumberConversion.wso");
    }

    @Test
    void convertsAmountUsingSoapResponse() {
        server.expect(requestTo("http://soap.test/NumberConversion.wso"))
                .andExpect(method(HttpMethod.POST))
            .andExpect(header("SOAPAction", ""))
                .andRespond(withSuccess(
                        "<NumberToWordsResult>one hundred and fifty</NumberToWordsResult>",
                        MediaType.TEXT_XML));

        String words = client.toWords(new BigDecimal("150.40"));

        assertThat(words).isEqualTo("one hundred and fifty");
        server.verify();
    }

    @Test
    void convertsAmountUsingNamespacedSoapResponse() {
        server.expect(requestTo("http://soap.test/NumberConversion.wso"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        "<m:NumberToWordsResult xmlns:m=\"http://www.dataaccess.com/webservicesserver/\">"
                                + "one hundred and fourteen </m:NumberToWordsResult>",
                        MediaType.TEXT_XML));

        String words = client.toWords(new BigDecimal("114"));

        assertThat(words).isEqualTo("one hundred and fourteen");
        server.verify();
    }

    @Test
    void extractResultFailsWhenTagMissing() {
        assertThatThrownBy(() -> client.extractResult("<xml/>"))
                .isInstanceOf(SoapIntegrationException.class);
    }

    @Test
    void extractResultFailsWhenBodyEmpty() {
        assertThatThrownBy(() -> client.extractResult(" "))
                .isInstanceOf(SoapIntegrationException.class);
    }

    @Test
    void rejectsNegativeAmounts() {
        assertThatThrownBy(() -> client.toWords(new BigDecimal("-1")))
                .isInstanceOf(SoapIntegrationException.class);
    }
}
