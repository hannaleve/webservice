package com.hanna.webservice.domain.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {

    /*
    자체 로그인 시 USER로 DB저장
    OAuth2 로그인 시 첫 권한 GUEST / 추가 정보 입력 후 USER
     */
    GUEST("ROLE_GUEST","손님"),
    USER("ROLE_USER","일반사용자");

    private final String key;
    private final String title;

}
