package com.hanna.webservice.config.dto;

import com.hanna.webservice.domain.user.User;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class SessionUser implements Serializable { //세션에 사용자정보 담는 DTO (인증된 사용자 정보)
    private String name;
    private String email;
    private String picture;

    public SessionUser(User User) {
        this.name = User.getNickname();
        this.email = User.getEmail();
        this.picture = User.getPicture();
    }

}
