package com.pruebas.ejercicioc;

import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

public class PaymentClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public PaymentClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.restTemplate = new RestTemplate();
    }

    public PaymentAuthorizationResponse authorize(PaymentAuthorizationRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<PaymentAuthorizationRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<PaymentAuthorizationResponse> response = restTemplate.exchange(
                    baseUrl + "/payments/authorize",
                    HttpMethod.POST,
                    entity,
                    PaymentAuthorizationResponse.class
            );
            return response.getBody();
        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode().value() == 422) {
                throw new PaymentDeclinedException(ex.getResponseBodyAsString());
            }
            throw ex;
        }
    }
}
