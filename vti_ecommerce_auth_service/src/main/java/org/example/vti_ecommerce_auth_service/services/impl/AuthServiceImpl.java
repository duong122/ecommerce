package org.example.vti_ecommerce_auth_service.services.impl;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import org.apache.catalina.User;
import org.example.vti_ecommerce_auth_service.dtos.requests.RefreshTokenRequest;
import org.example.vti_ecommerce_auth_service.dtos.requests.RegisterRequest;
import org.example.vti_ecommerce_auth_service.dtos.requests.ResetPasswordRequest;
import org.example.vti_ecommerce_auth_service.dtos.requests.TokenRequest;
import org.example.vti_ecommerce_auth_service.dtos.responses.RegisterResponse;
import org.example.vti_ecommerce_auth_service.dtos.responses.ResetpasswordResponse;
import org.example.vti_ecommerce_auth_service.dtos.responses.TokenResponse;
import org.example.vti_ecommerce_auth_service.exceptions.AppException;
import org.example.vti_ecommerce_auth_service.exceptions.ErrorCode;
import org.example.vti_ecommerce_auth_service.services.AuthService;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final Keycloak keycloak;
    private final WebClient webClient;

    @Value("${keycloak.server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    @Override
    public RegisterResponse register(RegisterRequest request) {
        // S1: Verify if user exited or not
        List<UserRepresentation> existingUser = keycloak.realm(realm)
                .users().search(request.getUsername());

        if (!existingUser.isEmpty()) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTS);
        }

        // S2: Set password credentials
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setValue(request.getPassword());

        // S3: Build user representation send to keycloak
        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFistName());
        user.setLastName(request.getLastName());
        user.setCredentials(Collections.singletonList(credentialRepresentation));

        // S4: Call Keycloak admin API to create user
        Response response = keycloak.realm(realm).users().create(user);

        // S5: Handle response frorm keyCloak
        if (response.getStatus() == 409) {
            throw new AppException(ErrorCode.REGISTER_FAILED);
        }

        // S6: Get userId from location header that keycloak responsed
        String userId = CreatedResponseUtil.getCreatedId(response);

        return RegisterResponse.builder()
                .userId(userId)
                .email(request.getEmail())
                .username(request.getUsername())
                .build();
    }

    @Override
    public TokenResponse login(TokenRequest request) {
        String tokenUrl = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        return webClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData("grant_type", "password")
                            .with("client_id", clientId)
                            .with("client_secret", clientSecret)
                            .with("username", request.getUsername())
                            .with("password", request.getPassword()))
                    .retrieve()
                    .onStatus(
                        status -> status.value() == 401,
                        clientResponse -> Mono.error(new AppException(ErrorCode.INVALID_CREDENTIAL))  
                    )
                    .bodyToMono(TokenResponse.class)
                    .block();
        
    }

    @Override
    public TokenResponse refreshToken(RefreshTokenRequest request) {

        String tokenUrl = authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        return webClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "refresh_token")
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret)
                        .with("refresh_token", request.getRefreshToken()))
                .retrieve()
                .onStatus(status -> status.value() == 400, 
                            clientResponse -> Mono.error(new AppException(ErrorCode.INVALID_REFRESH_TOKEN)))  
                .onStatus(
                    status -> status.value() == 401,
                    clientResponse -> Mono.error(new AppException(ErrorCode.UNAUTHORIZED))
                ) 
                .bodyToMono(TokenResponse.class)
                .block();

    }

    @Override
    public ResetpasswordResponse resetPassword(ResetPasswordRequest request) {
        
        // S1: Try to login to see if the old password correct or not
        try {
            TokenRequest tokenRequest = new TokenRequest();
            tokenRequest.setUsername(request.getUsername());
            tokenRequest.setPassword(request.getOldPassword());
            login(tokenRequest);
        } catch (AppException e) {
            if (e.getErrorCode() ==  ErrorCode.INVALID_CREDENTIAL) {
                throw new AppException(ErrorCode.INVALID_OLD_PASSWORD);
            }
            throw(e);
        }

        // S2: Searching for user in keycloak by username
        List<UserRepresentation> users = keycloak.realm(realm).users().search(request.getUsername());
        if (users.isEmpty()) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        // S3: Get the user
        UserRepresentation user = users.getFirst();
        String userId = user.getId();

        // S4: Create new credential and new password
        CredentialRepresentation newCredendial = new CredentialRepresentation();
        newCredendial.setType(CredentialRepresentation.PASSWORD);
        newCredendial.setTemporary(false);
        newCredendial.setValue(request.getNewPassword());

        // S5: Call keycloak API admin to change password
        keycloak.realm(realm).users().get(userId).resetPassword(newCredendial);

        return ResetpasswordResponse.builder()
                    .message("Reset password successfully")
                    .username(request.getUsername())
                    .build();

    }

}
