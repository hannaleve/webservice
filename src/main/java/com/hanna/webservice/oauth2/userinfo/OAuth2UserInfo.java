package com.hanna.webservice.oauth2.userinfo;

import java.util.Map;

public abstract class OAuth2UserInfo {
    protected Map<String,Object> attributes;

    public OAuth2UserInfo(Map<String,Object> attributes) { //파라미터 : 각 소셜 타입별 유저정보 attributes
        this.attributes = attributes;
    }

    //우리 서비스에서 사용하고자 하는 유저정보들을 가져오는 메소드

    public abstract String getId(); //소셜 식별 값 : 구글 - "sub", 카카오 - "id", 네이버 - "id"

    public abstract String getNickname();

    public abstract String getImageUrl();
}
