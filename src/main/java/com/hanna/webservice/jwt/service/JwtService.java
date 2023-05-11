package com.hanna.webservice.jwt.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.hanna.webservice.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Getter
@Log4j2
public class JwtService {

    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.access.expiration}")
    private Long accessTokenExpirationPeriod;

    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenExpirationPeriod;

    @Value("${jwt.access.header}")
    private String accessHeader;

    @Value("${jwt.refresh.header}")
    private String refreshHeader;

    /*
    JWT Payload에 Claim(key-value 형식으로 이루어진 하나의 쌍) 토큰에서 식별을 위해 사용할 정보
    sub(Subjectc) 토큰제목 - 토큰에서 사용자에 대한 식별값과 email 추가정의
    JWT Header에 들어오는 값 : 'Authorization(Key) = Bearer {토큰} (Value)' 형식
     */

    private static final String ACCESS_TOKEN_SUBJECT = "Authorization";
    private static final String REFRESH_TOKEN_SUBJECT = "Authorization-refresh";
    private static final String EMAIL_CLAIM = "email";
    private static final String BEARER = "Bearer";

    private final UserRepository userRepository;


    //AccessToken 생성
    public String createAccessToken(String email) {
        Date now = new Date();
        return JWT.create() //JWT 토큰 생성하는 빌더 반환
                .withSubject(ACCESS_TOKEN_SUBJECT)
                .withExpiresAt(new Date(now.getTime() + accessTokenExpirationPeriod)) //토큰 만료 시간 설정
                .withClaim(EMAIL_CLAIM,email) // Claim에 email 추가 정의 : .withClaim(클래임 이름, 클래임 값) 으로 설정
                //사용할 알고리즘과 서버의 개인 키를 지정하여 JWT 토큰이 암호화되어 생성
                .sign(Algorithm.HMAC512(secretKey)); //HMAC512 알고리즘 사용
    }

    //RefreshToken 생성
    //Claim에 email 넣지 않음. withClaim X
    public String createRefreshToken() {
        Date now = new Date();
        return JWT.create()
                .withSubject(REFRESH_TOKEN_SUBJECT)
                .withExpiresAt(new Date(now.getTime() + refreshTokenExpirationPeriod))
                .sign(Algorithm.HMAC512(secretKey));
    }

    //AccessToken 헤더에 실어서 보내기
    public void sendAccessToken(HttpServletResponse response, String accessToken) {
        response.setStatus(HttpServletResponse.SC_OK);

        response.setHeader(accessHeader,accessToken);
        log.info("재 발급된 Access Token : {}", accessToken);
    }

    //AccessToken + RefreshToken 헤더에 실어서 보내기
    public void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken) {
        response.setStatus(HttpServletResponse.SC_OK);

        setAccessTokenHeader(response,accessToken);
        setRefreshTokenHeader(response,refreshToken);
        log.info("Access Token, Refresh Token 헤더 설정 완료");
    }

    //AccessToken 헤더 설정
    public void setAccessTokenHeader(HttpServletResponse response, String accessToken) {
        response.setHeader(accessHeader,accessToken);
    }

    //RefreshToken 헤더 설정
    public void setRefreshTokenHeader(HttpServletResponse response, String refreshToken) {
        response.setHeader(refreshHeader,refreshToken);
    }


    /*
    * 클라이언트 요청에서 JWT Token, Email 추출
    * 헤더에 담긴 토큰 형식이 Bearer [토큰] 형식이므로 토큰 값을 가져오기 위해서는 Bearer를 제거해야함
    */

    /*
    헤더에서 AccessToken 추출
    토큰형식 : Bearer XXX에서 Bearer를 제외하고 순수 토큰만 가져오기 위해서
    헤더를 가져온 후 "Bearer" 삭제 (""로 replace)
     */
    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(accessHeader))
                .filter(refreshToken -> refreshToken.startsWith(BEARER))
                .map(reFreshToken -> reFreshToken.replace(BEARER,""));
    }

    /*
    헤더에서 RefreshToken 추출
    토큰형식 : Bearer XXX에서 Bearer를 제외하고 순수 토큰만 가져오기 위해서
    헤더를 가져온 후 "Bearer" 삭제 (""로 replace)
     */
    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(refreshHeader))
                .filter(refreshToken -> refreshToken.startsWith(BEARER))
                .map(reFreshToken -> reFreshToken.replace(BEARER,""));
    }

    /*
   AccessToken에서 Email 추출
   추출 전에 JWT.require()로 검증기 생성
   verify로 AccessToken 검증 후
   유효하다면 getClaim()으로 이메일 추출
   유효하지 않다면 빈 Optional 객체 반환
    */
    public Optional<String> extractEmail(String accessToken) {
        try {
            // 토큰 유효성 검사하는 데에 사용할 알고리즘이 있는 JWT verifier builder 반환
            return Optional.ofNullable(JWT.require(Algorithm.HMAC512(secretKey))
                    .build()
                    .verify(accessToken)// accessToken을 검증하고 유효하지 않다면 예외 발생
                    .getClaim(EMAIL_CLAIM)// 유효하면 claim(email) 가져오기
                    .asString());
        }catch (Exception e) {
            log.error("엑세스 토큰이 유효하지 않습니다.");
            return Optional.empty();
        }
    }


    /*
    * 유저 회원 가입 시 Entity에 저장 될 때 RefreshToken이 발급되기 전이기 때문에 DB에는 refreshToken column에 null로 저장됨
    * 따라서 로그인 시 RefreshToken을 발급하면서 발급한 RefreshToken을 DB에 저장하는 메소드
     */
    //RefreshToken DB 저장 (업데이트)
    public void updateRefreshToken(String email, String refreshToken) {
        userRepository.findByEmail(email)
                .ifPresentOrElse(
                        user -> user.updateRefreshToken(refreshToken),
                        () -> new Exception("일치하는 회원이 없습니다.")
                );
    }

    //토큰의 유효성 검사
    public boolean isTokenValid(String token) {
        try {
            JWT.require(Algorithm.HMAC512(secretKey)).build().verify(token);
            return true;
        }catch (Exception e) {
            log.error("유효하지 않은 토큰입니다. {}", e.getMessage());
            return false;
        }
    }

}
