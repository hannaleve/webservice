package com.hanna.webservice.login.handler;

import com.hanna.webservice.jwt.service.JwtService;
import com.hanna.webservice.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;

//JSON 로그인 필터를 정상적으로 통과하여 인증 성공 처리가 되었을 때(로그인 성공) 실행되는 핸들러
@Log4j2
@RequiredArgsConstructor
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Value("${jwt.access.expiration}")
    private String accessTokenExpiration;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String email = extractUsername(authentication); //인증 정보에서 Username(email)추출
        //AccessToken과 RefreshToken 발급
        String accessToken = jwtService.createAccessToken(email);
        String refreshToken = jwtService.createRefreshToken();

        //응답 헤더에 AccessToken과 RefreshToken 실어서 응답
        jwtService.sendAccessAndRefreshToken(response,accessToken,refreshToken);

        userRepository.findByEmail(email)
                //회원가입 시에는 RefreshToken Column이 null이 때문에 로그인 성공 시 발급된 RefreshToken을 DB에 저장
                .ifPresent(user -> {user.updateRefreshToken(refreshToken);
                userRepository.saveAndFlush(user);
                });

        log.info("로그인 성공----- 이메일 : {}", email);
        log.info("로그인 성공----- AccessToken 발급 : {}",accessToken);
        log.info("발급된 AccessToken 만료기간 : {}", accessTokenExpiration);
    }

    private String extractUsername(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }
}
