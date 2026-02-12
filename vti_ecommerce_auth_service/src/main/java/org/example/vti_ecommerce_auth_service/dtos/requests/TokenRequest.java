package org.example.vti_ecommerce_auth_service.dtos.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public class TokenRequest {
    @JsonProperty("grant_type")
    private String grantType;

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("client_secret")
    private String clientSecret;

    private String username;

    private String password;

    @JsonProperty("scope")
    private String scope;
}
