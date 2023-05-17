package com.hanna.webservice.oauth2;

import com.hanna.webservice.domain.user.Role;
import com.hanna.webservice.domain.user.SocialType;
import com.hanna.webservice.domain.user.User;
import com.hanna.webservice.oauth2.userinfo.GoogleOAuth2UserInfo;
import com.hanna.webservice.oauth2.userinfo.KakaoOAuth2UserInfo;
import com.hanna.webservice.oauth2.userinfo.NaverOAuth2UserInfo;
import com.hanna.webservice.oauth2.userinfo.OAuth2UserInfo;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;

//소셜별로 받는 데이터 분기 처리하는 DTO
@Getter
public class OAuthAttributes {

    private String nameAttributeKey; //OAuth2 로그인 진행 시 키가 되는 필드 값, PK와 같은 의미
    private OAuth2UserInfo oAuthUserInfo; // 소셜 타입별 로그인 유저 정보(닉네임, 이메일, 프로필 이미지 등)

    @Builder
    public OAuthAttributes(String nameAttributeKey, OAuth2UserInfo oAuthUserInfo) {
        this.nameAttributeKey = nameAttributeKey;
        this.oAuthUserInfo = oAuthUserInfo;
    }

    /*
    SocialType에 맞는 메소드 호출하여 OAuthAttributes 객체 반환
    파라미터 userNameAttributeName : OAuth2 로그인 시 키(PK)가 되는 값 / attributes : OAuth 서비스의 유저 정보
    소셜별 of 메소드는 각 소셜 로그인 API에서 제공하는 회원의 식별값(id), attributes, nameAttributeKey 저장 후 build
    */
    public static OAuthAttributes of(SocialType socialType, String userNameAttributeName, Map<String,Object> attributes) {

        if(socialType == SocialType.NAVER) {
            return ofNaver(userNameAttributeName, attributes);
        }
        if(socialType == SocialType.KAKAO) {
            return ofKakao(userNameAttributeName, attributes);
        }
            return ofGoogle(userNameAttributeName, attributes);

    }

    private static OAuthAttributes ofNaver(String userNameAttributeName, Map<String,Object> attributes) {
        return OAuthAttributes.builder()
                .nameAttributeKey(userNameAttributeName)
                .oAuthUserInfo(new NaverOAuth2UserInfo(attributes))
                .build();
    }

    private static OAuthAttributes ofKakao(String userNameAttributeName, Map<String,Object> attributes) {
        return OAuthAttributes.builder()
                .nameAttributeKey(userNameAttributeName)
                .oAuthUserInfo(new KakaoOAuth2UserInfo(attributes))
                .build();
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String,Object> attributes) {
        return OAuthAttributes.builder()
                .nameAttributeKey(userNameAttributeName)
                .oAuthUserInfo(new GoogleOAuth2UserInfo(attributes))
                .build();
    }

    /*
    of메소드로 OAuthAttributes 객체가 생성되며 유저 정보가 담긴 OAuth2UserInfo가 소셜 타입별로 주입된상태
    OAuth2UserInfo에는 socialId(식별값), nickname, picture 가져와서 build
    + 추가설정
     - email에는 UUID로 중복 없는 랜덤 값 생성 -> 소셜로그인 시에도 JWT Token 발급하기 위한 용도임
     - role는 GUEST로 설정
     */
    public User toEntity(SocialType socialType, OAuth2UserInfo oAuthUserInfo) {
        return User.builder()
                .socialType(socialType)
                .socialId(oAuthUserInfo.getId())
                .nickname(oAuthUserInfo.getNickname())
                .picture(oAuthUserInfo.getImageUrl())
                .email(UUID.randomUUID() + "@socialUser.com")
                .role(Role.GUEST)
                .build();
    }
}
