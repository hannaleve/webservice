package com.hanna.webservice.oauth2.handler;

import com.hanna.webservice.domain.user.Role;
import com.hanna.webservice.jwt.service.JwtService;
import com.hanna.webservice.oauth2.CustomOAuth2User;
import com.hanna.webservice.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

//OAuth2 로그인 성공한다면 실행되는 핸들러
@Log4j2
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        log.info("OAuth2 로그인 성공!");

        try {
            CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

            //처음 요청한 회원으로 회원가입 페이지로 리다이렉트
            if(oAuth2User.getRole() == Role.GUEST) {
                String accessToken = jwtService.createAccessToken(oAuth2User.getEmail());
                response.addHeader(jwtService.getAccessHeader(), "Bearer" + accessToken);

                //회원가입 추가 내용 컨트롤러 , 서비스 , 레파지토리 생성하기!! 다음 노션에 정리 !
                // /login - 자체로그인 (get,post) , /oauthSignuUp - 회원가입 추가 (get,post) /signUp -일반 회원가입 (get,post)
                response.sendRedirect("oauth2/sign-up"); //회원가입 추가 정보 입력 프론트

                jwtService.sendAccessAndRefreshToken(response,accessToken,null); //jwt적용 - 회원가입 시 accessToken만 토큰 생성
//                User findUser = userRepository.findByEmail(oAuth2User.getEmail())
//                                .orElseThrow(() -> new IllegalArgumentException("이메일에 해당하는 유저가 없습니다."));
//                findUser.authorizeUser(); //Role 권한 변경


            } else {
                //추가정보 기입하고 이미 한 번 이상 OAuth2 로그인했던 유저일 때 Token만 발급하여 헤더에 실어서 보내기
                //로그인 성공한 경우 access, refresh 토큰 생성
                loginSuccess(response,oAuth2User);
            }
        }catch (Exception e) {
            throw  e;

        }
    }

    private void loginSuccess(HttpServletResponse response, CustomOAuth2User oAuth2User) throws IOException {
        //현재 이미 한 번 로그인했던 유저면 계속 토큰 발급 하는 중 -> RefreshToken의 유/무, 만기에 따라 다르게 처리하도록 추후 수정 할 예정
        String accessToken = jwtService.createAccessToken(oAuth2User.getEmail());
        String refreshToken = jwtService.createRefreshToken();
        response.addHeader(jwtService.getAccessHeader(), "Bearer " + accessToken);
        response.addHeader(jwtService.getRefreshHeader(), "Bearer " + refreshToken);

        jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);
        jwtService.updateRefreshToken(oAuth2User.getEmail(), refreshToken);
    }
}
