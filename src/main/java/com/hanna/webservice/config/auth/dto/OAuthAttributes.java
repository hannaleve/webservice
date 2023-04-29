package com.hanna.webservice.config.auth.dto;

import com.hanna.webservice.domain.user.Role;
import com.hanna.webservice.domain.user.User;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;
@Getter
public class OAuthAttributes { //OAuth2User의 attribute를 담을 DTO

    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;
    private String picture;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String name, String email, String picture) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
        this.picture = picture;
    }

    // of() OAuth2User에서 반환하는 사용자 정보는 Map이기 때문에 값 하나하나 변환해야함
    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        return ofGoogle(userNameAttributeName,attributes);
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .picture((String) attributes.get("picture"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    /*
    User Entity생성
    OAuthAttribute에서 엔티티 생성하는 시점 - 처음 가입 시
    가입할 때 기본 권한 GUEST
     */
    public User toEntity() {
        return User.builder()
                .name(name)
                .email(email)
                .picture(picture)
                .role(Role.GUEST)
                .build();
    }
}
