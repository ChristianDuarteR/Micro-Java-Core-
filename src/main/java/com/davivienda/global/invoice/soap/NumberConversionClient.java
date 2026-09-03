package com.davivienda.global.invoice.soap;

import com.davivienda.global.invoice.exception.SoapIntegrationException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClient;

@Component
public class NumberConversionClient {

    private static final Logger log = LoggerFactory.getLogger(NumberConversionClient.class);
    private static final String SOAP_ACTION = "";
    private static final int MAX_LOG_BODY_LENGTH = 500;
    private static final Pattern RESULT_PATTERN = Pattern.compile(
            "<(?:[A-Za-z_][\\w.-]*:)?NumberToWordsResult(?:\\s[^>]*)?>(.*?)</(?:[A-Za-z_][\\w.-]*:)?NumberToWordsResult>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private final RestClient restClient;
    private final String soapUrl;

    public NumberConversionClient(
            RestClient restClient,
            @Value("${soap.number-conversion-url}") String soapUrl
    ) {
        this.restClient = restClient;
        this.soapUrl = soapUrl;
    }

    public String toWords(BigDecimal amount) {
        long wholeNumber = amount.setScale(0, RoundingMode.HALF_UP).longValueExact();
        if (wholeNumber < 0) {
            throw new SoapIntegrationException("El servicio SOAP no admite montos negativos");
        }
        log.debug("Iniciando conversión SOAP NumberToWords. url={}, amount={}", soapUrl, wholeNumber);
        String envelope = """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <NumberToWords xmlns="http://www.dataaccess.com/webservicesserver/">
                      <ubiNum>%d</ubiNum>
                    </NumberToWords>
                  </soap:Body>
                </soap:Envelope>
                """.formatted(wholeNumber);
        try {
            String xml = restClient.post()
                    .uri(soapUrl)
                    .contentType(MediaType.TEXT_XML)
                    .header("SOAPAction", SOAP_ACTION)
                    .body(envelope)
                    .retrieve()
                    .body(String.class);
            String result = extractResult(xml);
            log.info("Conversión SOAP NumberToWords completada. url={}, amount={}, result={}",
                    soapUrl, wholeNumber, result);
            return result;
        } catch (SoapIntegrationException ex) {
            log.error("Respuesta SOAP inválida. url={}, amount={}, reason={}",
                    soapUrl, wholeNumber, ex.getMessage(), ex);
            throw ex;
        } catch (RestClientResponseException ex) {
            log.error("El servicio SOAP respondió con error. url={}, amount={}, status={}, body={}",
                    soapUrl, wholeNumber, ex.getStatusCode(), truncate(ex.getResponseBodyAsString()), ex);
            throw new SoapIntegrationException("El servicio SOAP respondió con un error", ex);
        } catch (Exception ex) {
            log.error("No fue posible invocar el servicio SOAP. url={}, amount={}, reason={}",
                    soapUrl, wholeNumber, ex.getMessage(), ex);
            throw new SoapIntegrationException("No fue posible convertir el total vía SOAP", ex);
        }
    }

    private static String truncate(String value) {
        if (value == null || value.length() <= MAX_LOG_BODY_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_LOG_BODY_LENGTH) + "...";
    }

    String extractResult(String xml) {
        if (xml == null || xml.isBlank()) {
            throw new SoapIntegrationException("Respuesta SOAP vacía");
        }
        Matcher matcher = RESULT_PATTERN.matcher(xml);
        if (!matcher.find()) {
            throw new SoapIntegrationException("No se encontró NumberToWordsResult en la respuesta SOAP");
        }
        return matcher.group(1).trim();
    }
}
