package com.fithub.fithubbackend.global.auth;

import com.fithub.fithubbackend.domain.user.domain.User;
import com.fithub.fithubbackend.domain.user.repository.DocumentRepository;
import com.fithub.fithubbackend.domain.user.repository.UserRepository;
import com.fithub.fithubbackend.global.domain.Document;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 로그인 진행중인 서비스 구분
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        Map<String, Object> attributes = oAuth2User.getAttributes();

        User user = OAuthAttributes.extract(registrationId, attributes);
        if (!registrationId.equals("kakao") && userRepository.existsByEmailAndProviderIsNull(user.getEmail())) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST), "일반 회원가입이 진행된 이메일입니다.");
        }

        user = saveOrUpdate(user,registrationId);

        Map<String, Object> customAttribute = customAttribute(attributes, userNameAttributeName, user);
        return new DefaultOAuth2User(
                user.getRoles().stream()
                        .map(SimpleGrantedAuthority::new).collect(Collectors.toList()),
                customAttribute,
                userNameAttributeName
        );
    }

    private Map<String, Object> customAttribute(Map<String, Object> attributes, String userNameAttributeName, User user) {
        Map<String, Object> customAttribute = new LinkedHashMap<>();
        customAttribute.put(userNameAttributeName, attributes.get(userNameAttributeName));
        customAttribute.put("id", user.getId());
        customAttribute.put("provider", user.getProvider());
        customAttribute.put("providerId", user.getProviderId());
        customAttribute.put("name", user.getName());
        customAttribute.put("email", user.getEmail());
        customAttribute.put("phone", user.getPhone());
        customAttribute.put("gender", user.getGender().toString());
        return customAttribute;
    }

    private User saveOrUpdate(User user, String registrationId) {
        User newUser;
        if (registrationId.equals("kakao")) {
            newUser = userRepository.findByProviderId(user.getProviderId()).orElseGet(() -> ofKakao(user));
        } else {
            newUser = userRepository.findByEmailAndProvider(user.getEmail(), user.getProvider())
                    .orElseGet(() -> {
                        if(registrationId.equals("google")) {
                            return ofGoogle(user);
                        }
                        return ofNaver(user);
                    });
        }
        Optional<Document> defaultDocument = documentRepository.findById(1L);
        defaultDocument.ifPresent(newUser::updateProfileImg);
        return userRepository.save(newUser);
    }

    private User ofGoogle(User user) {
        return User.oAuthBuilder()
                .nickname(user.getNickname())
                .email(user.getEmail())
                .provider(user.getProvider())
                .providerId(user.getProviderId())
                .oAuthBuild();
    }

    private User ofKakao(User user) {
        return User.oAuthKakaoBuilder()
                .nickname(user.getNickname())
                .provider(user.getProvider())
                .providerId(user.getProviderId())
                .oAuthKakaoBuild();
    }

    private User ofNaver(User user) {
        return User.oAuthNaverBuilder()
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .name(user.getName())
                .gender(user.getGender())
                .provider(user.getProvider())
                .providerId(user.getProviderId())
                .oAuthNaverBuild();
    }
}
