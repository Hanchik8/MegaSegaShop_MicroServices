package org.example.megasegashop.auth.client;

import org.example.megasegashop.auth.dto.CreateUserProfileRequest;
import org.example.megasegashop.auth.dto.UserProfileResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class UserProfileClient {
    private final RestClient restClient;

    public UserProfileClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public UserProfileResponse createProfile(CreateUserProfileRequest request) {
        return restClient.post()
                .uri("http://user-service/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(UserProfileResponse.class);
    }

    public void deleteProfile(Long profileId) {
        if (profileId == null) {
            return;
        }
        try {
            restClient.delete()
                    .uri("http://user-service/users/{id}", profileId)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() != 404) {
                throw ex;
            }
        }
    }
}
