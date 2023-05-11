package com.hanna.webservice.dto.user;

import com.hanna.webservice.domain.user.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserSignUpDto {

    private String nickname;
    private String password;
    private String email;
    private String phone;
    private Integer age;
    private String city;
    private Role role;
}
