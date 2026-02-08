package org.example.megasegashop.user.service;

import org.example.megasegashop.user.dto.CreateUserProfileRequest;
import org.example.megasegashop.user.dto.UpdateUserProfileRequest;
import org.example.megasegashop.user.entity.UserProfile;
import org.example.megasegashop.user.repository.UserProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.util.StringUtils;

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
                        request.phone(),
                        null
                )));
    }

    public UserProfile getProfile(Long id) {
        return userProfileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
    }

    public UserProfile getProfileByAuthUserId(Long authUserId) {
        return userProfileRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
    }

    @Transactional
    public UserProfile updateProfile(Long id, UpdateUserProfileRequest request) {
        UserProfile profile = userProfileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        if (StringUtils.hasText(request.firstName())) {
            profile.setFirstName(request.firstName());
        }
        if (StringUtils.hasText(request.lastName())) {
            profile.setLastName(request.lastName());
        }
        if (StringUtils.hasText(request.phone())) {
            profile.setPhone(request.phone());
        }

        return userProfileRepository.save(profile);
    }

    @Transactional
    public void deleteProfile(Long id) {
        UserProfile profile = userProfileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
        userProfileRepository.delete(profile);
    }
}
