package com.davivienda.global.invoice.config;

import java.net.http.HttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    RestClient restClient(RestClient.Builder builder) {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        return builder.requestFactory(new JdkClientHttpRequestFactory(httpClient)).build();
    }
}
