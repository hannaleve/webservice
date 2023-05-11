package com.hanna.webservice.config.auth;
import com.hanna.webservice.config.dto.OAuthAttributes;
import com.hanna.webservice.config.dto.SessionUser;
import com.hanna.webservice.repository.UserRepository;
import com.hanna.webservice.domain.user.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final HttpSession httpSession;
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException { //로그인 성공 후 후속 조치

        OAuth2UserService delegete = new DefaultOAuth2UserService();
        OAuth2User oAuth2User= delegete.loadUser(userRequest);

        /*
        registrationId
        현재 로그인 진행 중인 서비스 구분
        구글 외 네이버 로그인 연동 시 네이버 로그인인지 구글 로그인인지 구분하기위해 사용
         */
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        /*
        userNameAttributeName
        OAuth2 로그인 진행 시 키가 되는 필드값 (Primary Key와 같은 의미)
        구글의 경우 기본 코드 "sub"
        네이버/카카오 등 기본 지원안함. 이후 네이버 로그인과 구글 로그인 시 동시 지원할 때 사용
         */
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        /*
        OAuth2UserService를 통해 가져온 OAuth2User의 attribute를 담을 클래스
        이후 네이버 등 다른 소셜 로그인도 이 클래스 사용할 것이다.
         */
        OAuthAttributes oAuthAttributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        User user = saveOrUpdate(oAuthAttributes); //사용자정보변경되면 같이 수정되기 위한 대비용

        /*
        SessionUser
        세션에 사용자 정보를 저장하기 위한 Dto 클래스
         */
        httpSession.setAttribute("user", new SessionUser(user));

        return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority(user.getRoleKey())), oAuthAttributes.getAttributes(), oAuthAttributes.getNameAttributeKey());

    }

    private User saveOrUpdate(OAuthAttributes oAuthAttributes) {
        User user = userRepository.findByEmail(oAuthAttributes.getEmail())
                .map(entity -> entity.update(oAuthAttributes.getName(), oAuthAttributes.getPicture()))
                .orElse(oAuthAttributes.toEntity());

        return userRepository.save(user);
    }
}
