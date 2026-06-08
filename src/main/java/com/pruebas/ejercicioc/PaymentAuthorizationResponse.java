package com.pruebas.ejercicioc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentAuthorizationResponse {

    private String authorizationCode;
    private String status;
    private BigDecimal approvedAmount;

    public PaymentAuthorizationResponse() {}

    public String getAuthorizationCode() { return authorizationCode; }
    public String getStatus() { return status; }
    public BigDecimal getApprovedAmount() { return approvedAmount; }

    public void setAuthorizationCode(String authorizationCode) { this.authorizationCode = authorizationCode; }
    public void setStatus(String status) { this.status = status; }
    public void setApprovedAmount(BigDecimal approvedAmount) { this.approvedAmount = approvedAmount; }
}
