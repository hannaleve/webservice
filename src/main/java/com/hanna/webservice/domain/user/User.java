package com.hanna.webservice.domain.user;

import com.hanna.webservice.domain.posts.BaseTimeEntity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30, unique = true)
    private String nickname;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false)
    private String email;

    private String phone;

    private Integer age;

    private String city;

    @Column
    private String picture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    private SocialType socialType; // NAVER, GOOGLE , KAKAO

    private String socialId; // 로그인한 소셜 타입 식별자 값 (일반 로그인인경우 null)

    private String refreshToken; //리프레시 토큰

    // 유저 권한 설정 메소드
    public void authorizeUser() {
        this.role = Role.USER;
    }

    // 비밀번호 암호화 메소드
    public void passwordEncode(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(this.password);
    }

    public void updateRefreshToken(String updateRefreshToken) {
        this.refreshToken = updateRefreshToken;
    }


    @Builder
    public User(String nickname, String password,String phone, Integer age, String city, String email, String picture, Role role) {
        this.nickname = nickname;
        this.password = password;
        this.phone = phone;
        this.age = age;
        this.city = city;
        this.email = email;
        this.picture = picture;
        this.role = role;
    }

    public User update(String nickname, String picture) {
        this.nickname = nickname;
        this.picture = picture;

        return this;
    }

    public String getRoleKey() {
        return this.role.getKey();
    }
}
