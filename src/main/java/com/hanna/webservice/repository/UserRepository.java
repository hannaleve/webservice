package com.hanna.webservice.repository;

import com.hanna.webservice.domain.user.SocialType;
import com.hanna.webservice.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

  Optional<User> findByEmail(String email); //email을 통해 이미 생성된 사용자인지 / 처음가입한 사용자인지 판단하기위한 메소드

  Optional<User> findBySocialTypeAndSocialId(SocialType socialType, String socialId); //소셜타입과 소셜의 식별값으로 회원 찾는 메소드

  Optional<User> findByRefreshToken(String refreshToken);
}
