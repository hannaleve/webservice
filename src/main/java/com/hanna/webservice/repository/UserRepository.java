package com.hanna.webservice.repository;

import com.hanna.webservice.domain.user.SocialType;
import com.hanna.webservice.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

  Optional<User> findByEmail(String email); //소셜로그인으로 반환되는 값 중 email을 통해 이미 생성된 사용자인지 / 처음가입한 사용자인지 판단하기위한 메소드

  /*
    소셜타입과 소셜의 식별값으로 회원 찾는 메소드
    유저정보는 DB에 있지만 추가정보가 빠진 상태 / 추가정보 : 휴대폰번호, 나이, 사는 지역 등
    OAuth2로그인 시 추가 정보 제외한 정보를 받아 DB저장함 -> 이후 추가정보 입력하기 위한 해당 회원을 DB에서 찾는 메소드
  */
  Optional<User> findBySocialTypeAndSocialId(SocialType socialType, String socialId);

  Optional<User> findByRefreshToken(String refreshToken);
}
