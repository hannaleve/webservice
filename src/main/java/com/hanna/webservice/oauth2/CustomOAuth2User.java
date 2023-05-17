package com.hanna.webservice.oauth2;

import com.hanna.webservice.domain.user.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.Map;

//Resource Server에서 제공하지 않는 추가 정보 필요함에 따른 Custom
@Getter
public class CustomOAuth2User extends DefaultOAuth2User {

    private String email; //처음 로그인 시 추가정보 입력
    private Role role; //추가정보 입력 확인, 처음 OAuth 로그인인지 판단

    /**
     * Constructs a {@code DefaultOAuth2User} using the provided parameters.
     *
     * @param authorities      the authorities granted to the user
     * @param attributes       the attributes about the user
     * @param nameAttributeKey the key used to access the user's &quot;name&quot; from
     *                         {@link #getAttributes()}
     */
    public CustomOAuth2User(Collection<? extends GrantedAuthority> authorities, Map<String, Object> attributes, String nameAttributeKey,String email, Role role) {
        super(authorities, attributes, nameAttributeKey);
        this.email = email;
        this.role = role;
    }
}
