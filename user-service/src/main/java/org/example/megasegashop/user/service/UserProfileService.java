package org.example.megasegashop.user.service;

import org.example.megasegashop.user.dto.CreateUserProfileRequest;
import org.example.megasegashop.user.entity.UserProfile;
import org.example.megasegashop.user.repository.UserProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;

    public UserProfileService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Transactional
    public UserProfile createProfile(CreateUserProfileRequest request) {
        return userProfileRepository.findByEmail(request.email())
                .orElseGet(() -> userProfileRepository.save(new UserProfile(
                        null,
                        request.email(),
                        request.authUserId(),
                        request.firstName(),
                        request.lastName(),
                        null
                )));
    }

    public UserProfile getProfile(Long id) {
        return userProfileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
    }
}
