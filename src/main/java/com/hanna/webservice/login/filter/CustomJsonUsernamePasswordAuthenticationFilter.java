package com.hanna.webservice.login.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StreamUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/*
기본 Spring Security에서 제공하는 Form Login 방식 사용하지 않고 JSON 로그인 방식을 사용하기 위한 커스텀 필터 적용
자체 로그인 시 JSON형식으로 RequestBody로 보내는 로그인 방식
Username : 회원아이디 -> email로 설정
"/login" 요청 왔을 때 JSON 값을 매핑 처리하는 필터
 */
public class CustomJsonUsernamePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String DEFAULT_LOGIN_REQUEST_URL = "/login"; // /login으로 오는 요청을 처리
    private static final String HTTP_METHOD = "POST"; //로그인 HTTP 메소드는 POST
    private static final String CONTENT_TYPE = "application/json"; // JSON타입의 데이터로 오는 로그인 요청만 처리
    private static final String USERNAME_KEY = "email"; //회원 로그인 시 이메일 요청 JSON key : "email"
    private static final String PASSWORD_KEY = "password"; //회원 로그인 시 비밀번호 요청 JSON key : "password"
    private static final AntPathRequestMatcher DEFAULT_LOGIN_PATH_REQUEST_MATCHER
            = new AntPathRequestMatcher(DEFAULT_LOGIN_REQUEST_URL,HTTP_METHOD); // /login + POST로 온 요청에 매칭된다.
    private final ObjectMapper objectMapper;

    //위에 설정한 "login" + POST 온 요청을 처리하기 위해 설정으로 "/login" URL 들어올 시 작동
    public CustomJsonUsernamePasswordAuthenticationFilter(ObjectMapper objectMapper) {
        super(DEFAULT_LOGIN_PATH_REQUEST_MATCHER);
        this.objectMapper = objectMapper;
    }

    /*
    [인증 처리 메소드]
    - FormLogin 필터인 UsernamePasswordAuthenticationFilter와 동일하게 UsernamePasswordAuthenticationToken 객체 사용
    - 인증 처리 객체인 AuthenticationManager가 인증 시 사용할 인증 대상 객체가 된다.
    - StreamUtils를 통한 request에서 messageBody(JSON) 반환
    ex. request json
    {
        "email" : "aaa@bbb.com"
        "password" : "test123"
    }
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
       if(request.getContentType() == null || !request.getContentType().equals(CONTENT_TYPE)) {
           throw new AuthenticationServiceException("Authentication Content-Type not supported : " + request.getContentType());
       }

       //JSON요청을 String으로 변환
       String messageBody = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);

       //꺼낸 messageBody를 objectMapper.readValue를 통해 Map으로 변환
       Map<String,String> usernamePasswordMap = objectMapper.readValue(messageBody, Map.class);

       //Map의 Key(email,password)로 해당 이메일,패스워드 추출
       String email = usernamePasswordMap.get(USERNAME_KEY);
       String password = usernamePasswordMap.get(PASSWORD_KEY);

        //email : principal / password : credentials 전달
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email,password);

        /*
        인증 처리 객체인 AuthenticationManager가 인증 성공/ 인증 실패 처리를 함
        AbstractAuthenticationProcessingFilter(부모)의 getAuthenticationManager()로 AuthenticationManager 객체를 반환 받은 후
        authenticate()의 파라미터로 UsernamePasswordAuthenticationToken 객체를 넣고 인증 처리
        (AuthenticationManager(ProviderManager)에게 전달 -> SecurityConfig에 설정)
        */
        return this.getAuthenticationManager().authenticate(authenticationToken);
    }
}
