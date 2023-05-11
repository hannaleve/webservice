package com.hanna.webservice.jwt.filter;

import com.hanna.webservice.jwt.service.JwtService;
import com.hanna.webservice.jwt.util.PasswordUtil;
import com.hanna.webservice.domain.user.User;
import com.hanna.webservice.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


/*
 * JWT 인증 필터
 * /login 이외의 URI 요청 왔을 때 처리하는 필터
 * 기본적으로 사용자는 요청 헤더에 AccessToken만 담아서 요청
 * AccessToken 만료 시에만 RefreshToken을 요청 헤더에 AccessToken과 함께 요청
 *
 * 1. RefreshToken이 없고, AccessToken이 유효한 경우 -> 인증 성공
 * 2. RefreshToken이 없고, AccessToken이 없거나 유효하지 않은 경우 -> 인증 실패 403 ERROR
 * 3. RefreshToken이 있는 경우 -> DB의 RefreshToken과 비교하여 일치하면 AccessToken 재발급, RefreshToken 재발급 (RTR 방식)
 *                              인증 성공 처리는 하지 않고 실패 처리
 */


@RequiredArgsConstructor
@Log4j2
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter { //OncePerRequestFilter 인증,인가 한 번만 거치고 다음 로직 진행

    private static final String NO_CHECK_URL = "/login"; // /login으로 들어오는 요청은 Filter 작동 x

    private final JwtService jwtService;
    private final UserRepository userRepository;

    private GrantedAuthoritiesMapper grantedAuthoritiesMapper = new NullAuthoritiesMapper();


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if(request.getRequestURI().equals(NO_CHECK_URL)) {
            filterChain.doFilter(request,response); // /login 요청 들어오면 다음 필터 호출
            return; //return 이후 현재 필터 진행 막기
        }

        /*
        사용자 요청 헤더에서 RefreshToken 추출
        요청 헤더에 RefreshToken 있는경우 AccessToken 만료되어 요청된 경우이므로
        이 경우를 제외하면 추출한 RefreshToken은 모두 null
         */
        String refreshToken = jwtService.extractRefreshToken(request)
                .filter(jwtService :: isTokenValid)
                .orElse(null);

        /*
        요청 헤더에 RefreshToken 있는경우 사용자가 AccessToken 만료되어 RefreshToken까지 보낸것이므로
        RefreshToken이 DB의 RefreshToken과 일치하는지 판단 후 일치한다면 AccessToken + RefreshToken 재발급
        */
        if(refreshToken != null) {
            checkRefreshTokenAndReIssueAccessToken(response,refreshToken);
            return; //RefreshToken을 보낸 경우 AccessToken을 재발급하고 인증 처리는 하지 않기 위해 (일단 실패 처리) return 이후 현재 필터 진행 막기
        }

        /*
        RefreshToken이 없거나 유효하지 않다면 AccessToken을 검사하고 인증을 처리하는 로직 수행
        AccessToken이 없거나 유효하지 않으면 인증 객체가 담기지 않은 상태로 다음 필터로 넘어가기 때문에 403에러발생
        AccessToken이 유효하다면 인증 객체 담긴 상태로 다음 필터 넘어가기 때문에 인증 성공
         */
        if(refreshToken == null) {
            checkAccessTokenAndAuthentication(request,response,filterChain);
        }
    }

    /*
    [RefreshToken으로 User정보 찾기 & AccessToken / RefreshToken 재발급 메소드]
    - jwtService.createAccessToken()으로 AccessToken 생성,
      reIssueRefreshToken()로 해당 유저의 RefreshToken 재발급 & DB에 RefreshToken 업데이트 메소드 호출
    그 후 jwtService.sendAccessTokenAndRefreshToken()으로 응답 헤더 보내기
     */
    public void checkRefreshTokenAndReIssueAccessToken(HttpServletResponse response, String refreshToken) {
        userRepository.findByRefreshToken(refreshToken) // 파라미터로 들어온 헤더에서 추출한 RefreshToken으로 DB에서 유저를 찾고
                .ifPresent(user -> { //유저가 있다면
                    String reIssuedRefreshToken = reIssueRefreshToken(user);
                    jwtService.sendAccessAndRefreshToken(response, jwtService.createAccessToken(user.getEmail()),
                            reIssuedRefreshToken);
                });
    }

    /*
    [RefreshToken 재발급 & DB에 RefreshToken 업데이트 메소드]
    jwtService.createRefreshToken()으로 RefreshToken 재발급 후
    DB에 재발급한 RefreshToken 업데이트 후 Flush
     */
    public String reIssueRefreshToken(User user) {
        String reIssuedRefreshToken = jwtService.createRefreshToken();
        userRepository.saveAndFlush(user);
        return reIssuedRefreshToken;

    }


    /*
    [엑세스 토큰 체크 & 인증 처리 메소드]
    유효한 토큰이면 AccessToken에서 extractEmail()로 email추출 후 findByEmail()로 해당 이메일 사용하는 유저 객체 반환
    그 유저객체를 saveAuthentication()으로 인증 처리하여
    인증 허가 처리된 객체를 SecurityContextHolder에 담기
    그 후 다음 인증 필터로 진행
     */
    public void checkAccessTokenAndAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        log.info("checkAccessTokenAndAuthentication() 호출!");
        jwtService.extractAccessToken(request) //AccessToken 토큰 추출
                .filter(jwtService :: isTokenValid) //유효한 토큰인지 검증
                .ifPresent(accessToken -> jwtService.extractEmail(accessToken)
                        .ifPresent(email -> userRepository.findByEmail(email)
                                .ifPresent(this :: saveAuthentication)));

        filterChain.doFilter(request,response);
    }


    /*
    [인증 허가 메소드]
    UsernamePasswordAuthenticationToken()의 파라미터
    1. UserDetails 객체 (유저정보)
    2. credential (보통 비밀번호로, 인증 시에는 보통 null로 제거)
    3. UserDetails의 User 객체 안에 Set<GrantedAuthority> authorities이 있어서 getter로 호출한 후에,
       new NullAuthoritiesMapper()로 GrantedAuthoritiesMapper 객체를 생성하고 mapAuthorities()에 담기
    SecurityContextHolder.getContext()로 SecurityContext를 꺼낸 후,
    setAuthentication()를 이용하여 위에만든 authentication 객체에 대한 인증 허가 처리
     */
    public void saveAuthentication(User user) {

        //소셜 로그인 유저의 비밀번호 임의 설정하여 소셜로그인 유저도 인증 되도록 설정
        //소셜로그인인경우 password가 null이여서 인증 처리 시 password가 null이면 안되므로 랜덤 패스워드 부여함
        String password = user.getPassword();
        if(password == null) {
            password = PasswordUtil.generateRandomPassword();
        }

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(password)
                .roles(user.getRole().name())
                .build();

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null,
                        grantedAuthoritiesMapper.mapAuthorities(userDetails.getAuthorities()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

    }

}
