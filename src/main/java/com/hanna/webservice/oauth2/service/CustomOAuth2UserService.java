package com.hanna.webservice.oauth2.service;

import com.hanna.webservice.domain.user.SocialType;
import com.hanna.webservice.domain.user.User;
import com.hanna.webservice.oauth2.CustomOAuth2User;
import com.hanna.webservice.oauth2.OAuthAttributes;
import com.hanna.webservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

//OAuth2 로그인 로직 담당
@Log4j2
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private static final String NAVER = "naver";
    private static final String KAKAO = "kakao";

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        log.info("CustomOAuth2UserService.loadUser() 실행 - OAuth2 로그인 요청 진입");

        /*
        DefaultOAuth2UserService 객체 생성하여 loadUser(userRequest)를 통해 DefaultOAuth2User 객체를 생성 후 반환
        DefaultOAuth2UserService의 loadUser()는 소셜 로그인 API의 사용자 정보 제공 URI로 요청 보내서 사용자 정보 얻은 후 
        DefaultOAuth2User 객체 생성 후 반환한다.
        결과적으로 OAuth2User는 OAuth 서비스에서 가져온 유저 정보 담고 있는 유저
         */

        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        SocialType socialType = getSociaType(registrationId);
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName(); //OAuth2 로그인 시 키(PK)가 되는 식별값
        Map<String,Object> attributes = oAuth2User.getAttributes(); //소셜 로그인에서 API가 제공하는 userInfo의 JSON값 (유저정보들)

        //socialType에 따라 유저정보를 통해 OAuthAttributes 객체 생성
        OAuthAttributes extractAttributes = OAuthAttributes.of(socialType, userNameAttributeName,attributes);

        User createdUser = getUser(extractAttributes,socialType); //User객체 생성 후 반환

        //DefaultOAuth2User를 구현한 CustomOAuth2User 객체 생성해서 반환
        return new CustomOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(createdUser.getRole().getKey())),
                attributes,
                extractAttributes.getNameAttributeKey(),
                createdUser.getEmail(),
                createdUser.getRole()
        );
    }

    private SocialType getSociaType(String registrationId) {

        if(NAVER.equals(registrationId)) {
            return SocialType.NAVER;
        }
        if(KAKAO.equals(registrationId)) {
            return SocialType.KAKAO;
        }
        return SocialType.GOOGLE;
    }

    /*
     소셜타입과 식별값 id를 통해 회원을 찾아 반환
     만약 찾은 회원이 있다면 그대로 반환
     없다면 saveUser()호출하여 회원 저장
     */
    private User getUser(OAuthAttributes oAuthAttributes, SocialType socialType) {
        User findUser = userRepository.findBySocialTypeAndSocialId(socialType,oAuthAttributes.getOAuthUserInfo().getId()).orElse(null);

        if(findUser == null) {
            return saveUser(oAuthAttributes, socialType);
        }
        return findUser;
    }

    private User saveUser(OAuthAttributes oAuthAttributes, SocialType socialType) {
        //빌더로 User객체 생성 후 반환
        User createdUser = oAuthAttributes.toEntity(socialType, oAuthAttributes.getOAuthUserInfo());

        //생성된 User객체 DB에 저장
        return userRepository.save(createdUser);
    }
}
