package com.hanna.webservice.domain.user;

import com.hanna.webservice.domain.posts.BaseTimeEntity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30, unique = true)
    private String nickname;

    private String password;

    private String email;

    private String phone;

    private Integer age;

    private String city;

    private String picture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    private SocialType socialType; // NAVER, GOOGLE , KAKAO

    /*
    로그인한 소셜 타입 식별자 값 (일반 로그인인경우 null)
    구글 - "sub", 카카오 - "id", 네이버 - "id"
     */
    private String socialId;

    private String refreshToken; //리프레시 토큰 저장용 (만료 시 비교)

    // 유저 권한 설정 메소드 ==> 추가정보입력 후 회원가입 시 권한 변경
    public void authorizeUser() {
        this.role = Role.USER;
    }

    // 비밀번호 암호화 메소드
    public void passwordEncode(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(this.password);
    }

    // 리프레시 토큰 재발급 후 업뎃 메소드
    public void updateRefreshToken(String updateRefreshToken) {
        this.refreshToken = updateRefreshToken;
    }



}
